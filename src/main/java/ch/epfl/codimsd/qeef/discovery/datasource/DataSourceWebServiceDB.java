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
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.wsmo.service.WebService;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.dataSource.DataSourceException;
import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.exceptions.dataSource.UnsupportedDataSourceTypeException;
import ch.epfl.codimsd.qeef.discovery.DiscoveryOptimizer;
import ch.epfl.codimsd.qeef.relational.Column;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.test.WebServiceDB;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.types.WsmoWebService;
import ch.epfl.codimsd.qeef.util.Constants;

/**
 * The DataSourceWebServiceDB reads from a database and returns all WsmoWebService objects from it.
 * Each WsmoWebService object, contains a Wsmo WebService.
 * 
 * @author Othman Tajmouati.
 */
public class DataSourceWebServiceDB  extends DataSource {

	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DiscoveryOptimizer.class.getName());
	
	/**
	 * Cursor indicating the current WebServiceExt in the list of WebServiceExt objects.
	 */
	private int cursor;

	/**
	 * IRI of the database to read from.
	 */
	private String IRI;
	
	/**
	 * Number of webServices in the database.
	 */
	protected int numberOfWebServicesInFile;
	
	/**
	 * List containing our WebServiceExt objects.
	 */
	protected LinkedList<WsmoWebService> webServices;
	
	/**
	 * Default constructor. 
	 * 
	 * @param alias name of the dataSource.
	 * @param metadata metadata of this dataSource.
	 * @param IRI iri of the database to connect to.
	 * @throws CatalogException
	 */
	public DataSourceWebServiceDB(String alias, TupleMetadata metadata, String IRI)
			throws CatalogException {
		
		// Super constructor.
		super(alias, null);
		
		// Set the iri of the database.
		this.IRI = IRI;
		
		// Initialize the list storing webservice.
		webServices = new LinkedList<WsmoWebService>();
	}
	
	/**
	 * Open the dataSource.
	 */
	public void open() throws DataSourceException {

		try {
			
			// Call the WebServiceDB class and creates and list of Wsmo WebService.
			// The WebServiceDB class reads WebServices in a database. Those WebService are stored in 
			// several pieces.
			WebServiceDB wsDB = new WebServiceDB(IRI);
			ArrayList<WebService> webServicesDB = wsDB.readDB();
			
			// Construct the list of webServices.
			for (int i = 0; i < webServicesDB.size(); i++) {
				
				WsmoWebService wsmowebService = new WsmoWebService(webServicesDB.get(i));
				webServices.add(wsmowebService);
			}
			
			numberOfWebServicesInFile = webServices.size();
			cursor = 0;
			
			// set the metadata.
			metadata = new TupleMetadata();
			Column column = new Column("WsmoWebService", Config.getDataType(Constants.WEBSERVICE_WSMO), 1, 0, false);
			metadata.addData(column);
			setMetadata(metadata);
		
		} catch (SQLException ex){
			throw new DataSourceException("Could not open the DataSourceWebServiceDB : " + ex.getMessage());
		} catch (ClassNotFoundException ex) {
			throw new DataSourceException("Could not find the driver to open the DataSourceWebServiceDB: " + ex.getMessage());
		} catch (UnsupportedDataSourceTypeException ex) {
			throw new DataSourceException("UnsupportedDataSourceTypeException in DataSourceWebServiceDB : " + ex.getMessage());
		} catch (Exception ex) {
			throw new DataSourceException("Exception in DataSourceWebServiceDB: " + ex.getMessage());
		}
	}
	
	/**
	 * Read tuple by tuple from the list of webServices.
	 * 
	 * @return tuple containing WsmoWebService object.
	 */
	public DataUnit read() throws DataSourceException {
		
		// If there are no more interfaces to return, then return null.
		if (numberOfWebServicesInFile == 0) {
        	return null;
		}

		// Construct a new tuple.
		Tuple tuple = new Tuple();
        tuple.addData((Type)webServices.get(cursor));

        numberOfWebServicesInFile --;
        cursor ++;
        
        return tuple;
	}
	
	/**
	 * Close the DataSource.
	 */
	public void close() throws DataSourceException {
		
		cursor = 0;
		webServices = null;
		logger = null;
	}
}

