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

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.codimsd.exceptions.CodimsException;
import ch.epfl.codimsd.qep.QEP;
import ch.epfl.codimsd.query.QueryComponent;
import ch.epfl.codimsd.query.Request;
import ch.epfl.codimsd.query.RequestResult;

/**
 * Version 1
 * 
 * QueryManager is the base class managing the execution of a request. It is called by
 * QueryManagerImpl in its executeRequest or execAsnyc methods.
 * 
 * @author Othman Tajmouati
 * 
 * @date April 13, 2006
 */
public class QueryManager extends Thread {
	
	final static Logger logger = LoggerFactory.getLogger(QueryManager.class);
	
	/**
	 * The request to execute.
	 */
	private Request request;
	
	/**
	 * RequestResult containing the results of this request.
	 */
	private RequestResult requestResult;
	
	/**
	 * QEP
	 */
	private QEP qep;

	/**
	 * 
	 */
	private long qepCreationTime;
	
	/**
	 * 
	 */
	private long executionTime;
	
	/**
	 * Named parameters
	 */
	private Set<String> namedParams;
	
	
	/**
	 * Default constructor
	 */
	public QueryManager(Request request) throws CodimsException {
		this.request = request;
		createQEP();
		
		// Fills named parameters from Operators
		this.namedParams = new HashSet<String>();
		Map<String, Object> params = new Hashtable<String, Object>();
		params.put("namedParams", this.namedParams);

		Operator rootOp = this.qep.getConcreteopList().get(1);
		
		// Finds which named params are being used.
		// Changes this.namedParams
		rootOp.sendMessage(params);
		
		if (this.namedParams.size() == 0) {
			this.namedParams = null;
		}
		logger.info("QEP: {} - Named Parameters: {}", this.request.getRequestID(), this.namedParams);
	}

	/**
	 * 
	 * @return
	 */
	private synchronized void createQEP() throws CodimsException {
		PerformanceAnalyzer pa = PerformanceAnalyzer.getPerformanceAnalyzer();
		
		long buildingPlanAndComponentTime = System.currentTimeMillis();
		
		// Call the QPCompFactory, and initialize the required component.
		QPCompFactory qpCompFactory = new QPCompFactory(this.request);
		QueryComponent queryComponent = qpCompFactory.getQueryComponent();
		
		// Create and optimize the QEP.
		PlanManager planManager = queryComponent.getPlanManager();
		this.qep = (QEP) planManager.instantiatePlan(this.request);

		long elapsedTime = System.currentTimeMillis()-buildingPlanAndComponentTime;
		logger.info("Plan instantiation time: " + elapsedTime);
		pa.log("Building components and QEP plan in client side  : ", elapsedTime);
		this.qepCreationTime = elapsedTime;
	}

	public synchronized void executeRequest() throws CodimsException {
		executeRequest(null, null);
	}
	
	
	/**
	 * executRequest computes the request sent by the QueryManagerImpl. The query execution
	 * performs the following actions :
	 * <li> Calls the QPCompFactory which creates (according the request type) 
	 * the corresponding object instances for the system
	 * <li> Encapsulate these objects in a QueryComponent
	 * <li> Calls the DiscoveryPlanManager which creates the query execution plan (QEP)
	 * <li> Executes the QEP in distributed or centralized mode.
	 * <li> Returns the results in a RequestResult object
	 * 
	 * @param request the request (containing the requestType and the requestParameter fields)
	 * @return requestResult the result of execution
	 * 
	 * @throws RemoteException
	 * @throws CodimsException
	 */
	public synchronized void executeRequest(String[] paramNames, String[] paramValues) throws CodimsException {
		
		if (this.namedParams != null && (paramNames == null || paramValues == null)) {
			throw new CodimsException("Parameters names and values must be supplied: " + namedParams);
		}
		
		PerformanceAnalyzer pa = PerformanceAnalyzer.getPerformanceAnalyzer();

		// If there exist remote nodes, we execute the request in distributed mode.
		logger.debug("Executing the request.");
		long beginTime = System.currentTimeMillis();
		if (this.qep.existRemote) {
			executeRemoteRequest();
		} else {
			executeLocalRequest(paramNames, paramValues);
		}

		// Log performance times and create the log file.
		long elapsedTime = System.currentTimeMillis()-beginTime; 
		logger.info("Execution time: " + elapsedTime);
		pa.log("Total Execution time for Request (id " + this.request.getRequestID() + ")  : ", elapsedTime);
		pa.finalizeLogging();

		logger.debug("Execution finished.");
		this.executionTime = elapsedTime;
	}

