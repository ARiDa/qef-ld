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
package ch.epfl.codimsd.qeef.discovery;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.initialization.QEPInitializationException;
import ch.epfl.codimsd.exceptions.initialization.RequestTypeNotFoundException;
import ch.epfl.codimsd.exceptions.optimizer.OptimizerException;
import ch.epfl.codimsd.qeef.ExecutionProfileLogger;
import ch.epfl.codimsd.qeef.Optimizer;
import ch.epfl.codimsd.qeef.SystemConfiguration;
import ch.epfl.codimsd.qeef.operator.control.FragmentInfo;
import ch.epfl.codimsd.qeef.scheduler.G2N;
import ch.epfl.codimsd.qeef.scheduler.G2NInfoNodes;
import ch.epfl.codimsd.qeef.scheduler.G2NNode;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qep.OpNode;
import ch.epfl.codimsd.qep.QEP;
import ch.epfl.codimsd.qep.QEPFactory;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.query.Request;

/**
 * 
 * The DiscoveryOptimizer optimizes a query execution plan (QEP). It performs the following operations :
 * <li> Call G2N algorithm;
 * <li> If G2N returns a null number of remote nodes, the QEP is not optimized;
 * <li> If there exist remote nodes, the optimizer transform the initial QEP to a more complex one that will
 * support distributed communications. The optimizer chooses the operator to ditribute from the initial QEP
 * and builds a list of optimized remote QEP containing the distributed operator(s).
 * 
 * @author Othman Tajmouati
 */
public class DiscoveryOptimizer extends Optimizer {
	
	/**
	 * Inital query execution plan. This is the plan without modifications,
	 * without distribution's modifications.
	 */
	private QEP qepInitial = null;
	
	/**
	 * List of remote plans.
	 */
	private LinkedList<String> qepList = null;
	
	/**
	 * Structure storing the remote nodes.
	 */
	private G2NInfoNodes infoNodes = null;
	
	/**
	 * Identifiers of the senders.
	 */
	private int[] senderIDs;
	
	/**
	 * Identifiers of the receivers.
	 */
	private int[] receiverIDs;
	
	/**
	 * Identifier of the operator that is located just under
	 * the last distributed operator in the plan.
	 */
	private int idDistributedBottomCut;
	
	/**
	 * Identifier of the operator located just above the first 
	 * distributed operator in the plan.
	 */
	private int idDistributedTopCut;
	
	/**
	 * Identifier of the Block2Instance operator.
	 */
	private int idB2I;
	
	/**
	 * Temporary hashtable used to store operators.
	 */
	private Hashtable<String, OpNode> temporaryHashtable;
	
	/**
	 * Array of distributed operators ids.
	 */
	private int[] distributedOperators;
	
	/**
	 * The initial distributed query execution plan.
	 * This plan is modified in order to be executed on a remote node.
	 * Basically, distributed operators are inserted into it.
	 */
	private QEP qepDistributedRemote;
	
	/**
	 * This is a plan used to convert the initial local plan to
	 * a plan that will accept distribution (addition if operators Merge,
	 * Split, B2I, I2B, receivers, senders).
	 */
	private QEP qepDistributedLocal;
	
	/**
	 * Log4j logger.
	 */
	private static Logger logger = Logger.getLogger(DiscoveryOptimizer.class.getName());
	
	/**
	 * The request to execute.
	 */
	private Request request;
	
	/**
	 * Default constructor.
	 */
	public DiscoveryOptimizer(Request request) {
		this.request = request;
	}
	
	/**
	 * Returns the initial QEP. The initial QEP could be modified or not.
	 * 
	 * @return the initial plan
	 */
	public QEP getLocalPlan() {
		return qepInitial;
	}
	
