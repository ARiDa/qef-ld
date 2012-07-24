/*
* CoDIMS version 1.0 
* Copyright (C) 2006 Othman Tajmouati
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package ch.epfl.codimsd.qeef;

import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.connection.TransactionMonitor;
import ch.epfl.codimsd.exceptions.CodimsException;
import ch.epfl.codimsd.exceptions.cacheManager.CacheManagerException;
import ch.epfl.codimsd.exceptions.cacheManager.CacheManagerInitializationException;
import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorException;
import ch.epfl.codimsd.qeef.QueryManagerImpl;
import ch.epfl.codimsd.qeef.discovery.datasource.dataSourceWsmoDB.Query;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.query.IRIParser;

import java.util.LinkedHashMap;
import java.util.Map;

import java.sql.ResultSet;

import org.wsmo.datastore.DataStore;
import org.wsmo.factory.Factory;
import org.wsmo.common.Entity;
import org.wsmo.common.IRI;
import org.wsmo.factory.WsmoFactory;

/**
 * The CacheManager component keeps tracks of the IRIs requested by other components.
 * The objects corresponding to those IRIs are stored in a LinkedHashMap java objects.
 * 
 * In order to retrieve objects, the CacheManager uses the TransactionMonitor or the
 * WSMORepository. Different ways of accessing objects could be added in the getIRI method.
 * For non WSMO objects, the user sends the following IRI to getIRI(String IRI) :
 * 		"IRI_REQUEST?[Query="SQL_REQUEST"]"
 * 
 * @author Othman Tajmouati.
 */
public class CacheManager {

	/**
	 * Private Class that contains the LinkedHashMap storing objects.
	 * This is the common way to implement caches.
	 */
	private static Cache cache;
	
	/**
	 * CacheManager singleton.
	 */
	private static CacheManager ref;
	
	/**
	 * Default constructor. It gets the maximum cache size from the Catalog
	 * and initializes the Cache object.
	 * 
	 * @throws CacheManagerInitializationException
	 */
	private CacheManager() throws CacheManagerInitializationException {
		
		String cacheSize = null;
		
		try {
			// Get the Cache size.
			CatalogManager catalogManager = CatalogManager.getCatalogManager();
			cacheSize = (String) catalogManager.getSingleObject("initialConfig","Value", 
					"KeyName='" + Constants.CacheSize + "'");
		} catch (CatalogException ex) {
			throw new CacheManagerInitializationException("Error reading from " +
					"Catalog in CacheManager constructor : " + ex.getMessage()); 
		}
		
		// Creates a new cache object.
		cache = new Cache(Integer.parseInt(cacheSize.trim()));
	}
	
	/**
	 * @return the CacheManager singleton.
	 * 
	 * @throws CacheManagerInitializationException
	 */
	public static synchronized CacheManager getCacheManager() throws CacheManagerInitializationException {
		
		if (ref == null) {
			ref = new CacheManager();
		} else
			throw new CacheManagerInitializationException("CacheManager already intilialized");
		
		return ref;
	}
	
	/**
	 * Private Class that extends the LinkedHashMap java object. It stores the requested objects.
	 * 
	 * @author Othman Tajmouati.
	 */
	private static class Cache extends LinkedHashMap<String, Object> {

		/**
		 * For the Serializer.
		 */
		private static final long serialVersionUID = 1L;
		
		/**
		 * Maximum cache size.
		 */
		private int maxSize;

		/**
		 * Default constructor.
		 * 
		 * @param maxSize maximum cache size.
		 */
        public Cache(int maxSize) {
            
        	// Call superconstructor LinkedHashMap.
        	super(64, .75f, true);
        	
        	// Set max cache size.
            this.maxSize = maxSize;
        }

