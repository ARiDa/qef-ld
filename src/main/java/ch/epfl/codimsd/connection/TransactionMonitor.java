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

import java.util.Hashtable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorConnectionException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorIDException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorInitializationException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorSQLException;
import ch.epfl.codimsd.qeef.discovery.datasource.dataSourceWsmoDB.Query;
import ch.epfl.codimsd.qeef.util.Constants;

/**
 * The TransactionMonitor manages the communication with the databases. It has a singleton reference in 
 * Codims.
 * 
 * The TransactionMonitor
 * <li> handles the transactions
 * <li> verfies the maximum number of connections for each database and the connection timeouts
 * <li> closes the connections when necessary
 * 
 * A user should call the TransactionMonitor in order to execute a query on a database. The user
 * first ask for a transaction id by calling the open() method, passing the IRI of the database.
 * Using this id, the user can execute sql queries and get the results. The method executeQuery returns
 * a ResultSet object, it should be used with SELECT statement. For other statements use the method
 * execute(); this naming follows the one defined in Derby API. When the connection is not needed anymore, 
 * it should be closed by calling the close() method, passing the id of the transaction. If the connection is not used
 * for a certain amount of time (timeout set in the catalog), it is closed automatically by the
 * TransactionMonitor.
 * 
 * Example :
 * 
 * TransactionMonitor tm = TransactionMonitor.getTransactionMonitor();
 * int transactionID = tm.open(myIRI);
 * Query query = new Query;
 * query.setStringQuery("INSERT INTO myDB Values (1,1)");
 * tm.execute(transactionID,qusery);
 * query.setStringQuery("SELECT * FROM myDB);
 * ResultSet rset = tm.executeQuery(transactionID,query);
 * tm.close(transactionID);
 * 
 * @author Othman Tajmouati
 */
public class TransactionMonitor implements Runnable {

	/**
	 * The singleton reference.
	 */
	private static TransactionMonitor ref;
	
	/**
	 * The connectionPoolManager managing connections to ConnectionPool objects.
	 */
	private static ConnectionPoolManager connectionPoolManager;
	
	/**
	 * transactionList hashtable contains the transaction objects.
	 */
	private Hashtable<Integer, TransactionObject> transactionList;
	
	/**
	 * This thread verifies if a connection is unused during a certain amount of 
	 * time maxBusyConnectionTime(set in the catalog in the initialConfig table).
	 */
	private Thread T;
	
	/**
	 * flag used for the initialization of the singleton.
	 */
	private boolean firstOpenCall = false;
	
	/**
	 * A connection cannot be sleeping more than maxBusyConnectionTime.
	 */
	private long maxBusyConnectionTime;
	
	/**
	 * The sleeping time for the thread T.
	 */
	private long threadSleepTime;
	
	/**
	 * Parameter needed for closing the TransactionMonitor.
	 */
	private boolean releaseMsg = false;
	
	/**
	 * Log4j logger.
	 */
	private static Logger logger = Logger.getLogger(TransactionMonitor.class.getName());
	
	/**
	 * Private constructor. It retrieves initial parameters from the catalog (as the 
	 * max busy connection time, thread sleep time), initializes the ConnectionPoolManager
	 * and creates the thread that releases non-used connections.
	 *   
	 * @throws TransactionMonitorInitializationException
	 */
	private TransactionMonitor() throws TransactionMonitorException {
		
		try {
			
			// Get the max connection time from the catalog (intialConfig table)
			CatalogManager catalogManager = CatalogManager.getCatalogManager();
			String maxCon = (String) catalogManager.getSingleObject("initialConfig",
					"Value","KeyName='" + Constants.MaxBusyConnectionTimeKey + "'");

			// Get the sleep time of the closed connection checker thread
			String sleepTime = (String) catalogManager.getSingleObject("initialConfig",
					"Value","KeyName='" + Constants.ThreadSleepTimeKey + "'");
			
			maxBusyConnectionTime = Integer.parseInt(maxCon.trim());
			threadSleepTime = Integer.parseInt(sleepTime.trim());
			
		} catch (CatalogException ex) {
			throw new TransactionMonitorException("TransactionMonitor " +
					"cannot get initial parameters (check connection parameters in IRICatalog) " + ex.getMessage());
		}
		
		// Get the singleton reference of the ConnectionPoolManager
		connectionPoolManager = ConnectionPoolManager.getConnectionPoolManager();
		
		// Initializes the hashtable that contain transaction objects
		transactionList = new Hashtable<Integer, TransactionObject>();
		
		// Initializes the thread that checks non-used connections.
		if (firstOpenCall == false) {
			
			firstOpenCall = true;
			T = new Thread(this, Constants.timeOutThread);
			T.start();
		}
	}

