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
import ch.epfl.qosdisc.operators.PropertySet;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.initialization.InitializationException;
import ch.epfl.codimsd.exceptions.operator.OperatorException;
import ch.epfl.codimsd.qep.QEP;
import ch.epfl.codimsd.query.RequestResult;
import ch.epfl.codimsd.query.ResultSet;
import ch.epfl.codimsd.qeef.discovery.DiscoveryPlanManager;
import ch.epfl.codimsd.qeef.operator.OperatorFactoryManager;

/**
 * QEEF defines the functionalities of an execution machine in CoDIMS. The class executes a request
 * in both remote and centralized mode. When executing a request, QEEF methods performs the following :
 * <li> Open the operators;
 * <li> Call getNext on the operator chain, and get the results;
 * <li> Close the operatos.
 * 
 * In distributed mode, the request result is converted to a DataHandler object in order to send it in 
 * a SOAP message.
 * 
 * Othman Tajmouati changes :
 * - Javadoc transalated to english.
 * - Added variables : Metadata[] clientMetada, RequestResult requestResult, ResultSet rset.
 * - Added methods : getRequestResult(),  setPlanOperator(), init, getClientMetadata
 * 
 * @author Fausto Ayres, Vinicius Fontes Vieira, Othman Tajmouati
 */

public class QEEF {

	/**
	 * Metadata used for writing the object returns to the client, when
	 * QEEF is running in a Local Web Service.
	 */
	private Metadata[] clientMetadata;

	/**
	 * Contains the operators.
	 */
	private Hashtable<Integer, Operator> planOps;

	final static Logger logger = LoggerFactory.getLogger(QEEF.class);

	/**
	 * The final results.
	 */
	private RequestResult requestResult;

	/**
	 * Default constructor.
	 */
	public QEEF() {}

	/**
	 * @return the request result in a centralized mode.
	 */
	public RequestResult getRequestResult() {
		return requestResult;
	}

	/**
	 * Execute a request when CoDIMS runs in centralized mode.
	 * @return the results in a RequestResult object.
	 * @throws Exception
	 */
	public RequestResult execute(String[] paramNames, String[] paramValues) throws Exception {

		long finishingTime = 0;
		PerformanceAnalyzer pa = PerformanceAnalyzer.getPerformanceAnalyzer();

		// Get the first operator of the query execution plan.
		Operator rootOp = (Operator) planOps.get(1);
		
		if (paramNames != null && paramValues != null) {
			Map<String, Object> params = new Hashtable<String, Object>();
			params.put("paramNames", paramNames);
			params.put("paramValues", paramValues);
			
			rootOp = rootOp.cloneOperator(params);
		}

		// Initializations.
		DataUnit nextTuple;
		ResultSet result = new ResultSet();
		requestResult = new RequestResult();
		Metadata metadata = null;
		short resultCode = 0;
		result.open();

		try {

			// Open the root operator. This operation opens all the operators of the chain.
			long beginOpeningOperatorsTime = System.currentTimeMillis();
			open(rootOp);
			metadata = rootOp.getMetadata(rootOp.getId());
			pa.log("Opening operators : ", System.currentTimeMillis()-beginOpeningOperatorsTime);
			logger.debug("Operators opened.");

			// Get the results and add each tuple to the ResultSet object.
			long beginGetNextOperatorsTime = System.currentTimeMillis();
			BlackBoard bl = BlackBoard.getBlackBoard();
			bl.put("GETNEXT_TIME", beginGetNextOperatorsTime );

			while ((nextTuple = getNext(rootOp)) != null) {
				result.add(nextTuple);
			}

			finishingTime = System.currentTimeMillis();

			// Closes the operators.
			close(rootOp);
			logger.debug("Operators closed.");

		} catch (Exception ex) {
			throw new Exception("Error while executing the request : " + ex.getMessage());
		}

		// Constructs the RequestResult object.
		result.close();
		requestResult.setResultSet(result);
		requestResult.setResultCode(resultCode);
		requestResult.setMetadata(metadata);

		pa.log("FinishingTime : ", System.currentTimeMillis()-finishingTime);

		return requestResult;
	}

	/**
	 * Execute a request in the remote node.
	 * 
	 * @return the DataHandler containing the result in for of InputStream. 
	 * @throws RemoteException
	 */
	@SuppressWarnings("deprecation")
	public DataHandler executeRemote() throws RemoteException {

		PerformanceAnalyzer pa = PerformanceAnalyzer.getPerformanceAnalyzer();

		// Open the first operator of the chain.
		Operator rootOp = (Operator) planOps.get(1);

		// Initializations.
		DataHandler dh;
		DataUnit nextTuple;
		Block finalBlock = new Block();
		ResultSet result = new ResultSet();
		requestResult = new RequestResult();
		result.open();

		try {
			logger.info("Executing request...");

			// Open the chain of operators.
			logger.info("Opening operators.");
			long beginOpeningOperatorsTime = System.currentTimeMillis();
			open(rootOp);
			pa.log("Opening operators : ", System.currentTimeMillis()-beginOpeningOperatorsTime);

			// Set the metadata of the returned result.
			clientMetadata = rootOp.returnMetadata();

			long beginGetNextOperatorsTime = System.currentTimeMillis();
			BlackBoard bl = BlackBoard.getBlackBoard();
			bl.put("GETNEXT_TIME", beginGetNextOperatorsTime );

			// Get the results and add each tuple in a final block.
			while ((nextTuple = (DataUnit) getNext(rootOp)) != null) {

				finalBlock.add((Instance)nextTuple);
			}
			logger.info("Execution finished.");

			// Close the chain of operator.
			logger.info("Closing Operators.");
			close(rootOp);

			long finishingTime = System.currentTimeMillis();
			byte[] returnBytes;
			ByteArrayOutputStream out;
			ObjectOutputStream objOut;

			out = new ByteArrayOutputStream();
			objOut = new ObjectOutputStream(out);
			objOut.writeObject(finalBlock);
			objOut.flush();

			returnBytes = out.toByteArray();
			dh = new DataHandler(new org.apache.turbine.util.mail.ByteArrayDataSource(returnBytes,"application/octet-stream"));

			pa.log("FinishingTime : ", System.currentTimeMillis()-finishingTime);
			pa.finalizeLogging();

			// Return the DataHandler.
			return dh;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RemoteException(ex.getMessage());
		}
	}

