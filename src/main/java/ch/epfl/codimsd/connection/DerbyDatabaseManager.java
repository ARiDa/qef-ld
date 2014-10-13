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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Logger;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.qeef.SystemConfiguration;
import ch.epfl.codimsd.qeef.util.Constants;

/**
 * The DerbyDataBaseManager class manages the creation of a Derby database. On creation, the user
 * can specify a set of sql queries to execute on the Database.
 * 
 * @author Othman Tajmouati
 *
 */
public class DerbyDatabaseManager {

	/**
	 * Sql connection
	 */
	private Connection con;
	
	/**
	 * Sql statement
	 */
	private Statement stmt;
	
	/**
	 * Derby Network Server
	 */
	private NetworkServerControl server;
	
	/**
	 * Log4j logger
	 */
	private Logger logger;
	
	/**
	 * Default constructor
	 *
	 */
	public DerbyDatabaseManager() {
		
		logger = Logger.getLogger(DerbyDatabaseManager.class.getName());
	}
	
	/**
	 * Executes a set of sql queries in a file.
	 * 
	 * @param fileName
	 * @throws CatalogException
	 */
	private void  executeQueryFile(String fileName) throws Exception {
		
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
				while (!statementFound && (line != null)) {
										
					// Check if the string is not empty, and is not a comment
					if (!line.equalsIgnoreCase("") && !line.substring(0,2).equalsIgnoreCase("--"))
						sqlRequest = sqlRequest + line;

					// Check if the statement is found here and ready to be executed
					if ((line.equalsIgnoreCase("")) && !sqlRequest.equalsIgnoreCase("")) 
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
	
		} catch (SQLException ex1) {
			throw new Exception("Error in CatalogManager : " + ex1.getMessage());
		} catch (FileNotFoundException ex2) {
			throw new Exception("CatalogManager exception : " + ex2.getMessage());
		} catch (IOException ex3) {
			throw new Exception("CatalogManager exception : " + ex3.getMessage());
		}
	}
	
	/**
	 * Delete the database.
	 * 
	 * @param url - url of the database to delete.
	 */
	private void deleteDatabase(String url) {
		
		int indexOfHome = url.indexOf("codims-home");
		indexOfHome += "codims-home".length();
		String home = SystemConfiguration.getSystemConfigInfo(Constants.HOME) + File.separator;
		String filePath = url.substring(indexOfHome+1,url.length());
		File file = new File(home + filePath);
		
		if (file.exists()) {
			
			boolean msg = deleteDir(file);
			
			if (msg == false)
				logger.debug("Cannot delete database !");
			else
				logger.debug("Database deleted ...");
		}
	}
	
	/**
	 * Create a Derby database.
	 * 
	 * @param databaseName - the name of the database to create
	 * @param scriptPaths - a list of scripts containing sql requests
	 * @param properties - Java properties containing username, passwords, drivername and 
	 * url of the database. if the Properties object is null, the method uses its default parameter :
	 * username = "CODIMS", password = "CODIMS", drivername = "org.apache.derby.jdbc.ClientDriver",
	 * and url = codims-home
	 */
	public void createDatabase(String databaseName, String[] scriptPaths, Properties properties) 
		throws Exception {

		if (properties == null)
			properties = new Properties();
		
		// Sets the properties (url, user, pwd, drivername)
		String url = properties.getProperty("url") == null ? 
				"jdbc:derby://127.0.0.1:1527/codims-home/" : properties.getProperty("url");
                //"jdbc:derby://localhost:1527/codims-home/" : properties.getProperty("url");
		String user = properties.getProperty("user") == null ? 
				"" : properties.getProperty("user");
		String pwd = properties.getProperty("pwd") == null ? 
				"" : properties.getProperty("pwd");
		String drivername = properties.getProperty("drivername") == null ? 
				"org.apache.derby.jdbc.ClientDriver" : properties.getProperty("drivername");
		
		Properties prop = new Properties();
		prop.setProperty(user, pwd);
			Class.forName(drivername);
	
		url += databaseName;
		deleteDatabase(url);
		url += ";create=true" ;
		
		// Start the Network Server
		server = new NetworkServerControl(InetAddress.getLocalHost(),1527);
		server.start(null);
		
		// Build the local host IP adress and replace the substring "localhost" in the SystemConfigFile
		int indexOfLocalhost = url.indexOf("localhost");
		String localIP = InetAddress.getLocalHost().getHostAddress();
               // String localIP = "127.0.0.1";
		url = url.substring(0, indexOfLocalhost) + 
			localIP + url.substring(indexOfLocalhost+"localhost".length(), url.length());
		
		// Create the DB 
		con = DriverManager.getConnection(url , properties);
		stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
			ResultSet.CONCUR_UPDATABLE);
		
		// Execute requests
		if (scriptPaths.length != 0) {
			for (String scripts : scriptPaths) {
				executeQueryFile(scripts);
			}
		}
		
		stmt.close();
		con.close();

		CatalogManager cm = CatalogManager.getCatalogManager();
		ch.epfl.codimsd.qeef.util.Util.dump(cm.getObject("initialConfig"));
		
		server.shutdown();
	}
	
	/**
	 * Delete the Derby database, ie delete directories and subdirectories. This method is called
	 * by deleteDatabase(String url)
	 * 
	 * @param dir - the file to delete
	 * @return true if the database is deleted, false otherwise
	 */
	private boolean deleteDir(File dir) {
        
		if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
	
	/**
	 * Example on how to create a Derby Database. Here we create the codims-catalog.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
		
			// Initialization of the System
			SystemConfiguration.loadSystemConfiguration();
			DerbyDatabaseManager dbManager = new DerbyDatabaseManager();
		
			// Construct the paths to the creation scripts
			String path = SystemConfiguration.getSystemConfigInfo(Constants.HOME) + File.separator + "SQL Requests" + File.separator;
			String[] paths = new String[2];
			String createTablesScript = "CatalogTCP.txt";
			String insertIntoCatalogScript = "CatalogInsertion.txt";
			paths[0] = path + createTablesScript;
			paths[1] = path + insertIntoCatalogScript;
			
				// Build user and pwd properties
			Properties properties = new Properties();
			properties.setProperty("user","CODIMS");
			properties.setProperty("pwd","CODIMS");
		
			// Create the database
			dbManager.createDatabase("DerbyCatalog", paths, properties);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