	/**
	 * This method returns the singleton reference of the TransactionMonitor.
	 * 
	 * @return - The singleton reference of the TransactionMonitor.
	 * 
	 * @throws TransactionMonitorException
	 */
	public static synchronized TransactionMonitor getTransactionMonitor() 
		throws TransactionMonitorException{
		
		if (ref == null)
			ref = new TransactionMonitor();
		
		return ref;
	}
	
	/**
	 * This method opens a new transaction. It creates a new TransactionObject, puts it in the
	 * transaction object list and returns a transaction id to the caller.
	 *  
	 * @param IRI - a database IRI.
	 * @return - a newly created transaction id, to be used in further communications with the
	 * TransactionMonitor.
	 * @throws TransactionMonitorException
	 */
	public synchronized int open(String IRI) throws TransactionMonitorException {
		
		// Get an open corresponding to this IRI from the ConnectionPoolManager
		Connection con = connectionPoolManager.getConnection(IRI);
		
		if (con != null) {
			
			try {
				
				// Create a new sql statement for this transaction
				Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
						ResultSet.CONCUR_UPDATABLE);
				
				// Create a new TransactionObject and put it in the transactionList hashtable
				TransactionObject transactionObject = new TransactionObject(con, IRI, 
						System.currentTimeMillis(), stmt);
				transactionList.put(transactionList.size()+1,transactionObject);

				// Return a new transaction id
				return (transactionList.size());
			
			} catch (SQLException ex){
				throw new TransactionMonitorSQLException("SQLException : " + ex.getMessage());
			}
		} else 
			throw new TransactionMonitorConnectionException("The connection you " +
					"requested could not be opened");
	}
	/**
	 * This method returns a database metadata.
	 * 
	 * @return a sql.DatabaseMetaData object
	 * @throws CatalogException
	 */
	public synchronized DatabaseMetaData getDatabaseMetadata(String IRI) 
	   throws TransactionMonitorException {
		
		DatabaseMetaData dbMData=null;	
		try {
			Connection con = connectionPoolManager.getConnection(IRI);
			dbMData= con.getMetaData();
		}
		catch (SQLException sqle){
			throw new TransactionMonitorException ("Error getting Catalog Metadata"+ sqle.getMessage());
		}
		return dbMData;
	}
	
	/**
	 * The executeQuery method executes an sql query and returns a ResultSet object. It should be
	 * used for executing SELECT statement.
	 * 
	 * @param id - The id of the transaction.
	 * @param query - a Query object containing the sql string query.
	 * @return - The sql ResultSet object.
	 * @throws TransactionMonitorException
	 */ 
	public synchronized ResultSet executeQuery(int id, Query query) 
			throws TransactionMonitorException {
		
		// The id of a transaction could not be null
		if (id == 0)
			throw new TransactionMonitorIDException("Transaction id null");
		
		// Get the transaction from the transaction list hashtable
		ResultSet rset = null;
		TransactionObject transactionObject = (TransactionObject) transactionList.get(new Integer(id));
		Connection con = transactionObject.getConnection();
		
		if (con == null)
			throw new TransactionMonitorConnectionException("Your connection is closed or " +
					"has been reseted");
		
		try {	
			
			// AutoCommit mode is set to false
			con.setAutoCommit(false);
			
			// If the query doesn't contain an sql statement, we call the executeQuery method
			// of the Repository class.
			if (query.getStringRequest() == null) {
				
				Repository rep = new Repository(query,con);
				rep.excecuteQuery();
				rset = rep.getResultSet();
			
			} else if (query.getStringRequest().equalsIgnoreCase(Constants.COMMIT)) {
				
				// Commit the set of previously executed queries
				con.commit();
				
			} else if (query.getStringRequest().equalsIgnoreCase(Constants.ROLLBACK)) {
				
				// Rollback the queries
				con.rollback();
				
			} else {

				// Execute the sql query and update the transaction list hashtable
				Statement stmt = transactionObject.getSQLStatement();
				rset = stmt.executeQuery (query.getStringRequest());
				transactionObject.setSQLStatement(stmt);
				transactionObject.setTimeStamp(System.currentTimeMillis());
				transactionObject.setConnection(con);
				transactionList.put(new Integer(id), transactionObject);
			}
		
		} catch (SQLException ex) {

			throw new TransactionMonitorSQLException("SQLException : " + ex.getMessage());
		}

		return rset;
	}
	
	/**
	 * The execute method executes an sql query. It should be used for executing statement different
	 * than SELECT (ex : INSERT, UPDATE, DELETE, DROP).
	 * 
	 * @param id - The id of the transaction.
	 * @param query - The Query object containing the sql string query.
	 * 
	 * @throws TransactionMonitorException
	 */
	public synchronized void execute(int id, Query query) throws TransactionMonitorException {
		
		// The id of a transaction could not be null
		if (id == 0)
			throw new TransactionMonitorIDException("Transaction id null");

		// Get the transaction from the transaction list hashtable
		TransactionObject transactionObject = (TransactionObject) transactionList.get(new Integer(id));
		Connection con = transactionObject.getConnection();
		
		if (con == null)
			throw new TransactionMonitorConnectionException("Your connection is closed " +
					"or has been reseted");
		
		try {	
			
			// AutoCommit mode is set to false
			con.setAutoCommit(false);
			
			// If the query doesn't contain an sql statement, we call the executeQuery method
			// of the Repository class.
			if (query.getStringRequest() == null) {
				
				Repository rep = new Repository(query,con);
				rep.excecuteQuery();
				
			} else if (query.getStringRequest().equalsIgnoreCase(Constants.COMMIT)) {
				
				// Commit the set of previously executed queries
				con.commit();
			
			} else if (query.getStringRequest().equalsIgnoreCase(Constants.ROLLBACK)) {
				
				// Rollback the queries
				con.rollback();
				
			} else {

				// Execute the sql query and update the transaction list hashtable
				Statement stmt = transactionObject.getSQLStatement();
				stmt.execute(query.getStringRequest());
				transactionObject.setSQLStatement(stmt);
				transactionObject.setTimeStamp(System.currentTimeMillis());
				transactionObject.setConnection(con);
				transactionList.put(new Integer(id), transactionObject);
			}
		
		} catch (SQLException ex) {

			throw new TransactionMonitorSQLException("SQLException : " + ex.getMessage());
		}
	}

	
	/**
	 * This method closes a transaction id given a transaction id.
	 * 
	 * @param id - The id of the transaction to close.
	 * @throws TransactionMonitorException
	 */
	public synchronized void close(int id) throws TransactionMonitorException {
		
		try {
			
			// Get the transaction from the transaction list hashtable
			TransactionObject transactionObject = transactionList.get(new Integer(id));
			Connection con = transactionObject.getConnection();
			
			if (con != null) {
				
				// Commit the connection
				con.commit();
				
				// Close the connection and release it in the ConnectionPoolManager
				con.close();
				transactionObject.closeSQLStatement();
				connectionPoolManager.freeConnection(con, transactionObject.getIRI());
				
				// Set the connection to null and update the transaction list hashtable
				transactionObject.setConnection(null);
				transactionList.put(new Integer(id), transactionObject);
			}
			
		} catch (SQLException ex) {
			throw new TransactionMonitorSQLException("SQLException : " + ex.getMessage());
		}
	}
	
	/**
	 * This method closes the TransactionMonitor. It closes all the opened connection
	 * in the ConnecitonPoolManager and closes the checker thread.
	 * 
	 * @throws TransactionMonitorException
	 */
	public synchronized void closeTransactionMonitor() throws TransactionMonitorException {
		
		releaseMsg = true;
		if (T.isAlive()) {
			T.interrupt();
		}
		checkTransactionTimeOut();
		connectionPoolManager.release();
	}
	
	/**
	 * Returns the transactionList hashtable.
	 * @return - The transactionList hashtable.
	 */
	private synchronized Hashtable<Integer, TransactionObject> getTransactionList() {
		return transactionList;
	}
	
	/**
	 * This method is called for starting the thread that checks non-used connections
	 * in the transactionList hashtable. At a regular time interval it calls the
	 * checkTransactionTimeOut method.
	 */
	@SuppressWarnings("static-access")
	public void run() {
		
		while(releaseMsg == false)
		{
			try {
			
				// Check non-used connections
				checkTransactionTimeOut();
				
				if (releaseMsg == false)
					T.sleep(threadSleepTime);
			
			} catch (InterruptedException ex1) {} 
			catch (TransactionMonitorException ex2) {}
		}
	}
	
	/**
	 * This method is used for checking non-used connections.
	 * 
	 * @throws TransactionMonitorException
	 */
	private void checkTransactionTimeOut() throws TransactionMonitorException {
		
		Hashtable<Integer, TransactionObject> transactionList = getTransactionList();
		
		// If there exists transactions in the transactionList hashtable check non-used connections
		if (transactionList.size() != 0) {
			
			for (int i = 1; i <= transactionList.size(); i++) {
				
				TransactionObject transactionObject = 
					(TransactionObject)transactionList.get(new Integer(i));
				
				if (transactionObject.getConnection() != null) {
					
					// the transaction should not be opened and not used for a time greated than
					// maxBusyConnectionTime. Id it's the case, close the connection.
					if (System.currentTimeMillis() - transactionObject.getTimeStamp() > 
							maxBusyConnectionTime) {
						
						connectionPoolManager.freeConnection(transactionObject.getConnection(), 
							transactionObject.getIRI());

						transactionObject.setConnection(null);
						transactionObject.closeSQLStatement();
						transactionList.put(i, transactionObject);
				
						logger.info("Enters the thread and delete the " +
								"connection number " + i + " : ");
						logger.info(+ (System.currentTimeMillis() - transactionObject.getTimeStamp()) 
								+ " > " + maxBusyConnectionTime);
					}
				}
			}
		}
	}
}

