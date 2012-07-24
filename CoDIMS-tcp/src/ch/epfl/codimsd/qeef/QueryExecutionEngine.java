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

import ch.epfl.codimsd.exceptions.operator.OperatorInitializationException;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.initialization.InitializationException;
import ch.epfl.codimsd.exceptions.operator.OperatorException;
import ch.epfl.codimsd.qeef.relational.control.exchange.Receiver;
import ch.epfl.codimsd.qeef.relational.control.exchange.Sender;
import org.globus.examples.stubs.DQEEService_instance.BlackBoardParams;

/**
 * The QueryExecutionEngine manages the query execution. If CoDIMS is running in centralized mode, 
 * the QueryExecutionEngine executes a request; on the other hand if the distributed mode is used, 
 * the QueryExecutionEngine behaves as a bridge between the QueryManager and the remote node. In
 * distributed mode the QueryExecutionEngine is called by the DQEE class which defines CoDIMS
 * Web Service port types (interface methods).
 * 
 * Version 0.1
 * 
 * @author Othman Tajmouati
 * 
 * @date April 13, 2006
 */
public class QueryExecutionEngine extends QEEF {

	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(QueryExecutionEngine.class.getName());
	
	/**
	 * Default constructor.
	 */
	public QueryExecutionEngine() {
		super();
	}
	
	/**
	 * Initialize a remote node.
	 * 
	 * @param qepString the QEP for the remote node.
	 * @throws RemoteException
	 */
	public void initRemote(String qepString) throws RemoteException, OperatorInitializationException, PredicateEvaluatorException {
		
		try {
			// logger.debug("Initializing remote plan.");
			super.init(qepString);
		
		} catch (InitializationException ex) {
			 throw new RemoteException("Cannot initialize remote node : " + ex.getMessage());
		} catch (OperatorException ex) {
			 throw new RemoteException("Cannot create operator : " + ex.getMessage());
		} catch (CatalogException ex) {
			 throw new RemoteException("Cannot connect to catalog : " + ex.getMessage());
		}
	}

	/**
	 * Executes a request on a remote node.
	 * @return the result of the request in DataHandler format (soap attachement).
	 */
	public DataHandler executeRemote() throws RemoteException {
        
        try {
        	
        	// logger.debug("Executing request in remote node.");
        	
        	DataHandler dh = super.executeRemote();
        	return dh;
        
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
	}
	
	/**
	 * Returns some metadatas.
	 * @param remoteOp the id of the remote operator.
	 * @return the metadata in DataHanlder format (soap attachement).
	 * @throws RemoteException
	 */
	public DataHandler getMetadataRemote(int remoteOp) throws RemoteException {
		
		// If the node id = 0, we return the metadata of the final results. Those
		// are located in class QEEF.
		if (remoteOp == 0) {
			
			try {
			
				return super.getClientMetadata();
		    
			} catch (IOException exc) {
		            throw new RemoteException(exc.getMessage());
		    }
		}
		
		// If the remote operator id is not 0, we return its metadata.
		Sender op;
		try {
			op = (Sender) super.getPlanOperator(remoteOp);

			return op.getMetadataRemote();
		} catch (Exception exc){
			throw new RemoteException(exc.getMessage());
		}
	}
	
	/**
	 * Open a remote operator. This method is called for opening the remote senders.
	 * @param remoteOp the id of the remote operator.
	 * @throws RemoteException
	 */
	public void openRemote(int remoteOp) throws RemoteException {
		
		Sender op;
        
		try {
        	
                    // Retrieve the sender.
                    op = (Sender) super.getPlanOperator(remoteOp);
                    op.openRemote();

                } catch (Exception ex) {
                    throw new RemoteException(ex.getMessage());
                }
                }
	
	/**
	 * Get next remote results. It is called for getting senders results.
	 * @param remoteOp the id of the remote sender.
	 * @param blockSize the block size.
	 * @param waitTime the waiting time (required by Globus).
	 * @return tuples.
	 * @throws RemoteException
	 */
	public DataHandler getNextRemote(int remoteOp, int blockSize, long waitTime) throws RemoteException {
		
		Sender op;
	        
		try {
			
			op = (Sender) super.getPlanOperator(remoteOp);
			return op.getNextRemote(blockSize, waitTime);
			
		} catch (Exception ex) {
	       	throw new RemoteException(ex.getMessage());
		}
	}
	
	/**
	 * Close the remote operator.
	 * @param remoteOp the id of the remote operator.
	 * @throws RemoteException
	 */
	public void closeRemote(int remoteOp) throws RemoteException {
		 
		Sender op;
		
		try {

			op = (Sender) super.getPlanOperator(remoteOp);
			op.closeRemote();

		} catch (Exception exc) {
			throw new RemoteException(exc.getMessage());
		}
	}
	
	/**
	 * Copies a value to the remote BlackBoard.
	 * @param key
	 * @param value 
	 * @throws RemoteException
	 */
	public void copyToRemoteBlackBoard(String key, String value) throws RemoteException {
		 
		try {

			BlackBoard bl = BlackBoard.getBlackBoard();
			bl.put(key, value);

		} catch (Exception ex) {
			throw new RemoteException(ex.getMessage());
		}
	}
}

