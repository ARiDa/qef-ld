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

import java.net.InetAddress;
import java.sql.Connection;
import java.util.Vector;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorConnectionException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorMaximumNumberConnectionReachedException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorSQLException;

/**
 * The ConnectionPool class defines a connection pool to a specific database IRI. In codims, a connection
 * pool is a set of connections to grouped in a Java Collection. When a user requests a connection, the first
 * free connection is returned; if there are no free connections a new one is created and finally if the maximim amount
 * of connections is already created no connections is returned.
 * 
 * @author Othman Tajmouati
 *
 */
public class ConnectionPool {

	/**
	 * The maximum number of sql connections to the same database.
	 * This paramater is taken form the Catalog.
	 */
	private int maxConnections;
	
	/**
	 * The url of the database.
	 */
	private String url;
	
	/**
	 * The username.
	 */
	private String user;
	
	/**
	 * The password associated to the username.
	 */
	private String pwd;
	
	/**
	 * The driver required to connect to the database.
	 */
	private String driverName;
	
	/**
	 * The Vector structure where the connections are stored.
	 */
	private Vector<Connection> freeConnections;
	
	/**
	 * This paramtere indicates if we have reached the maximum number of connections.
	 */
	private int checkedOut;
	
	/**
	 * Default constructor that creates a connection to a database IRI.
	 * 
	 * @param IRI - The IRI of the database
	 * @throws CatalogException
	 */
	public ConnectionPool(String IRI) throws CatalogException {
		freeConnections = new Vector<Connection>();
		checkedOut = 0;
		createConnectionParameters(IRI);
	}
	
	/**
	 * This method creates the connection parameters, ie maximum number of connections, username, 
	 * password, drivername. All the parameters are taken from the Catalog.
	 * 
	 * @param IRI - The IRI of the database.
	 * @throws CatalogException
	 */
	private void createConnectionParameters(String IRI) throws CatalogException {
		
		try {
			
			CatalogManager catalogManager = CatalogManager.getCatalogManager();
			Integer integer = (Integer) catalogManager.getSingleObject("ds_database", "maxconnection", 
					"iri='"+IRI+"'");
			this.maxConnections = integer.intValue();
			this.url = (String) catalogManager.getSingleObject("ds_database", "host", "iri='"+IRI+"'");
			int indexOfLocalhost = url.indexOf("localhost");
                        System.out.println(url);
			if (indexOfLocalhost > 0) {
			
				try {
		
					String localIP = InetAddress.getLocalHost().getHostAddress();
                                       // String localIP = "127.0.0.1";
					url = url.substring(0, indexOfLocalhost) + 
					localIP + url.substring(indexOfLocalhost+"localhost".length(), url.length());
	
				} catch(Exception ex) {
					throw new CatalogException("UnknowHostException in " +
							"TransactionMonitor : " + ex.getMessage());
				}
			}
		
			this.user = (String) catalogManager.getSingleObject("ds_database", "username", "iri='"+IRI+"'");
			this.pwd = (String) catalogManager.getSingleObject("ds_database", "passwrd", "iri='"+IRI+"'");
			this.driverName = (String) catalogManager.getSingleObject("ds_database", "driverName", "iri='"+IRI+"'");			
                        System.out.println(user + " " + pwd + " " + driverName);
		} catch (CatalogException ex) {
			throw new CatalogException("CatalogException in ConnectionPool : " + ex.getMessage());
		}
		
	}
	
	/**
	 * Returns the sql connection to this registred database.
	 * 
	 * @return - An sql connection.
	 * @throws TransactionMonitorMaximumNumberConnectionReachedException
	 */
	 public synchronized Connection getConnection() throws TransactionMonitorException {
		 
		 Connection con = null;
		 if (freeConnections.size() > 0) {
			 //	Pick the first Connection in the Vector to get round-robin usage 
			 con = (Connection) freeConnections.firstElement();
			 freeConnections.removeElementAt(0);
			 try {
                 if (con.isClosed()) {
                     // Try again recursively
                     con = getConnection();
                 }
             }
             catch (SQLException e) {
                 // Try again recursively
                 con = getConnection();
             }
		 } else if (maxConnections == 0 || checkedOut < maxConnections) {
             con = newConnection();
         }
		 if (con != null) {
             checkedOut++;
         }
		 if (checkedOut == maxConnections) {
			 throw new TransactionMonitorMaximumNumberConnectionReachedException("The connection" +
			 		" you requested could not be opened");
		 }
		 
		 return con;
	 }
	 
	 /**
	  * This method creates a new connection in this Connection pool.
	  * 
	  * @return - The newly created connection.
	  */
	 private Connection newConnection() throws TransactionMonitorConnectionException {
		 
		 ConnectionObject connectionObject = new ConnectionObject(driverName, url, user, pwd);
		 return connectionObject.createConnection();
	 }
	 
	 /**
	  * This method notifies that a connection has been released, and that it should be
	  * added to the vector of free connections.
	  * 
	  * @param con - The sql connection to be released.
	  */
	 public synchronized void freeConnection(Connection con) {
         
		 // Put the connection at the end of the Vector
		 if (con != null) {
			freeConnections.addElement(con);
         	checkedOut--;
         	notifyAll();
		 }
     }
	 
	 /**
	  * This method creates a connection to a database and assign a timeout to it.
	  * 
	  * @param timeout - The desired timeout.
	  * @return - The newly created sql connection.
	  * 
	  * @throws TransactionMonitorException
	  */
	 public synchronized Connection getConnection(long timeout) throws TransactionMonitorException {
         
		 long startTime = new Date().getTime();
         Connection con;
         while ((con = getConnection()) == null) {
             try {
                 wait(timeout);
             }
             catch (InterruptedException ex) {
            	 System.err.println(ex.getMessage());
             }
             if ((new Date().getTime() - startTime) >= timeout) {
                 // Timeout has expired
                 return null;
             }
         }
         return con;
     }
	 
	 /**
	  * This metod closes all the connections registred in this connection pool.
	  * 
	  * @throws TransactionMonitorException
	  */
     public synchronized void release() throws TransactionMonitorException {
         
    	 Enumeration allConnections = freeConnections.elements();
         while (allConnections.hasMoreElements()) {
             Connection con = (Connection) allConnections.nextElement();
             try {
                 con.close();
             }
             catch (SQLException ex) {
            	 throw new TransactionMonitorSQLException("Cannot close the connection from" +
            			 "the ConnectionPool :" + ex.getMessage());
             }
         }
         freeConnections.removeAllElements();
     }
}

