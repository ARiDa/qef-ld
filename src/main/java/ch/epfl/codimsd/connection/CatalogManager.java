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
package ch.epfl.codimsd.connection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.FileNotFoundException;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Logger;


import ch.epfl.DerbyServerStarter;
import ch.epfl.codimsd.config.AppConfig;
import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.shutdown.ShutDownException;
import ch.epfl.codimsd.helper.ScriptRunner;
import ch.epfl.codimsd.qeef.SystemConfiguration;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.qosdisc.wsmx.LoadDatabaseIntegratedDemo;

/**
 * The CatalogManager class manages the communication with the Catalog. Use this class to 
 * execute sql queries (strings or files), start the Derby Network Server, get metadata 
 * from the catalog. The catalog IRI is defined under {@link ch.epfl.codimsd.qeef.util.Constants}.
 * 
 * @author Othman Tajmouati
 */
public class CatalogManager {
	
	/**
	 * Derby Network server.
	 */
	private NetworkServerControl server;
	
	/**
	 * Only one sql connection will be created and used for accessing the catalog during
	 * system execution time.
	 */
	private Connection con;
	
	/**
	 * The sql statement.
	 */
	private Statement stmt;
	
	/**
	 * Singleton reference.
	 */
	private static CatalogManager ref;
	
	/**
	 * Log4j logger.
	 */
	protected static Logger logger = Logger.getLogger(CatalogManager.class.getName());

	/**
	 * Default constructor.
	 * 
	 * @param IRI
	 * @throws CatalogException
	 */
	private CatalogManager() throws CatalogException {
	    ref = this;
        try {
            DerbyServerStarter.start();
        } 
        catch (Exception e) {
            throw new CatalogException("Cannot start Derby Catalog Server : " + e.getMessage());
        }   
        connectToCatalog();
	}
    
	/**
	 * getCatalogManager returns the singleton CatalogManager object.
	 * 
	 * @param IRI
	 * @throws CatalogException
	 */
	public static synchronized CatalogManager getCatalogManager() throws CatalogException {
		
		if (ref == null)
			ref = new CatalogManager();
		
		return ref;
	}
	
	/**
	 * connectToCatalog loads the driver and creates an sql connection to the catalog.
	 * 
	 * @param IRI - The IRI of the Catalog {@link ch.epfl.codimsd.qeef.util.Constants}
	 * 
	 * @throws CatalogException
	 */
	private void connectToCatalog() throws CatalogException {
	    try {
			Class.forName(AppConfig.CATALOG_DB_CLIENT_DRIVER);
			logger.debug("Getting connection to: " + AppConfig.CATALOG_URL);
            con = DriverManager.getConnection(AppConfig.CATALOG_URL, AppConfig.CATALOG_USER, AppConfig.CATALOG_PASSWORD);
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			logger.debug("Connection created successfully.");
		} catch (ClassNotFoundException ex) {
			throw new CatalogException("Driver not found in CatalogManager : " + ex.getMessage());
		} catch (SQLException ex) {
			throw new CatalogException("SQL Exception in CatalogManager : " + ex.getMessage());
		}  catch (Exception ex) {
			throw new CatalogException("Exception in CatalogManager : " + ex.getMessage());
		}
	}
	
	/**
	 * executeQueryFile executes a set of sql queries defined in fileName.
	 * 
	 * @param fileName - the file path containing the set of queries. 
	 * 
	 * @throws CatalogException
	 */
	public synchronized void  executeQueryFile(String fileName) throws CatalogException {
		
		String line = null;
		
		try {

			File querySetFile = null;
			BufferedReader in = null;
			
			querySetFile = new File(fileName);
			in = new BufferedReader(new FileReader(querySetFile));

			line = in.readLine();
			while (line != null) {

				boolean statementFound = false;
				String sqlRequest = "";
				while (!statementFound && line != null) {
					
					// Check if the string is not empty, and is not a comment
					if (!line.equalsIgnoreCase("") && !line.substring(0,2).equalsIgnoreCase("--"))
						sqlRequest = sqlRequest + line;

					// Check if the statement is found here and ready to be executed
					if (line.equalsIgnoreCase("") && !sqlRequest.equalsIgnoreCase("")) 
						statementFound = true;
					else
						line = in.readLine();
				}
				
				// Delete the sql ';' character as Derby doesn't accept it
				if (sqlRequest.charAt(sqlRequest.length()-1) == ';')
					sqlRequest = sqlRequest.substring(0,sqlRequest.length()-1);
				
				// Execute the request
				logger.debug(sqlRequest);
				stmt.execute(sqlRequest);
			}

			// If no error occurs commit the set of queries
			con.commit();
	
		} catch (SQLException ex) {
			throw new CatalogException("Error in CatalogManager : " + ex.getMessage());
		} catch (FileNotFoundException ex) {
			throw new CatalogException("CatalogManager exception : " + ex.getMessage());
		} catch (IOException ex) {
			throw new CatalogException("CatalogManager exception : " + ex.getMessage());
		}
	}
	

	
	/**
	 * executeQueryString executes an sql query.
	 * 
	 * @param queryString - the sql query
	 * 
	 * @return the ResultSet object if queryString is a SELECT statement, otherwise the
	 * method returns null
	 * 
	 * @throws CatalogException
	 */
	public synchronized ResultSet executeQueryString(String queryString) 
			throws CatalogException {

		ResultSet rset = null;

		try {
			
			if (queryString.substring(0,6).equalsIgnoreCase(Constants.SELECT)) {
				
				// Execute the SELECT statement and return the ResultSet object
				rset = stmt.executeQuery(queryString);	
				return rset;
			
			} else {
				
				// Execute the sql Statement (other than SELECT) and return null
				stmt.execute(queryString);
				return null;
			}
			
		} catch (SQLException ex) {
			throw new CatalogException(ex.getMessage());
		}
	}
	
