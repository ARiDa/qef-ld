package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.operators;

import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.BinaryOperator;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Operator;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.datanodes.ColumnNode;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.datanodes.PropertyNode;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.datanodes.ValueNode;
import ch.epfl.codimsd.qeef.types.Type;

/**
 * Define opera��o menor igual que (<=).
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 01 FEV 2005
 */

public class LessEqual extends BinaryOperator {

    /**
     * 
     */
    public LessEqual() {
        super();
    }

	/**
	 * @see QEEF.predicate.evaluator.Operator#newInstance()
	 */
	public Operator newInstance(){
	    return new LessEqual();
	}
	
	/**
	 * @see QEEF.predicate.evaluator.Node#getParserRepresentation()
	 */
    public String getParserRepresentation()throws PredicateEvaluatorException{

        String operand;
        PropertyNode propertyNode = new PropertyNode();
        ColumnNode columnNode = new ColumnNode();
        ValueNode valueNode = new ValueNode();
        
        operand = "(";
        operand += propertyNode.getParserRepresentation() + "|";
        operand += columnNode.getParserRepresentation()+ "|";
        operand += valueNode.getParserRepresentation();
        operand += ")";
        
        return "(" + operand + "(\\p{Space}*<=\\p{Space}*)" + operand + ")";
    }

    /**
     * 
     * Verifica se o valor definido por leftValue � menor igual que rightValue.
     * Essa verifica��o � realizada com base
     * na interface Comparable que � implementada em todas as sub-classes de Type.
     *
     * @param leftValue  Uma inst�ncia de Type representando o valor obtido por um QEEF.predicate.evaluator.DataNode.
     * @param rightValue Uma inst�ncia de Type representando o valor obtido por um QEEF.predicate.evaluator.DataNode.
     *
     * @return Boolean. True se leftValue menor igual que rightValue, false caso contr�rio.
     *
     * @see QEEF.predicate.evaluator.Operator#apply(Object, Object)  
     */
    protected Object apply(Object leftValue, Object rightValue) {

        return ((Type) leftValue).compareTo(rightValue) <= 0 ? new Boolean(true)
                : new Boolean(false);
    }

    /**
     * @see Object#toString()
     */
	public String toString(){
	    return "<=";	    
	}
}
