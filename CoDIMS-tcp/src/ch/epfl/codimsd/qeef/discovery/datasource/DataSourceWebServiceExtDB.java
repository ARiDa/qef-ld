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
package ch.epfl.codimsd.qeef.discovery.datasource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;
import org.wsmo.service.WebService;

import com.ontotext.wsmo4j.service.InterfaceImpl;
import com.ontotext.wsmo4j.service.WebServiceImpl;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.dataSource.DataSourceException;
import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.exceptions.dataSource.UnsupportedDataSourceTypeException;
import ch.epfl.codimsd.qeef.relational.Column;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.test.WebServiceDB;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.types.WebServiceExt;
import ch.epfl.codimsd.qeef.util.Constants;

/**
 * The DataSourceWebServiceExtDB reads from a database and returns all WebServiceExt objects from it.
 * Each WebServiceExt object, contains a Wsmo WebService and Wsmo Interface.
 * 
 * @author Othman Tajmouati.
 */
public class DataSourceWebServiceExtDB extends DataSource {

	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DataSourceWsmoText.class.getName());
	
	/**
	 * Cursor indicating the current WebServiceExt in the list of WebServiceExt objects.
	 */
	private int cursor;
	
	/**
	 * The iri of the database to read from.
	 */
	private String IRI;
	
	/**
	 * The number of interfaces in the Database.
	 */
	protected int numberOfInterfacesInFile;
	
	/**
	 * List containing our WebServiceExt objects.
	 */
	protected LinkedList<WebServiceExt> webServices;
	
	/**
	 * Default constructor. 
	 * 
	 * @param alias name of the dataSource.
	 * @param metadata metadata of this dataSource.
	 * @param IRI iri of the database to connect to.
	 * @throws CatalogException
	 */
	public DataSourceWebServiceExtDB(String alias, TupleMetadata metadata, String IRI)
			throws CatalogException {
		
		// Call super constructor.
		super(alias, null);

		// Set the database iri.
		this.IRI = IRI;
		
		// Initializes the list where we storr our webservices.
		webServices = new LinkedList<WebServiceExt>();
	}
	
	/**
	 * Open the DataSource and reads all the tuples in the DataBase.
	 */
	public void open() throws DataSourceException {

		try {
			
			// Call the WebServiceDB class and creates and list of Wsmo WebService.
			// The WebServiceDB class reads WebServices in a database. Those WebService are stored in 
			// several pieces.
			WebServiceDB wsDB = new WebServiceDB(IRI);
			ArrayList<WebService> webServicesDB = wsDB.readDB();
			
			// Transform WebService List to WebServiceExt list.
			for (int i = 0; i < webServicesDB.size(); i++) {
				
				Set interfaces = webServicesDB.get(i).listInterfaces();
				Iterator itInterfaces = interfaces.iterator();
				
				while(itInterfaces.hasNext()) {
					
					WebServiceExt webServiceExt = new WebServiceExt();
					webServiceExt.setWebService((WebServiceImpl)webServicesDB.get(i));
					webServiceExt.setInterface((InterfaceImpl) itInterfaces.next());
					webServices.add(webServiceExt);
				}
			}

			numberOfInterfacesInFile = webServices.size();
			cursor = 0;
			
			// Set the metadata.
			metadata = new TupleMetadata();
			Column column = new Column("WebServiceExt", Config.getDataType(Constants.WEBSERVICE_EXT), 1, 0, false);
			metadata.addData(column);
			setMetadata(metadata);
		
		} catch (SQLException ex){
			throw new DataSourceException("Could not open the DataSourceWebServiceExtDB : " + ex.getMessage());
		} catch (ClassNotFoundException ex) {
			throw new DataSourceException("Could not find the driver to open the dataSource : " + ex.getMessage());
		} catch (UnsupportedDataSourceTypeException ex) {
			throw new DataSourceException("UnsupportedDataSourceTypeException in DataSourceWebServiceExtDB : " + ex.getMessage());
		} catch (Exception ex) {
			throw new DataSourceException("Exception in DataSourceWebServiceExtDB : " + ex.getMessage());
		}
	}
	
	/**
	 * Read tuple by tuple and return WebServiceExt object to Scan operator.
	 */
	public DataUnit read() throws DataSourceException {
		
		// If there are no more interfaces to return, then return null.
		if (numberOfInterfacesInFile == 0) {
        	return null;
		}

		// Constrcut a new tuple, insert the WebServiceExt object in it.
		Tuple tuple = new Tuple();
        tuple.addData((Type)webServices.get(cursor));

        numberOfInterfacesInFile --;
        cursor ++;
        
        return tuple;
	}
	
	/**
	 * Close the dataSource.
	 */
	public void close() throws DataSourceException {
		
		cursor = 0;
		webServices = null;
		logger = null;
	}
}