	/**
	 * The optimize method optimizes the initial QEP. It fills the QEP with a QEP fragment corresponding
	 * to the operators that we should add in order to allow distributed communications (split, merge, sender, 
	 * receiver operators).
	 * 
	 * @param qepIntial - The intial QEP to optimize
	 * @return true if the QEP has been optimized, false otherwise (no remote nodes)
	 */
	public boolean optimize(QEP qepInitial) throws OptimizerException {

		// Set the initial plan.
		this.qepInitial = qepInitial;

		try {

			// Call G2N in order to know if the plan is to be executed
			// on remote nodes.
			callG2N();

		} catch (CatalogException e) {
			throw new OptimizerException("CatalogException :" + e.getMessage());
		}

		// G2N will fill this structure in case we have a distributed request.
		// If the number of nodes are null, then the execution of the request will be done
		// in the local node.
		if (infoNodes.getNumberOfNodes() == 0) {
			logger.debug("No remote execution for this request.");
			return false;
		}

		try {

			// If the execution is parallelized, load needed templates (distributed local, 
			// and distributed remote).
			loadQEPTemplates();
			
		} catch (QEPInitializationException ex) {
			throw new OptimizerException("QEP templates error :" + ex.getMessage());
		}
		
		// Intialize the list that will store remote plans.
		qepList = new LinkedList<String>();
		
		// Update the plans.
		updateQEPInitial(qepInitial);
		updateQEPDistributedRemote();
		
		// Put node infos and remote qeps into the blakcBoard.
		BlackBoard bl = BlackBoard.getBlackBoard();
		bl.put(Constants.infoNodes, infoNodes);
		bl.put(Constants.qepRemoteList, qepList);

		// There's a distributed execution.
		return true;
	}
	
	/**
	 * This method updates the initial QEP with the Distributed fragment.
	 * 
	 * @param qepInitial - the intial QEP
	 */
	private void updateQEPInitial(QEP qepInitial) {
		
		int numberOfDistributedOperators = distributedOperators.length;
		int initalQEPsize = qepInitial.getOperatorList().size();
		
		// Temporary hashtable where to put the distributed fragment
		temporaryHashtable = new Hashtable<String, OpNode>();
		
		// Remove the distributed fragment from the initial QEP and store it in the temp Hashtable
		int i = 0, id = 0;
		idDistributedTopCut = distributedOperators[i];
		
		for (i = 0; i < numberOfDistributedOperators; i++) {
			id = distributedOperators[i];
			temporaryHashtable.put((i+1)+"",(OpNode)qepInitial.getOperatorList().get(id+""));
			qepInitial.getOperatorList().remove(id+"");
		}
		
		idDistributedBottomCut = id + 1 - distributedOperators.length;
		int idFinal = id;
		
		// Change ids and producer ids from BottomCutNode till the end
		for (int j = idFinal+1; j <= initalQEPsize; j++ ) {
			OpNode tempNode = (OpNode)qepInitial.getOperatorList().get(j+"");
			
			// if (tempNode.getOpName().equalsIgnoreCase(Constants.ScanDiscoveryOperator) == false) {
			if (tempNode.getType().equalsIgnoreCase("SCAN") == false) {
				int[] tempIDs = tempNode.getProducerIDs();
				for (int k = 0; k < tempIDs.length; k++) {
					tempIDs[k] -= numberOfDistributedOperators;
				}
				tempNode.setProducerIDs(tempIDs);
			}
			
			tempNode.setOpID(j-numberOfDistributedOperators);
			qepInitial.getOperatorList().put((j-numberOfDistributedOperators)+"",tempNode);
			qepInitial.getOperatorList().remove(j+"");
		}
		
		// update the QEP Ditributed Local here in order to fill topCutNode operator with I2B id
		int numberLocalOperators = qepInitial.getOperatorList().size();
		updateQEPDistributedLocal(numberLocalOperators, numberOfDistributedOperators);
		
		// Set element above topCutNode here if exist (last anchor!)
		if (idDistributedTopCut > 1) {
			OpNode topCutNode = (OpNode)qepInitial.getOperatorList().get((idDistributedTopCut-1)+"");
			int[] _id = new int[1];
			_id[0]=idB2I;
			topCutNode.setProducerIDs(_id);
			qepInitial.getOperatorList().put((idDistributedTopCut-1)+"",topCutNode);
		}
		
		// Merge the two local hashtables
		for (i = numberLocalOperators+1; i <= qepDistributedLocal.getOperatorList().size()+numberLocalOperators; i++) {
			OpNode tempNode = (OpNode) qepDistributedLocal.getOperatorList().get(i+"");
			qepInitial.getOperatorList().put(i+"", tempNode);
		}
		
		String qepString = QEPFactory.generateString(qepInitial, "Initial");
		// logger.debug(qepString);
//		QEPFactory.logFile("qepLocal" + "_Req" + request.getRequestID() 
//				+ "_" + + System.currentTimeMillis() + ".xml", qepString);
		qepList.add(qepString);
	}