	/**
	 * Open an operator given its id.
	 * 
	 * @param idOp id of operator.
	 */
	public void open(int idOp) throws Exception {

		Operator op;
		op = (Operator) this.planOps.get(idOp);

		if (op == null) {
			logger.warn("QEEF error: Operator not defined {}", idOp);
			throw new Exception("Operator " + idOp + " is not defined in this execution plan.");
		}

		open(op);
	}

	/**
	 * Open an operator given itself.
	 * 
	 * @param op the operator to open.
	 */
	public void open(Operator op) throws Exception {
		op.open();
	}

	/**
	 * Get the next tuple from an operator.
	 * 
	 * @param idOp the operator from which it gets the tuple.
	 */
	public DataUnit getNext(int idOp) throws Exception {

		Operator op;
		op = (Operator) this.planOps.get(idOp);

		if (op == null) {
			logger.warn("QEEF error: Operator not defined {}", idOp);
			throw new Exception("Operator " + idOp + "didn't exist.");
		}

		return getNext(op);
	}

	/**
	 * Get the next result of this operator.
	 * 
	 * @param op the operator from which it gets the result.
	 */
	public DataUnit getNext(Operator op) throws Exception {
		return op.getNext(0);
	}

	/**
	 * Close an operator given its id.
	 * 
	 * @param idOp the id of the operator to close.
	 */
	public void close(int idOp) throws Exception {

		Operator op;
		op = (Operator) this.planOps.get(idOp);

		if (op == null) {
			logger.warn("QEEF error: Operator not defined {}", idOp);
			throw new Exception("Operator " + idOp + "didn't exist.");
		}

		close(op);
	}

	/**
	 * Close an operator
	 * 
	 * @param op
	 *            Operador que se deseja encerrar.
	 */
	public void close(Operator op) throws Exception {
		op.close();
	}

	/**
	 * Imprime um cabe�alho no fluxo out com o nome dos dados contidos no
	 * metadado de um operador.
	 * 
	 * @param out
	 *            Fluxo onde cabe�alho ser� impresso.
	 * @param op
	 *            Operador do qual se obter� metadados.
	 */
	public void displayNames(Writer out, Operator op) throws IOException,
	Exception {

		op.getMetadata(0).displayNames(out);
	}

	/**
	 * Obtem um operador do plano em execução na máquina.
	 * 
	 * @param opId Identificador do operador.
	 */
	protected Operator getPlanOperator(int opId) {
		return (Operator) planOps.get(opId);
	}

	protected void setPlanOperator(Hashtable<Integer, Operator> planOps) {
		this.planOps = planOps;
	}

	public void init(String qepString) throws InitializationException, OperatorException, CatalogException, OperatorInitializationException, PredicateEvaluatorException {

		SystemConfiguration.loadRemoteSystemConfiguration();
		OperatorFactoryManager operatorFactoryManager = new OperatorFactoryManager();
		DiscoveryPlanManager discoveryPlanManager = new DiscoveryPlanManager(operatorFactoryManager);
		//PlainFilePlanManager trajectoryPlanManager = new PlainFilePlanManager(operatorFactoryManager);

		QEP qep = discoveryPlanManager.instantiatePlan(qepString);
		this.planOps = qep.getConcreteopList();

		// Recover all properties from the blackboard.
		BlackBoard bb = BlackBoard.getBlackBoard();
		Properties props = new Properties();
		for(Map.Entry<String, Object> o : bb.getHashtable().entrySet()) {

			if(o.getValue().getClass() == String.class) {
				props.setProperty(o.getKey(), (String)o.getValue());
			}
		}

		PropertySet.setup(".");
		PropertySet.setProperties(props);

		/*try {

			Connection.open(PropertySet.props);

                } catch (Exception ex) {
                        // throw new InitializationException("Cannot connect to qosdisc database : " + ex.getMessage());
                }*/
	}

	@SuppressWarnings("deprecation")
	public DataHandler getClientMetadata() throws IOException {

		DataHandler dh = null;
		byte[] returnBytes;
		ByteArrayOutputStream out;
		ObjectOutputStream objOut;

		out = new ByteArrayOutputStream();
		objOut = new ObjectOutputStream(out);
		objOut.writeObject(clientMetadata);
		objOut.flush();

		returnBytes = out.toByteArray();
		dh = new DataHandler(new org.apache.turbine.util.mail.ByteArrayDataSource(returnBytes,"application/octet-stream"));

		return dh;
	}
}

