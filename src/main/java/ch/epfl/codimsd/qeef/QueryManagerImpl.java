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

import ch.epfl.DerbyServerStarter;
import ch.epfl.codimsd.config.AppConfig;
import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.connection.TransactionMonitor;
import ch.epfl.codimsd.exceptions.CodimsException;
import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.initialization.InitializationException;
import ch.epfl.codimsd.exceptions.shutdown.ShutDownException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorException;
import ch.epfl.codimsd.helper.MemMon;
import ch.epfl.codimsd.query.RequestResult;
import ch.epfl.codimsd.query.Request;
import ch.epfl.codimsd.query.ResultSet;
import ch.epfl.codimsd.query.sparql.JSONOutput;
import ch.epfl.codimsd.query.sparql.ResultOutput;
import ch.epfl.codimsd.query.sparql.XMLOutput;
import ch.epfl.codimsd.qeef.sparql.QueryManipulation;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qep.QEPInfo;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version 1
 * 
 * QueryManagerImpl is the entry point of CoDIMS-D. In order to use CoDIMS for request execution,
 * the user should perform the following :
 * <li> Initialize CoDIMS by calling @see ch.epfl.codimsd.qeef.QueryManagerImpl#getQueryManagerImpl()
 * <li> Execute the request :
 *      - Synchronous request :  @see ch.epfl.codimsd.qeef.QueryManagerImpl#executeRequest(Request)
 *      - Asynchronous request : @see @see ch.epfl.codimsd.qeef.QueryManagerImpl#executeAsync(Request)
 * <li> Shutdown CoDIMS : @see @see ch.epfl.codimsd.qeef.QueryManagerImpl#shutdown()
 * 
 * @author Othman Tajmouati
 * 
 * @date April 13, 2006
 */

public class QueryManagerImpl extends Thread {

	final static Logger logger = LoggerFactory.getLogger(QueryManagerImpl.class);

	/**
	 * The TransactionMonitor
	 */
	private TransactionMonitor transactionMonitor;

	/**
	 * The CatalogManager object handles the communication with the catalog database.
	 */
	private CatalogManager catalogManager;

	/**
	 * The CatalogManager object handles the communication with the catalog database.
	 */
	@SuppressWarnings("unused")
	private DistributedManager distributedManager;

	/**
	 * The DiscoverQueryManagerImpl creates a singleton object (as defined in the Design Pattern).
	 */
	private static QueryManagerImpl ref = null;

	/**
	 * executionThread executes a request within another thread. It notifies the main thread
	 * when the results are ready for use.s
	 */
	private Thread executionThread;

	/**
	 * 
	 */
	private long initServiceTime;


	/**
	 * Private default constructor. It calls initializeService().
	 * 
	 * @throws CodimsException
	 */
	private QueryManagerImpl() throws CodimsException {
		initializeService();
	}

	/**
	 * The getQueryManagerImpl() creates a QueryManagerImpl singleton, 
	 * ensuring the unicity of the object during the system execution time (see Design Pattern
	 * for more informations {@link http://java.sun.com/blueprints/patterns/})
	 * 
	 * @return the singleton object
	 * 
	 * @throws CodimsException
	 */
	public static synchronized QueryManagerImpl getQueryManagerImpl() 
			throws CodimsException{

		// Call one single reference of the class.
		if (ref == null)
			ref = new QueryManagerImpl();

		return ref;
	}