	/**
	 * This method updates the distributed fragment
	 * 
	 * @param numberOfLocalOperators The number of local operators in the QEP initial (the operators to
	 * distribute are not taken into account).
	 * 
	 * @param numberOfDistributedOperators The number of distributed operators in the intial QEP
	 */
	private void updateQEPDistributedLocal(int numberOfLocalOperators, int numberOfDistributedOperators) {
		
		int idSplit = 0;
		int idMerge = 0;
		
		// Update QEP Disctributed Local with new operatos ids and producers ids
		for (int i = qepDistributedLocal.getOperatorList().size(); i > 0; i--) {
			
			OpNode tempOpNode = (OpNode)qepDistributedLocal.getOperatorList().get(i+"");
			
			if (tempOpNode.getOpName().equalsIgnoreCase(Constants.Block2InstanceOperator) == true) {
				
				idB2I = i + numberOfLocalOperators;

			} else if (tempOpNode.getOpName().equalsIgnoreCase(Constants.SplitOperator) == true) {

				idSplit = i + numberOfLocalOperators;
			
			} else if (tempOpNode.getOpName().equalsIgnoreCase(Constants.MergeOperator) == true) {
				
				idMerge = i + numberOfLocalOperators;
			
			} else if (tempOpNode.getOpName().equalsIgnoreCase(Constants.Instance2BlockOperator) == true) {

				int[] tempIDs = {idDistributedBottomCut};
				tempOpNode.setProducerIDs(tempIDs);
			}
			
			if (tempOpNode.getProducerIDs()[0] != 0 && !tempOpNode.getOpName().equalsIgnoreCase(Constants.Instance2BlockOperator)) {
				int[] tempIDs = tempOpNode.getProducerIDs();
				for (int k = 0; k < tempIDs.length; k++) {
					tempIDs[k] += numberOfLocalOperators;
				}
				tempOpNode.setProducerIDs(tempIDs);
			}
			
			tempOpNode.setOpID(i+numberOfLocalOperators);
			qepDistributedLocal.getOperatorList().put((i+numberOfLocalOperators)+"",tempOpNode);
			qepDistributedLocal.getOperatorList().remove(i+"");
		}
		
		// create receivers
		receiverIDs = new int[infoNodes.getNumberOfNodes()];
		
		for (int i= 1; i <= (infoNodes.getNumberOfNodes()); i++) {
			
			int increment = qepDistributedLocal.getOperatorList().size()+1+numberOfLocalOperators;
			String[] params = new String[4];
			params[0] = infoNodes.getNodes()[i-1];
			// We know that the remote sender has an id equal to 
			// (3 "original sender id" + number of operators in distributed fragment)
			params[1] = (Constants.idSenderInRemotePlan + idDistributedBottomCut - idDistributedTopCut + 1) + "";
			// TODO Check this parameter while testing the system
			params[2] = "5";
			params[3] = "1000";
			String timeStamp = System.currentTimeMillis() + "";
			int producers[] = new int[1];
			producers[0] = 0;
			OpNode tempOpReceiver = new OpNode(increment, "Receiver", producers, params, timeStamp, null, false);
			qepDistributedLocal.getOperatorList().put(increment+"", tempOpReceiver);
			receiverIDs[i-1] = increment;
		}
		
		// create senders and fill the relation string sender->node.
		// This string is used in the ExecutionProfileLogger.
		senderIDs = new int[infoNodes.getNumberOfNodes()];
		String senderToNodeRelation = "";
		for (int i= 1; i <= (infoNodes.getNumberOfNodes()); i++) {
			
			int increment = qepDistributedLocal.getOperatorList().size()+1+numberOfLocalOperators;
			int []_id = new int[1];
			_id[0]=idSplit;
			String timeStamp = System.currentTimeMillis() + "";
			OpNode tempOpSender = new OpNode(increment,"Sender",_id,null, timeStamp, null, false);
			qepDistributedLocal.getOperatorList().put(increment+"",tempOpSender);
			senderIDs[i-1] = increment;
			senderToNodeRelation +=  increment + "," + i + ";";
		}
		
		// Put the Sender to Node relation in the BlackBoard
		BlackBoard bl = BlackBoard.getBlackBoard();
		bl.put(Constants.SENDER_NODE_RELATIONS, senderToNodeRelation);
		
		// create Merge producers
		OpNode mergeNode = (OpNode) qepDistributedLocal.getOperatorList().get(idMerge+"");
		int[] mergeProducers = new int[receiverIDs.length];
		for (int i = 0; i < receiverIDs.length; i++) {
			
			mergeProducers[i] = receiverIDs[i];
		}
		mergeNode.setProducerIDs(mergeProducers);
		qepDistributedLocal.getOperatorList().remove(idMerge+"");
		qepDistributedLocal.getOperatorList().put(idMerge+"", mergeNode);
	}
	