        /** 
         * Remove the eldest entry from the cache. This method is called
         * when there's an insertion in the cache, and the cache has reached its 
         * maximum size.
         * 
         * @param eldest eldest entry in this cache.
         * @return true an entry was removed, false otherwise.
         */
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxSize;
        }
    }
	
	/**
	 * Retrieve an object given its IRI and its implementation class.
	 * The class is needed to access the WSMORepository.
	 * 
	 * First, we try to retrieve the IRI from the cache. If the object is not stored in the cache, 
	 * we call the TransactionMonitor or the WSMORepository to retrieve the object. The TransactionMonitor
	 * is called, if the parameter clazz is set to null; the TransactionMonitor then execute the request
	 * specified in the IRI string. The latter could be defined as follows : IRI_DATABASE?[Query="SQL_REQUEST"]", 
	 * where the fields in uppercase are user requirements. On the other hand, if the parameter "clazz" is not null,
	 * the WSMORepository is called in order to retrieve the object.
	 * 
	 * @param IRI IRI of the object to retrieve. When the user request an object from a database, the IRI is a query plus 
	 * a condition.
	 * @param clazz the class implementation of the object (requirement of WSMORepository).
	 * @return the requested object.
	 * 
	 * @throws CacheManagerException
	 */
	public synchronized Object getIRI(String IRI, Class clazz) throws CacheManagerException {
		
		Object obj = null;
		
		// Get the object from the cache if the IRI is there.
		if (cache.containsKey(IRI)) {
			
			obj = cache.get(IRI);
			return obj;
		
		} else {
			
			// if "clazz" equal to null, the IRI is an sql request; if not, the IRI is a WSMO identifier.
			if (clazz == null) {
				obj = callTransactionMonitor(IRI);
			} else {
				obj = callWSMORepository(IRI, clazz);
			}
			
			// Put the object in the cache.
			if (obj != null)
				cache.put(IRI, obj);
			else
				throw new CacheManagerException("Undefined error : IRI could not be loaded");
		}
		
		return obj;
	}
	
	/**
	 * Retrieve an object given its IRI. This method is called in order to retrieve objects
	 * using the TransactionMonitor.
	 * 
	 * @param IRI the IRI of the object.
	 * @return requested object.
	 * 
	 * @throws CacheManagerException
	 */
	public synchronized Object getIRI(String IRI) throws CacheManagerException {
		
		Object obj = null;
		
		// Check if the IRI is in the cache.
		if (cache.containsKey(IRI)) {
			obj = cache.get(IRI);
			return obj;
		} else
			obj = callTransactionMonitor(IRI);
		
		if (obj != null) {
			cache.put(IRI, obj);
		} else {
			throw new CacheManagerException("Undefined error : IRI could not be loaded");
		}
		
		return obj;
	}
	
	/**
	 * Retreive an object from a WSMO Repository.
	 * 
	 * @param IRI IRI of the object.
	 * @param clazz implementation class of the object.
	 * @return requested object.
	 * 
	 * @throws WSMXException
	 */
	private Object callWSMORepository(String IRI, Class clazz) throws CacheManagerException {
		
		Entity entity = null;
		DataStore dataStore = Factory.createDatastore(null);
		WsmoFactory wsmoFactory = Factory.createWsmoFactory(null);
		IRI iri = wsmoFactory.createIRI(IRI);
		entity = dataStore.load(iri, clazz);
		
		return entity;
	}
	
	/**
	 * Execute a request using the TransactionMonitor.
	 * 
	 * @param IRI sql request with condition.
	 * @return the requested object corresponding to this IRI.
	 * 
	 * @throws CacheManagerException
	 */
	private Object callTransactionMonitor(String IRI) throws CacheManagerException {
		
		// Initialize the IRI Parser.
		IRIParser iriParser = new IRIParser(IRI);
		int type = iriParser.getQueryType();
		String internalIRI = iriParser.getInternalIRI();
		
		// Intializations.
		Query query = new Query();
		int id = 0;
		ResultSet rset = null;
		TransactionMonitor tm = null;
		
		try {
			
			// Get a connection to the TransactionMonitor.
			tm = TransactionMonitor.getTransactionMonitor();
			id = tm.open(internalIRI);
		
		} catch (TransactionMonitorException ex) {
			throw new CacheManagerException(ex.getMessage());
		}
		
		// Construct a normal sql query or set required parameters if the user
		// request a wsmo file stored in a xml schema database.
		if (type == Constants.IS_SQL_QUERY) {
			
			query.setStringRequest(iriParser.getParameter(Constants.sqlQueryTag));
		
		} else if (type == Constants.IS_XMLDATASOURCE_QUERY) {
			
			String projection = iriParser.getParameter(Constants.projectionTag);
			String condition = iriParser.getParameter(Constants.conditionTag);
			String language = iriParser.getParameter(Constants.languageTag);
			
			query.setCondition(condition);
			query.setLanguage(language);
			query.setProjection(projection);
		
		} else
			throw new CacheManagerException("Query type undefined");
		
		try {
			
			// Execute the request and retrieve the object.
			rset = tm.executeQuery(id, query);
			query.setStringRequest(Constants.COMMIT);
			tm.executeQuery(id, query);
			tm.close(id);
		
		} catch (TransactionMonitorException ex) {
			throw new CacheManagerException(ex.getMessage());
		}
		
		return rset;
	}
	
	/**
	 * 
	 * @param args
	 * @throws CacheManagerException
	 * @throws CodimsException
	 */
	public static void main(String args[]) throws CacheManagerException, CodimsException {
		
		@SuppressWarnings("unused") 
		QueryManagerImpl queryManagerImpl = null;
		
		try {
			queryManagerImpl = QueryManagerImpl.getQueryManagerImpl();
		} catch (CodimsException ex) {
			ex.printStackTrace();
		}
		
		CacheManager cm = CacheManager.getCacheManager();
		String IRI = Constants.IRI_OPERATORS_LBD7 + "?[Query=\"SELECT * FROM QoSReport\"]";
		@SuppressWarnings("unused")
		Object obj = cm.getIRI(IRI);
		IRI = Constants.IRI_OPERATORS_LBD7 + "?[Query=\"SELECT * FROM QOSConformance\"]";
		obj = cm.getIRI(IRI);
		IRI = Constants.IRI_OPERATORS_LBD7 + "?[Query=\"SELECT * FROM QoSReport\"]";
		obj = cm.getIRI(IRI);
	}
}

