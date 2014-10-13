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

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorConnectionException;

/**
 * ConnectionObject class manages the creation of an sql connection in Codims.
 * 
 * @author Othman Tajmouati
 */
public class ConnectionObject {

	/**
	 * The url of the database.
	 */
	private String url;
	
	/**
	 * The user name.
	 */
	private String user;
	
	/**
	 * The password associated with this username.
	 */
	private String pwd;
	
	/**
	 * The driver required to connect to the database. Ex : oracle driver, derby driver, sqlite driver, etc.
	 */
	private String driverName;
	
	/**
	 * The sql connection.
	 */
	private java.sql.Connection sqlConnection;
	
	/**
	 * Semaphore on the connection.
	 */
	private boolean semaphore;
	
	/**
	 * Log4j logger.
	 */
	protected Logger logger;
	
	/**
	 * Default constructor.
	 * 
	 * @param driverName - the driver name
	 * @param url - The url of the database
	 * @param user - the username
	 * @param pwd - the passwiord associated to this username
	 */
	public ConnectionObject(String driverName, String url, String user, String pwd) {
		
		this.url = url;
		this.user = user;
		this.pwd = pwd;
		this.driverName = driverName;
	}
	
	/**
	 * Creates an sql connection.
	 * 
	 * @return
	 */
	public Connection createConnection() throws TransactionMonitorConnectionException {
		
		try {
			
			logger = Logger.getLogger(ConnectionObject.class.getName());
			// logger.debug(driverName);
			
			Class.forName(driverName);
			
			if (driverName.equalsIgnoreCase("oracle.jdbc.OracleDriver")) {
				// create an sql connection if the driver is the OracleDriver
				sqlConnection = DriverManager.getConnection(url, user, pwd);
			} else {
				Properties properties = new Properties();
				properties.setProperty("user", user);
				properties.setProperty("password", pwd);
				sqlConnection = DriverManager.getConnection(url, properties);
			}
		}
		catch (SQLException ex) {
			throw new TransactionMonitorConnectionException("Connection exception : " + ex);
		} catch(ClassNotFoundException ex) {
			throw new TransactionMonitorConnectionException("Connection exception : " + ex);
		}
		return sqlConnection;
	}
	
	/**
	 * Lock the access to an sql connection object and returns the connection.
	 * 
	 * @return
	 */
	public java.sql.Connection getConnection() {
		
		if (!semaphore) {
			
			setSemaphore();
			return sqlConnection;
		
		} else {
			return null;
		}
	}
	
	/**
	 * Sets the semaphore to true.
	 */
	public void setSemaphore() { semaphore = true; }
	
	/**
	 * Sets the semaphore to false. 
	 */
	public void releaseSemaphore() { semaphore = false; }
}

