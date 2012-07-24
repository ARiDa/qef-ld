package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.operators;

import java.util.Collection;
import java.util.Iterator;

import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.BinaryOperator;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Operator;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.datanodes.ColumnNode;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.datanodes.PropertyNode;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.datanodes.ValueNode;
import ch.epfl.codimsd.qeef.trajectory.function.tcp.TCP;
import ch.epfl.codimsd.qeef.types.PointListType;
import ch.epfl.codimsd.qeef.types.Point;

/**
 * Operator que determina se uma particula est� dentro de 
 * um tetraedro(que � definido por um conjunto de 4 pontos).
 * 
 * Forma: Point isInside List of Points
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 5 JUN 2005
 */
public class IsInside extends BinaryOperator{

    /**
     * 
     */
	public IsInside(){
		super();
	}
	
	/**
	 * @see QEEF.predicate.evaluator.Operator#newInstance()
	 */
	public Operator newInstance(){
	    return new IsInside();
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
        operand += columnNode.getParserRepresentation();
        operand += ")";
        
        return "(" + operand + "([\\p{Space}]*(?i)isInside(?-i)[\\p{Space}]*)" + operand + ")";
    }

    
	/**
	 * Verifica se um ponto est� dentro um Tetraedro(uma elements de quatro pontos).
	 * 
	 * @param QEEF.types.Point Um ponto qualquer.
	 * @param QEEF.types.PointListType Um tetraedro.
	 * 
     * @param leftValue  Uma inst�ncia de Type representando o valor obtido por um QEEF.predicate.evaluator.DataNode.
     * @param rightValue Uma inst�ncia de Type representando o valor obtido por um QEEF.predicate.evaluator.DataNode.
     *
	 * @return Boolean. True se ponto estiver dentro do tetraedro, false caso contr�rio.
	 * 
     * @see QEEF.predicate.evaluator.Operator#apply(Object, Object)
	 */
	protected Object apply(Object leftValue, Object rightValue) throws PredicateEvaluatorException {

		//Verifica parametros
	    if( ! (leftValue instanceof Point) )
	        throw new PredicateEvaluatorException("IsInside: Parametro inv�lido. Forma Point isInside Lista de pontos");
	        
	    if( ! (rightValue instanceof PointListType) )
	        throw new PredicateEvaluatorException("IsInside: Parametro inv�lido. Forma Point isInside Lista de pontos");
	    
	    //Prepara ambiente pegando variaveis
		Point particle = (Point)leftValue;
		
		Collection vertexes = (Collection)rightValue;
		Point p1, p2, p3, p4;
		Iterator it;
		
		it = vertexes.iterator();
		p1 = (Point)it.next();
		p2 = (Point)it.next();
		p3 = (Point)it.next();
		p4 = (Point)it.next();
		
		return new Boolean (TCP.isInsideCell(particle, p1, p2, p3, p4));
	}
	
    /**
     * @see Object#toString()
     */
	public String toString(){
	    return "IsInside";	    
	}
}