	/**
	 * It retrieves the requested type from the catalog. "type" corresponds to a table name.
	 * Example : getObject("intialConfig") returns all the tuples from the "intialconfig table".
	 * Types are defined in the table "ObjectType" under the catalog (see catalog schema for more
	 * informations).
	 * 
	 * @param type - the table name
	 * @return a ResultSet object
	 * @throws CatalogException
	 */
	public synchronized ResultSet getObject(String type) throws CatalogException {
		
		String query = "SELECT query FROM objectType WHERE description='" + type + "'";
		ResultSet rset = null;
		
		try {
		
			ResultSet firstRset = stmt.executeQuery(query);
			firstRset.next();
			String filtredQuery = (String) firstRset.getObject(1);
			rset = stmt.executeQuery(filtredQuery);
		
		} catch (SQLException ex) {
			throw new CatalogException(ex.getMessage());
		}
		
		return rset;
	}
	
	/**
	 * 
	 * It retrieves the requested type (table name) from the catalog and filter the tuples according 
	 * to the specified confition.
	 * 
	 * @param type the table name
	 * @param condition the condition (see catalog schema in order to write the condition)
	 * @return the tuples from "type" that match the condition
	 * @throws CatalogException
	 */
	public synchronized  ResultSet getObject(String type, String condition) 
		throws CatalogException {
		
		String query = "SELECT query FROM objectType WHERE description='" + type + "'";
		ResultSet rset = null;
		
		try {
		
			ResultSet firstRset = stmt.executeQuery(query);
			firstRset.next();
			String filtredQuery = (String) firstRset.getObject(1);
			filtredQuery +=  " AND " + condition;
			rset = stmt.executeQuery(filtredQuery);
		
		} catch (SQLException ex) {
			throw new CatalogException(ex.getMessage());
		}
		
		return rset;
	}
	
	/**
	 * This method returns a the catalog database metadata.
	 * 
	 * @return a sql.DatabaseMetaData object
	 * @throws CatalogException
	 */
	public synchronized DatabaseMetaData getCatalogMetadata() 
	   throws CatalogException{
		
		DatabaseMetaData dbMData=null;	
		try {
		 dbMData= con.getMetaData();
		}
		catch (SQLException sqle){
			throw new CatalogException ("Error getting Catalog Metadata"+ sqle.getMessage());
		}
		return dbMData;
	}
	
	/**
	 * This method returns a single object given a column and a condition constraint.
	 * @param type is the object type, see Constant for possible values;
	 * @param column is the name of column in the catalog relation we want information from;
	 * @param condition - specifies a condition that returns a single tuple of the chosen relation;
	 * @return a sql.DatabaseMetaData object
	 * @throws CatalogException
	 */
	public synchronized Object getSingleObject(String type, String column, String condition) 
		throws CatalogException {
	
		String query = "SELECT " + column + " FROM " + type + " WHERE " + condition;
		Object obj = null;
		if (con == null) {
			logger.debug("null");
		}
	
		try {
			ResultSet rset = stmt.executeQuery(query);
			if (rset.next()) {
	            ResultSetMetaData rsetMetadata = rset.getMetaData();
	            
	            if (rsetMetadata.getColumnCount() > 2)
	                throw new CatalogException("The request returns more than one object.");
	        
	            obj = rset.getObject(1);
			} else {
	            logger.debug("Query: " + query);
			    throw new CatalogException("No result found from query: " + query);
			}
		
	
		} catch (SQLException ex) {
            logger.debug("Query: " + query);
			throw new CatalogException("CatalogManager getSingleObject() error : " + ex.getMessage());
		}
	
		return obj;
	}
	
	/**
	 * closeCatalogManager closes the communication with the CatalogManager
	 * 
	 * @throws ShutDownException
	 */
	public synchronized void closeCatalogManager() throws ShutDownException {
		
		try {
			stmt.close();
			con.close();
			
			if (server != null)
				server.shutdown();
		
		} catch (Exception ex) {
			throw new ShutDownException("Cannot close CatalogManager : " + ex.getMessage());
		}
	}
	
	/**
	 * Creates the derby catalog server.
	 */
	public void createQoSDiscDB() {
		LoadDatabaseIntegratedDemo.main(null);
	}

	/**
	 * Creates the derby catalog server.
	 */
	public void setup() {
        try {
            ScriptRunner sr = new ScriptRunner(con, false, false);
            logger.info("Executing " + AppConfig.CATALOG_SCRIPT_PATH + " script...");
            sr.runScript(new FileReader(new File(AppConfig.CATALOG_SCRIPT_PATH)));
            con.commit();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException e1) {
                logger.error(e1.getMessage(), e1);
                e1.printStackTrace();
            }
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        }
		
	}
	
	/**
	 * 
	 * @return
	 */
	public Connection getConnection() {
	    return this.con;
	}

	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
	    SystemConfiguration.loadSystemConfiguration();
	}
}