	/**
	 * initializeService loads the system Configuration, initializes the BlackBoard
	 * and inserts the TransactionMonitor into the BlackBoard
	 * 
	 * @throws CodimsException
	 */
	private synchronized void initializeService() throws CodimsException {
		long startTime = System.currentTimeMillis();
		// Loads catalog connection parameters and initializes the CatalogManager
		SystemConfiguration.loadSystemConfiguration();

		try {

			// Start the Derby Network Server.
			String start = SystemConfiguration.getSystemConfigInfo(Constants.IS_DERBY_STARTED);

			if (start != null) {
				if (!start.equalsIgnoreCase("TRUE"))
					DerbyServerStarter.start();
			} else
				DerbyServerStarter.start();

			// Initializes the Catalog.
			catalogManager = CatalogManager.getCatalogManager();

			// Intializes the TransactionMonitor.
			transactionMonitor = TransactionMonitor.getTransactionMonitor();

			// Initialize the DistributedManager
			distributedManager = DistributedManager.getDistributedManager();

			this.initServiceTime = System.currentTimeMillis() - startTime; 

		} catch (CatalogException ex) {
			throw new InitializationException("Error in CatalogManager : " + ex.getMessage());
		} catch (TransactionMonitorException ex) {
			throw new InitializationException("InitializationException : " + ex.getMessage());
		} catch (Exception ex) {
			throw new InitializationException("Error in DerbyServerStarter : " + ex.getMessage());
		}
	}

	/**
	 * Shutdown the QueryManagerImpl class. It disconnects the connections with 
	 * the CatalogManager and the TransactionMonitor
	 * 
	 * @throws ShutDownException
	 */
	public synchronized void shutdown() throws CodimsException {

		try {

			// Close CoDIMS main component,
			catalogManager.closeCatalogManager();
			transactionMonitor.closeTransactionMonitor();

		} catch (TransactionMonitorException ex) {
			throw new CodimsException("Cannot close TransactionMonitor : " + ex.getMessage());
		}
	}

	/**
	 * This method allows the asynchronous execution of one request.
	 * 
	 * @param request
	 * @return id of the request.
	 * @throws CodimsException 
	 */
	public synchronized long executeAsync(Request request) throws CodimsException {

		// Set the request id. It corresponds to the current time.
		request.setRequestID(System.currentTimeMillis());

		// Create an ExecutionState for this request
		BlackBoard bl = BlackBoard.getBlackBoard();
		bl.put(Constants.EXEC_ASYNC+request.getRequestID(), new ExecutionState(request.getRequestID()));

		// Intialize the main component for the request execution.
		QueryManager queryManager = new QueryManager(request);

		// Create a new execution Thread and start it.
		executionThread = new Thread(queryManager, ch.epfl.codimsd.qeef.util.Constants.QueryManagerThread);
		executionThread.start();

		// Return the id of the request. It should be given for every communication.
		return request.getRequestID();
	}

	/**
	 * This method returns the execution state of one request.
	 * 
	 * @param requestId
	 * @return True if the execution is finished, false otherwise.
	 */
	public synchronized ExecutionState getExecutionState(long requestId) throws CodimsException {
		BlackBoard bl = BlackBoard.getBlackBoard();
		if (bl.containsKey(Constants.EXEC_ASYNC+requestId)) {
			return (ExecutionState) bl.get(Constants.EXEC_ASYNC+requestId);
		} else {
			return null;
		}
	}

	/**
	 * Returns the request result.
	 * 
	 * @param requestId
	 * @return the request result object or null if the execution is not finished.
	 */
	public synchronized RequestResult getRequestResult(long requestId) {
		BlackBoard bl = BlackBoard.getBlackBoard();

		if (bl.containsKey("REQUEST_ID_"+requestId))
			return (RequestResult) bl.get("REQUEST_ID_"+requestId);
		else
			return null;
	}

	public static void executeNTimes(QueryManager queryManager, int numberOfExecutions, String[] paramNames, String[] paramValues) throws Exception {
		// Defines some execution parameters
		long minTime = Long.MAX_VALUE;
		long maxTime = Long.MIN_VALUE;
		long sumTime = 0;

		for (int i=1; i <= numberOfExecutions; i++) {

			// Executes the request.
			queryManager.executeRequest(paramNames, paramValues);
			long executionTime = queryManager.getExecutionTime();

			// Access request results
			RequestResult finalRequestResult = queryManager.getRequestResult();

			// Shows results
			int results = 0;
			if (AppConfig.SHOW_RESULTS) {
				Writer out = new BufferedWriter(new FileWriter(AppConfig.RESULTS_FILE)); 
				results = QueryManipulation.printQueryResults(finalRequestResult, out);
				out.close();
			}

			System.out.println(MemMon.memoryInfo());
			MemMon.setMsg("");
			
			System.out.println("Execution #" + i + ": Time=" + executionTime + ", Results=" + results);
				

			if (executionTime < minTime) {
				minTime = executionTime;
			}
			if (executionTime > maxTime) {
				maxTime = executionTime;
			}
			sumTime += executionTime; 

		}
		double averageTime = sumTime / (double)numberOfExecutions;
		System.out.println("Results saved on file " + AppConfig.RESULTS_FILE);
		System.out.println(numberOfExecutions + " Execution(s): " + "Min=" + minTime + 
				", Avg=" + averageTime + ", Max=" + maxTime);
	}

