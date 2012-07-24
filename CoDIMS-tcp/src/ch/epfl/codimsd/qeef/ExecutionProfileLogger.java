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

import org.apache.log4j.Logger;

import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.qeef.scheduler.G2NInfoNodes;
import ch.epfl.codimsd.qeef.util.Constants;

/**
 * The ExecutionProfileLogger class computes the rates on a set of nodes and write
 * those rates in a CatalogManager table.
 * 
 * @author Othman Tajmouati.
 */
public class ExecutionProfileLogger {

	/**
	 * The CatalogManager.
	 */
	private CatalogManager cm;
	
	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ExecutionProfileLogger.class);
	
	/**
	 * Node id used to reference the node where this ExecutionProfileLogger runs.
	 * This is the case when we log the execution profile from the Block2InstanceConverter operator.
	 */
	private int nodeID;
	
	/**
	 * Request id used to reference the request id executing in this remote node.
	 * This is the case when we log the execution profile from the Block2InstanceConverter operator.
	 */
	private long requestID;
	
	/**
	 * Represent the number of tuples consumed by the Block2InstanceConverter operator.
	 */
	private int nrTuples;

	/**
	 * Constructor called by the Block2InstanceConverter operator. In this
	 * loggin mode, we write in the Catalog when the Block2InstanceConverter
	 * operator returns a tuple from its buffer.
	 * 
	 * @param nodeID identifier of this remote node.
	 */
	public ExecutionProfileLogger() {
		
		BlackBoard bl = BlackBoard.getBlackBoard();

		try {

			cm = CatalogManager.getCatalogManager();

			if (bl.containsKey(Constants.THIS_NODE)) {
			
				// this.requestID = Long.parseLong((String) bl.get(Constants.REQUEST_ID));
				this.nodeID = Integer.parseInt((String)bl.get(Constants.THIS_NODE));
				this.requestID = Long.parseLong((String) bl.get(Constants.REQUEST_ID));
				
				cm.executeQueryString("INSERT INTO EXECUTIONPROFILE VALUES " +
						"(" + nodeID + "," + requestID +"," + 0 + "," + 0 + ")");
			}

		} catch (CatalogException ex) {
			ex.printStackTrace();
		}
	}

	public void setRequestID(long requestID) {
		this.requestID = requestID;
	}
	
	/**
	 * Log into the Catalog when the Block2InstanceConverter operator returns a tuple
	 * from its buffer.
	 */
	public void logByTuple() {
		
		nrTuples += 1;

		try {

			cm.executeQueryString("UPDATE EXECUTIONPROFILE SET nrTuples=" + nrTuples + " WHERE idNode=" + nodeID);
			
		} catch (CatalogException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Print the initial informations for the graphical interface in the
	 * intialExecutionProfile.
	 *
	 * @param infoNodes contain parameters to write.
	 */
	public void initialLog(G2NInfoNodes infoNodes) {

		try {
		
			cm.executeQueryString("DELETE FROM EXECUTIONPROFILE");
			cm.executeQueryString("DELETE FROM INITIALEXECUTIONPROFILE");
			
			for (int i = 0; i < infoNodes.getNumberOfNodes(); i++) {
			
				int idNode = i+1;
				cm.executeQueryString("INSERT INTO INITIALEXECUTIONPROFILE VALUES " +
					"(" + idNode + "," + this.requestID + "," + infoNodes.getNumberOfTuples()[i] + "" +
							"," + infoNodes.getProdRates()[i] + ")");
			}
			
		} catch (CatalogException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Prints the last log when we log from the Block2Instanceconverter operator.
	 */
	public void printLastLogByTuple() {
		
		try {

			cm.executeQueryString("UPDATE EXECUTIONPROFILE SET endFlag=" + 1 + " WHERE idNode= " + nodeID);
			cm.executeQueryString("DELETE FROM INITIALEXECUTIONPROFILE");
			
		} catch (CatalogException ex) {
			ex.printStackTrace(); 
		}
	}
}