	/**
	 * This method creates the remote QEPs
	 */
	private void updateQEPDistributedRemote() {
		
		int idDistributedTopCutRemote = Constants.idB2IOperator; // XXX TODO Corresponds to the B2I operator in our current model
		int numberOfNewOperators = idDistributedBottomCut - idDistributedTopCut + 1;
		
		// update remote QEP with operator's and producer's ids according to the topCutNode and downCutNode
		for (int i = qepDistributedRemote.getOperatorList().size(); i > idDistributedTopCutRemote ; i--) {
			
			OpNode tempOpNode = (OpNode)qepDistributedRemote.getOperatorList().get(i+"");
			
			int[] tempIDs = tempOpNode.getProducerIDs();
			for (int k = 0; k < tempIDs.length; k++) {
				tempIDs[k] += numberOfNewOperators; 
			}
			
			tempOpNode.setProducerIDs(tempIDs);
			qepDistributedRemote.getOperatorList().put((numberOfNewOperators+i) + "", tempOpNode);
			qepDistributedRemote.getOperatorList().remove(i+"");
		}
		
		// update the temporary hashtable that stores the distributed fragment taken from the intial plan
		for (int i = 1; i <= numberOfNewOperators; i++) {
			
			OpNode tempOpNode = (OpNode) temporaryHashtable.get(i+"");
			int[] tempIDs = tempOpNode.getProducerIDs();
			for (int k = 0; k < tempIDs.length; k++) {
				tempIDs[k] = idDistributedTopCutRemote+i-1;
			}
			tempOpNode.setProducerIDs(tempIDs);
			qepDistributedRemote.getOperatorList().put((idDistributedTopCutRemote+i)+"", tempOpNode);
		}
	
		// create receivers, hashtable and fill the linkedlist containing the remote QEP
		for (int i= 1; i <= (infoNodes.getNumberOfNodes()); i++) {
			
			QEP qepRemote = new QEP();
			
			OpNode remoteOpReceiver = (OpNode) qepDistributedRemote.getOperatorList().get(Constants.idReceiverInRemotePlan + ""); // 1 = ID of Receiver in remote plan
			//remoteOpReceiver.setParams(null);
			String[] params = new String[4];
			// params[0] = infoNodes.getNodes()[0];
			String adressLocalWebService = SystemConfiguration.getSystemConfigInfo(Constants.LOCAL_WEB_SERVICE);
			
			// Build the local host IP adress and replace the substring "localhost" in the SystemConfigFile
			try {
				int indexOfLocalhost = adressLocalWebService.indexOf("localhost");
				if (indexOfLocalhost != -1) {
					String localIP = InetAddress.getLocalHost().getHostAddress();
					adressLocalWebService = adressLocalWebService.substring(0, indexOfLocalhost) + 
						localIP + adressLocalWebService.substring(indexOfLocalhost+"localhost".length(), adressLocalWebService.length());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			params[0] = adressLocalWebService;
			params[1] = senderIDs[i-1] + "";
			params[2] =  Constants.tempParamForReceveiver;
			params[3] = "1000";
			remoteOpReceiver.setParams(params);
			qepDistributedRemote.getOperatorList().put(Constants.idReceiverInRemotePlan+"", remoteOpReceiver);
			
			// create QEP Plans in string format
			qepRemote.setDocument(qepDistributedRemote.getDocument());
			qepRemote.setOpList(qepDistributedRemote.getOperatorList());
			String qepString = QEPFactory.generateString(qepRemote, "Remote");

			qepList.add(qepString);
			// logger.debug(qepString);
//			QEPFactory.logFile("qepDistributed" + "_Req" 
//					+ request.getRequestID() + "_node" + i + "_" + System.currentTimeMillis() + ".xml", qepString);
		}	
	}
	
	/**
	 * loadQEPTemplates loads the QEP fragments used in the generation of the remote and local plans 
	 *
	 * @throws QEPInitializationException
	 */
	private void loadQEPTemplates() throws QEPInitializationException {
			
		try {
		
			String qepDistributedLocalName = QEPFactory.
				getQEP(Constants.REQUEST_TYPE_SERVICE_DISCOVERY, 1);
		
			String qepDistributedRemoteName = QEPFactory.
				getQEP(Constants.REQUEST_TYPE_SERVICE_DISCOVERY, 2);
		
			qepDistributedLocal = new QEP();
			qepDistributedRemote = new QEP();
		
			qepDistributedRemote.setOpList(QEPFactory.
				loadQEP(qepDistributedRemoteName,ch.epfl.codimsd.qeef.util.Constants.qepAccessTypeLocal, qepDistributedRemote));
		
			qepDistributedLocal.setOpList(QEPFactory.
				loadQEP(qepDistributedLocalName,ch.epfl.codimsd.qeef.util.Constants.qepAccessTypeLocal, qepDistributedLocal));
		
		} catch (CatalogException ex) {
			throw new QEPInitializationException("Error searching for the distributed templates in the catalog");
		} catch (RequestTypeNotFoundException ex2) {
			throw new QEPInitializationException("Discovery Request Type not found");
		}
	}
	
	/**
	 * Construct the G2NInfoNode structure and make call to the initial scheduler algorithm of G2N.
	 * The method first retrieves the environment id to execute within the System Configuration info. It 
	 * constructs afterwards the corresponding FragmentInfo objects which corresponds to the nodes
	 * of this environment. The FragmentInfo objects are sent to G2N scheduler which creates a new set of 
	 * nodes with the corresponding number of initial tuples to process. The initial set of nodes is kept
	 * in a Vector structure under the G2NInfoNodes class and written in the BlackBoard for further use.
	 * 
	 * @throws CatalogException
	 */
	private void callG2N() throws CatalogException {
		
		// Get the environment ID and build the set of nodes corresponding to this ID
		String stringEnvironment = SystemConfiguration.getSystemConfigInfo(Constants.ENVIRONMENT_ID);
		int environment = Integer.parseInt(stringEnvironment);
		Vector<FragmentInfo> nodes =  new Vector<FragmentInfo>();
		HashMap<Integer, String> addresses = new HashMap<Integer, String>();
		
		try {
			
			// Get the CatalogManager and the BalckBoard for further use.
			CatalogManager catalogManager = CatalogManager.getCatalogManager();
			BlackBoard bl = BlackBoard.getBlackBoard();
			
			// Find the number of distributed operators and computes the rates from nodeAppRate table.
			int rates[] = findDistributedOperators(environment);
			
			// There's no distributed operator, ie no parallelizable operator in QEP.
			if (rates.length == 0) {
				infoNodes = new G2NInfoNodes(null, addresses, request.getRequestID());
				logger.debug("Rates undefined in AppNodeRate.");
				return;
			}
			
			// We log in this string the node rates in order to notify the ExecutionGraph.
			String ratesRelation = "";
			
			// Construct the initial set of nodes for G2N.
			ResultSet rset = catalogManager.getObject("environment","environment.idenvironment="+environment);
			int k = 0;
			while (rset.next() == true) {
				
				Integer idNode = (Integer) rset.getObject(3);
				String address = (String) rset.getObject(5);
				FragmentInfo fnode = null;

				fnode = new FragmentInfo(idNode, rates[k], 
					Constants.msgProcessingTime, Constants.netTransmissionTime , 0);
				ratesRelation += ratesRelation + rates[k] + ";";
                                System.out.println("IdNODE = " + idNode + " address = " + address);
				addresses.put(idNode, address);
				nodes.add(fnode);
				k = k+1;
			}

			// Get the initial number of tuples from the Catalog or from the BlackBoard
			int initialNumberOfTuples = 0;
			if (bl.containsKey(Constants.QEP_SCAN_NUMBER_TUPLES)) {
				initialNumberOfTuples = Integer.parseInt((String)bl.get(Constants.QEP_SCAN_NUMBER_TUPLES));
			} else {
				String sourceName = QEPFactory.getScanSource(qepInitial);
				initialNumberOfTuples = (Integer) catalogManager.getSingleObject("DS_TABLE",
						"numberoftuples","db_iri='"+sourceName+"'");
			}

			// Split need these parameters for the Execution profile table.
			bl.put(Constants.QEP_SCAN_NUMBER_TUPLES, initialNumberOfTuples+"");
			
			// Schedule the initial set of nodes that belongs to the specified environment id
			G2N g2n = new G2N();
			G2NNode[] g2NNodes = g2n.schedule(nodes, initialNumberOfTuples);
			infoNodes = new G2NInfoNodes(g2NNodes, addresses, request.getRequestID());
			
			logger.debug("G2N number of selected nodes : " + infoNodes.getNumberOfNodes());
			
			// Log into the initialExecutionProfile
			if (bl.containsKey(Constants.LOG_EXECUTION_PROFILE)) {
		    	String value = (String) bl.get(Constants.LOG_EXECUTION_PROFILE);
		    	if (value.equalsIgnoreCase("TRUE")) {
		    		ExecutionProfileLogger executionProfileLogger = new ExecutionProfileLogger();
		    		executionProfileLogger.setRequestID(request.getRequestID());
			    	executionProfileLogger.initialLog(infoNodes);
		    	}
		    }
			
		} catch (SQLException e) {
			throw new CatalogException("Cannot construct codims environement in Catalog : " + e.getMessage());
		}
	}
	
	/**
	 * Find the distributed operators in the plan. In order to decide which are the
	 * operators that will be executed in remote nodes, the method performs the
	 * following :
	 * <li> reads the chain of operators, and select a sub-chain composed of parallelizable operators.
	 * Those operators have the attribute parrallelizable set to true in the QEP intial.
	 * <li> For each operator, in each node of this environment, get its execution rate from
	 * the Catalog.
	 * <li> Computes the sum of the operator rates for each node.
	 * 
	 * @param environment the environment in which we execute the request.
	 * @return the node execution rates array.
	 * @throws CatalogException.
	 */
	private int[] findDistributedOperators(int environment) throws CatalogException {
		
		int rates[] = null;
		Vector<Integer> distributedOperatorsVector = new Vector<Integer>();
		Hashtable operators = qepInitial.getOperatorList();
		
		int i = 0;
		boolean firstParallelOperatorFound = false;
		boolean lastParallelOperatorFound = false;
		
		// Find the first operator that can be parallelizable in the QEP
		while (firstParallelOperatorFound == false) {
			i++;
			OpNode opNode = (OpNode) operators.get(i+"");
			if (opNode.getParallelizableIno() == true) {
				firstParallelOperatorFound = true;
				distributedOperatorsVector.add(new Integer(i));
			}
		}
		
		// Construct the chain of parallelizable operators in the QEP
		while (lastParallelOperatorFound == false) {
			i++;
			OpNode opNode = (OpNode) operators.get(i+"");
			if (opNode.getParallelizableIno() == false) {
				lastParallelOperatorFound = true;
			} else
				distributedOperatorsVector.add(new Integer(i));
		}

		// Construct the distributedOperators array (integers)
		distributedOperators = new int[distributedOperatorsVector.size()];
		for (int index = 0; index < distributedOperatorsVector.size(); index++) {
			distributedOperators[index] = distributedOperatorsVector.get(index);
		}
		
		try {
		
			CatalogManager cm = CatalogManager.getCatalogManager();
			ResultSet rset = cm.executeQueryString("SELECT * FROM node WHERE idenvironment="+environment);
			
			int numberOfNodes = 0;
			while (rset.next() == true)
				numberOfNodes++;

			rates = new int[numberOfNodes];
			
			for (int j = 0; j < rates.length; j++)
				rates[j] = 0 ; //Constants.DEFAULT_OPERATOR_EXECUTION_TIME;
			
			// Compute the rates.
			for (int operatorID : distributedOperators) {
			
				OpNode opNode = (OpNode) operators.get(operatorID+"");
				String opName = opNode.getOpName();
				ResultSet rset1 = cm.executeQueryString("SELECT idoperatortype FROM operatortype WHERE name='" + opName + "'");
				rset1.next();
				int operatortypeID = rset1.getInt(1);
				String query = "SELECT rate FROM AppNodeRate WHERE " +
						"idenvironment=" + environment + " AND idoperator=" + operatortypeID;
				
				ResultSet rset2 = cm.executeQueryString(query);
				
				int k = 0;
				while (rset2.next() == true) {
					int rate1 = rates[k];
					int rate2 = rset2.getBigDecimal(1).intValue();
					rates[k] += rate1 +  rate2;
					k++;
				}
			}
		
		} catch (CatalogException ex) {
			throw new CatalogException("CatalogException while computing node rates in Optimizer : " + ex.getMessage());
		} catch (SQLException ex) {
			throw new CatalogException("SQLException while computing node rates in Optimizer : " + ex.getMessage());
		}
		
		return rates;
	}
}

