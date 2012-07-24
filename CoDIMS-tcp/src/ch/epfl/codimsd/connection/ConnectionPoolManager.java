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

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Enumeration;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorException;

/**
 * The ConnectionPoolManager manages a set of Connection pools, where each connection pool
 * corresponds to a set of connections to the same database.
 * 
 * @author Othman Tajmouati.
 *
 */
public class ConnectionPoolManager {

	/**
	 * Hashtable of connection pools.
	 */
	private Hashtable<String, ConnectionPool> connectionPoolList;
	
	/**
	 * Singleton object of the ConnectionPoolManager class.
	 */
	private static ConnectionPoolManager ref;
 
	/**
	 * Defalut constructor. It initializes the connectionPoolList hashtable.
	 */
	private ConnectionPoolManager() {
		connectionPoolList = new Hashtable<String, ConnectionPool>();
	}

	/**
	 * Returns an sql connection if it's available, or create a new one otherwise.
	 * 
	 * @param IRI - The IRI of the database.
	 * @return - The sql connection.
	 * 
	 * @throws TransactionMonitorException
	 */
	public synchronized Connection getConnection(String IRI) throws TransactionMonitorException {
	
		Connection con;
		ConnectionPool connectionPool = connectionPoolList.get(IRI);
	
		if (connectionPool == null) {
			
			try {
				connectionPool = new ConnectionPool(IRI);
			} catch (CatalogException ex) {
				throw new TransactionMonitorException("Error connecting to Catalog : " + ex.getMessage());
			}
			
			connectionPoolList.put(IRI, connectionPool);
			con = connectionPool.getConnection();
		} else {
			connectionPool = connectionPoolList.get(IRI);
			con = connectionPool.getConnection();
		}
	
		return con;
	}
	
	/**
	 * Creates the singleton reference of the ConnectionPoolManager or retunrs the reference
	 * id the singleton is already created.
	 * 
	 * @return - The singleton reference of the ConnectionPoolManager class.
	 */
	public static synchronized ConnectionPoolManager getConnectionPoolManager() {
		
		if (ref == null)
			ref = new ConnectionPoolManager();
		return ref;
	}

	/**
	 * This method release an sql connection.
	 * 
	 * @param con - The connection to free.
	 * @param IRI - The IRI of the database.
	 */
	public synchronized void freeConnection(Connection con, String IRI) {
		
		ConnectionPool connectionPool = connectionPoolList.get(IRI);
		connectionPool.freeConnection(con);
	}
	
	/**
	 * Release all the connections in every registred connection pool.
	 * 
	 * @throws TransactionMonitorException
	 */
	public synchronized void release() throws TransactionMonitorException {
		
		if (connectionPoolList.size() != 0) {
			
			Enumeration<String> IRI = connectionPoolList.keys();
			while (IRI.hasMoreElements()) {
				ConnectionPool connectionPool = connectionPoolList.get(IRI.nextElement());
				connectionPool.release();
			}
		}
	}
	
	/**
	 * This method prevents from cloning the singleton reference.
	 */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}