	public static void executeWithFormattedOutput(QueryManager queryManager, String format) throws CodimsException {
		// Executes the request.
		queryManager.executeRequest();
		//long executionTime = queryManager.getExecutionTime();

		// Access request results
		RequestResult result = queryManager.getRequestResult();
		Metadata mt = result.getResultMetadata();
		ResultSet rs = result.getResultSet();

		if (format == null)
			format = "html";

		ResultOutput output = null;
		if (format.equals("html")) {
			output = new XMLOutput("/xml-to-html.xsl");

		} else if (format.equals("xml")) {
			output = new XMLOutput();

		} else if (format.equals("json")) {
			output = new JSONOutput();

		}

		output.format(System.out, rs, mt);
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String args[]) {
//		int requestType = Constants.REQUEST_TYPE_QEF_SPARQL_05A;
		int requestType = 31;
		int numberOfExecutions = 1;
		
//		String[] paramNames = new String[] {"?:p", "?:o"};
//		String[] paramValues = new String[] {"<http://purl.org/dc/elements/1.1/creator>", "\"J.K. Rowling\"" };
		
		// REQUEST_TYPE_QEF_SPARQL_05C
//		String[] paramNames = new String[] {"?:dt", "?:reg"};
//		String[] paramValues = new String[] {"2004", "\"Paqueta\"" };		

		// REQUEST_TYPE_DISEASES_DRUGS_Q1
//		String[] paramNames = new String[] {"?:dsname"};
//		String[] paramValues = new String[] { "\"vitamin\"" };		

		// REQUEST_TYPE_DISEASES_DRUGS_Q2
//		String[] paramNames = new String[] {"?:ds"};
//		String[] paramValues = new String[] { "\"asthma\"" };		
//		String[] paramValues = new String[] { "<http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseases/3149>" };		

		String[] paramNames = null;
		String[] paramValues = null;
		
		try {
			if (args.length > 0) {
				if (args[0].equals("list")) {
					List<QEPInfo> qeps = QEPInfo.getQEPInfo();
					for (QEPInfo qep : qeps) {
						System.out.println(qep);
					}
					return;
				}
				requestType = Integer.parseInt(args[0]);
			}
			if (args.length > 1) {
				numberOfExecutions = Integer.parseInt(args[1]); 
			}
			
		} catch (Exception e) {
			System.out.println("Args should be: [RequestType] [NumberOfExecutions]");
			System.exit(1);
		}
		
		try {
			long startTime = System.currentTimeMillis();
			System.out.println("QEP: " + requestType + " - Execution started.");

			// Initializes service
			// Get the QueryManagerImpl singleton.
			QueryManagerImpl queryManagerImpl = QueryManagerImpl.getQueryManagerImpl();
			System.out.println("Initialization service time: " + queryManagerImpl.initServiceTime);

			// Gets the query manager which is responsible for the query execution.
			QueryManager queryManager = QueryManagerRepository.getInstance().get(requestType);
			System.out.println("QEP creation time: " + queryManager.getQepCreationTime());

			executeNTimes(queryManager, numberOfExecutions, paramNames, paramValues);

			// Possible formats: html, xml, json
			//executeWithFormattedOutput(queryManager, "xml");

			// Close the QueryManagerImpl.
			queryManagerImpl.shutdown();
			System.out.println("Execution ended (" + (System.currentTimeMillis() - startTime)/1000.0 + "s)." );

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

}

