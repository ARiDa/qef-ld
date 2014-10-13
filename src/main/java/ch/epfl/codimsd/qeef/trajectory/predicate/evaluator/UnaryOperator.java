package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator;
 
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;

/**
 * Classe abstrata que representa um operador un�rio.
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 01 FEV 2005
 */

public abstract class UnaryOperator extends Operator {

    /**
     * @clientCardinality 1
     * @directed true
     * @label child
     * @link aggregationByValue
     * @supplierCardinality 1
     */

	public UnaryOperator(){
		super();
	}		
	
	/**
	 * @see QEEF.predicate.evaluator.Node#evaluate(DataUnit , DataUnit )
	 */
	public Object evaluate(Instance leftInstance, Instance rightInstance) throws PredicateEvaluatorException{
		
		Object childResult;
		
		//Verifica se este operador � o filho esquerdo o direito em rela��o a seu pai
        if ( parent.leftChild.equals(this) )
            childResult = getChild().evaluate(leftInstance, null);
        else 
            childResult = getChild().evaluate(rightInstance, null);
        
		return  apply(childResult, null);
		
	}
	
	/**
	 * 
	 */
	public Node getChild(){
	    return leftChild;
	}
	
	/**
	 * 
	 */
	public void setChild(Node child){
	    leftChild = child;
	}
	
}
