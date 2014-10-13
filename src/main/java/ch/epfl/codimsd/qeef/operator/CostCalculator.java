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
package ch.epfl.codimsd.qeef.operator;

import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;
import java.math.BigDecimal;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.exceptions.operator.OperatorInitializationException;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.operator.OperatorFactoryManager;
import ch.epfl.codimsd.qeef.operator.modules.ModFix;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qep.OpNode;

/**
 * The CostCalculator operator computes the rate of some operator
 * running on one particular node. The rates are stroed in the
 * CatalogManager and used in the optimization process.
 * 
 * In order to compute the rate, we encapsulate the operator definition
 * in the QEP in the definition of the CostCalculator. Example of QEP definition : 
 *  <op:Operator id="3" prod="4" type="" parallelizable="true">
 *  	<Name>CostCalculator</Name>
 *  	<ParameterList>
 *  		<operator>Project</operator>
 *  		<removedAttributes/>
 *  	</ParameterList>
 *  </op:Operator>
 *  
 * @author Othman Tajmouati
 */
public class CostCalculator extends Operator {

	/**
	 * The operator to test.
	 */
	private Operator encapsulatedOperator;
	
	/**
	 * OpNode structure containing some useful informations on the
	 * operator.
	 */
	private OpNode opNode;
	
	/**
	 * Vector containing the registred execution times.
	 */
	private Vector<Long> executionTimeValues;
	
	/**
     * Log4j logger.
     */
    private static Logger logger = Logger.getLogger(CostCalculator.class.getName());
	
	/**
	 * Constructor. It creates the encapsulated operator.
	 * 
	 * @param id Id of the operator.
	 * @param opNode CostCalculator opNode.
	 */
	public CostCalculator(int id, OpNode opNode) throws PredicateEvaluatorException {
		
		// Call Operator constructor.
		super(id);
		
		this.opNode = opNode;
		executionTimeValues = new Vector<Long>();
		
		// Get encapsulatedOperator name and parameters
		String params[] = opNode.getParams();
		String opName = params[0];
		String newParams[] = new String[params.length-1];
		
		// Get encapsulated operator parameters.
		for (int i = 0; i < newParams.length; i++) {
			newParams[i] = params[i+1];
		}
		
		// Create the opNode structure for the encapsulated op and call the OperatorFactoryManager.
		OpNode newOpNode = new OpNode(opNode.getOpID(), 
				opName, opNode.getProducerIDs(), newParams, 
					System.currentTimeMillis()+"", opNode.getType(), true);
		OperatorFactoryManager operatorFactoryManager =  new OperatorFactoryManager();
		
		try {
			
			// create encapsulated operator
			encapsulatedOperator = operatorFactoryManager.createOperator(newOpNode);
			
		} catch (OperatorInitializationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Open the operator.
	 */
	public void open() throws Exception {
		
		try {	
			
			// Create modfix (consumer->producer relation)
			// This operation is done in the open because all the operators are now created
			// - we can read the producers.
			for (int i = 0; i < getProducers().size(); i++) {
				@SuppressWarnings("unused") 
				ModFix modFix = new ModFix(encapsulatedOperator, (Operator) producers.get(i));
			}
			
			// open the encapsulated operator.
			// encapsulatedOperator.open();
			
			// open the chain of operators
			
			
			// set metadata for the encapsulated operator.
			Metadata aux[] = new Metadata[getProducers().size()];
			aux[0] = encapsulatedOperator.getMetadata(opNode.getOpID());
			setMetadata(aux);
			
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
	}
	
	/**
	 * Get next tuples.
	 */
	public DataUnit getNext(int consumerId) throws Exception {
		
		try {

			// Log startime.
			long startTime = System.currentTimeMillis();
			
			// Get next results from the encapsulated operator.
			instance = encapsulatedOperator.getNext(consumerId);

			// Log needed times for cost calculation.
			executionTimeValues.add(System.currentTimeMillis() - startTime);
		
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		
		return instance;
	}
	
	/**
	 * Close the operator and log the mean for the encapsulated operator.
	 */
	public void close() throws Exception {
		
		try {

			// Close the encapsulated operator.
			encapsulatedOperator.close();
			
			// Calculate the mean rate.
			long rate = 0;
			for (int i = 0; i < executionTimeValues.size(); i++) {
				long time = executionTimeValues.get(i);
				rate = rate + time;
			}
			rate /= executionTimeValues.size();
			
			// Get needed parameters for cost logging.
			BlackBoard bl =  BlackBoard.getBlackBoard();
			String nodeID = (String) bl.get(Constants.ENVIRONMENT_ID);
			String environmentID = (String) bl.get(Constants.NODE_ID);

			// Log the computed rate.
			logger.debug("Rate equal to : " + rate + ", for this op and nodeID(" + nodeID + ")");
			
			// Get the CatalogManager for writing into the AppNodeRate table.
			CatalogManager cm = CatalogManager.getCatalogManager();
			
			// Get encapsulated operator id from operatortype table.
			BigDecimal opID = (BigDecimal) cm.getSingleObject("operatortype","idoperatortype","name='"+opNode.getParams()[0]+"'");
			
			// Delete old rates for this entry.
			cm.executeQueryString("DELETE FROM AppNodeRate WHERE " +
					"idenvironment=" + environmentID + " AND idoperator=" + opID.intValue());
			
			// Log the rates.
			cm.executeQueryString("insert into AppNodeRate Values " +
					"("+ environmentID +"," + nodeID + "," + opID.intValue() + "," + rate + ")");
		
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
	}
	
	/**
	 * Set the metadata.
	 */
	public void setMetadata(Metadata prdMetadata[]) {
		this.metadata = prdMetadata;
	}
}

