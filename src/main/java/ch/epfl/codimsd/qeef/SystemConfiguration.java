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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.config.AppConfig;
import ch.epfl.codimsd.exceptions.initialization.InitialConfigurationException;
import ch.epfl.codimsd.qeef.util.Constants;

/**
 * The SystemConfiguration class keeps Codims parameters. It loads the codims.properties file.
 * and keep the values in a Hashtable.
 * 
 * @author Othman Tajmouati 
 *
 */
public class SystemConfiguration {

	/**
	 * Indicates if the system has been initialized. This is the case when 
	 * come components need to check the system state.
	 */
	private static boolean systemInitialized = false;
	
	/**
	 * Log4j logger.
	 */
	protected static Logger logger = Logger.getLogger(SystemConfiguration.class.getName());
	
	/**
	 * Keeps system configuraton values.
	 */
	private static Hashtable<String,String> systemConfigInfo;
	
	private static String home = AppConfig.CODIMS_HOME;
	
	/**
	 * Load intial properties, put the codims-home and the CatalogIRI in the hashtable.
	 * The method assumes that codims-home is located under user directory. When 
	 * Codims interfact with other systems, users can specify another location
	 * by calling the setHome method before calling this method.
	 * 
	 * @throws InitialConfigurationException
	 */
	public static void loadSystemConfiguration() throws InitialConfigurationException {
		
		// Initializations & Put codims-home location in both the BlackBoard and the
		// Systemconfiguration hashtable.
		systemConfigInfo = new Hashtable<String,String>();
		systemConfigInfo.put(Constants.HOME, home);
		
		BlackBoard bl = BlackBoard.getBlackBoard();
		bl.put(Constants.HOME, home);
		
		// Read "codims.properties"
		readProperties();
		
		// Fill the system config info hashtable with this information
		// It will be used by the Optimizer and the DistributedManager
		if (systemConfigInfo.get(Constants.LOCAL_WEB_SERVICE) == null)
                        systemConfigInfo.put(Constants.LOCAL_WEB_SERVICE, Constants.addressLocalWebService);

              //  System.out.println(systemConfigInfo.get(Constants.LOCAL_WEB_SERVICE));
		systemInitialized = true;
	}
	
	public static boolean getSystemState() {
		return systemInitialized;
	}
	
	/**
	 * Overwrite the codims-home location if needed.
	 * @param home codims-home location.
	 */
	public static void setHome(String newHome) {
		home = newHome;
	}
	
	/**
	 * In remote nodes, there's no codims.properties file. Hence we call this
	 * method to create needed remote parameters.
	 */
	public static void loadRemoteSystemConfiguration() throws InitialConfigurationException {
	
		// Get the BalckBoard
		BlackBoard bl = BlackBoard.getBlackBoard();

                // Retrieve the Catalog IRI from the BlackBoard.
                String catalogInfo = (String) bl.get(Constants.IRICatalog);

                // Put codims-home (located in GLOBUS_LOCATION)
		String globusLocation = System.getenv("GLOBUS_LOCATION");
		
		//String codimsHome = globusLocation + File.separator + "codims-home";
                String codimsHome = home;
                System.out.println("home = " + codimsHome);

		File codimsHomeFile = new File(codimsHome);
		if (!codimsHomeFile.exists()) {
			codimsHome = globusLocation;
		}

		// build the system config hashtable and insert the Catalog IRI.
		systemConfigInfo = new Hashtable<String,String>();
		systemConfigInfo.put(Constants.IRICatalog, catalogInfo);
		systemConfigInfo.put(Constants.HOME, codimsHome);

                // Read "codims.properties"
		readProperties();

                catalogInfo = (String) bl.get(Constants.IRICatalog);
		
		bl.put(Constants.HOME, codimsHome);
	}
	
	/**
	 * Return the value corresponding to this key from the System properties.
	 * 
	 * @param key the key.
	 * @return the associated value.
	 */
 	public static String getSystemProperty(String key) {
		return System.getProperty(key);
	}

 	/**
 	 * Return the value corresponding to this key from the system config hashtable.
 	 * 
 	 * @param key the key.
 	 * @return the associated value.
 	 */
	public static synchronized String getSystemConfigInfo(String key) {

		return systemConfigInfo.get(key);
	}

	/**
	 * Read codims propertie file.
	 */
	private static void readProperties() throws InitialConfigurationException {

		// Read properties file.
	    Properties properties = new Properties();
	    
	    try {
	    	String fileProperties = home + "codims.properties";
            properties.load(new FileInputStream(fileProperties));
	        Enumeration propertyNameEnum = properties.propertyNames();
	        
	        while (propertyNameEnum.hasMoreElements()) {
	        	
	        	String propertyName = (String) propertyNameEnum.nextElement();
	        	systemConfigInfo.put(propertyName, properties.getProperty(propertyName));
	        }
	        
	    } catch (IOException ex) {
	    	throw new InitialConfigurationException("Could not read codims.properties : " + ex.getMessage());
	    }
	}
}