	/**
	 * Call requestResult in this Thread.
	 */
	public void run() {
		
		try {
			executeRequest();
			this.interrupt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return the RequestResult.
	 */
	public RequestResult getRequestResult() {
		return requestResult;
	}
	
	private void executeRemoteRequest() throws CodimsException {
		
		requestResult = null;
		// DQEEPortType qeLocalNode = null;
		
		try {	
		
			// Check if the distributed environment is already created. This is usually
			// the case if we receive multiple requests
			logger.debug("Building remote environment and initializing web services.");
			
			long beginBuildingDistEnvironmentTime = System.currentTimeMillis();
			// DistributedManager distributedManager = DistributedManager.getDistributedManager();
			// DQEEPortType qeLocalNode = distributedManager.buildEnvironement(request);
			PerformanceAnalyzer pa = PerformanceAnalyzer.getPerformanceAnalyzer();
			pa.log("Building distributed environment : ",
					(System.currentTimeMillis()-beginBuildingDistEnvironmentTime));

			// Execute the request.
//			long begineExecutionTime = System.currentTimeMillis();
//			DataHandler dhBlock = qeLocalNode.executeRemote(new ExecuteRemoteRequest());

			// Get the remote metadata in order to recover the result.
			//DataHandler dh = qeLocalNode.getMetadataRemote(0);
			//Object o = (new ObjectInputStream(dh.getInputStream())).readObject();
			//Metadata[] metadata = (Metadata[])o;
                        // InputStream is = dhBlock.getInputStream();

                        // Write the block containing the result tuples.
                     /*   Object oBlock = (new ObjectInputStream(dhBlock.getInputStream())).readObject();
                        Block block = (Block)oBlock;

			// Construct this RequestResult object.
			ResultSet result = new ResultSet();
			requestResult = new RequestResult();
			result.open();
			
			int blockSize = block.size();
			for (int i = 0; i < blockSize; i++) {
				result.add((Tuple)block.get());
			}
			result.close();
			
                        requestResult.setResultSet(result);
                        short resultCode = 0;
                        requestResult.setResultCode(resultCode);
                        requestResult.setMetadata(metadata[0]);
                        long elapsedTime = System.currentTimeMillis() - begineExecutionTime;
                        requestResult.setElapsedTime(elapsedTime);
	        
                        // Put the requestResult into the BlackBoard.
			bl.put("REQUEST_ID_"+request.getRequestID()+"", requestResult);
			
			// Put the requestResult in the BlackBoard for the async execution
			if (bl.containsKey(Constants.EXEC_ASYNC + request.getRequestID())) {

				ExecutionState executionState = (ExecutionState) bl.get(Constants.EXEC_ASYNC + request.getRequestID());
				executionState.setRequestResult(requestResult);
				executionState.close();
				bl.put(Constants.EXEC_ASYNC, executionState);
			}*/

//		} catch (RemoteException ex) {
//			throw new CodimsException("Exception in remote node : " + ex.getMessage());
//		} catch (DistributedException ex) {
//			throw new CodimsException("Error in DistributedManager : " + ex.getMessage());
		} catch (Exception ex) {
			throw new CodimsException("Cannot recover RequestResult from the local Web Service : " + ex.getMessage());
		}
	}
	
	/**
	 * 
	 * @throws CodimsException
	 */
	public void executeLocalRequest(String[] paramNames, String[] paramValues) throws CodimsException {

		// Execute the request in centralized mode.
		QueryExecutionEngine queryExecutionEngine = new QueryExecutionEngine();
		queryExecutionEngine.setPlanOperator(this.qep.getConcreteopList());
		
		try {
			long beginExecutionTime = System.currentTimeMillis();
			requestResult = queryExecutionEngine.execute(paramNames, paramValues);
			requestResult.setElapsedTime(System.currentTimeMillis() - beginExecutionTime);
		} catch (Exception ex) {
			throw new CodimsException(ex.getMessage());
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public long getExecutionTime() {
		return executionTime;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getQepCreationTime() {
		return qepCreationTime;
	}

	/**
	 * 
	 * @return
	 */
	public Set<String> getNamedParams() {
		return namedParams;
	}
	
}

