package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.operators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.BinaryOperator;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.DataNode;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Operator;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;

/**
 * Define opera��o AND d� l�gica booleana.
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 01 FEV 2005
 *
 * Implementa��o do m�todo apply transferido para evaluate por quest�es de performance.
 * Nem sempre � necess�rio a avalia��o de ambos os lados da express�o. Se a esquerda j� for falsa,
 * o resultado j� � falso.
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 25 FEV 2005
 */

public class And extends BinaryOperator {

    /**
     * 
     */
	public And(){
		super();
	}
	
	/**
	 * @see QEEF.predicate.evaluator.Operator#newInstance()
	 */
	public Operator newInstance(){
	    return new And();
	}
	
	/**
	 * @see QEEF.predicate.evaluator.Node#getParserRepresentation()
	 */
	public String getParserRepresentation(){
		
		return "(\\p{Space}+(?i)AND(?-i)\\p{Space}+)";		
	}

    /**
     * Implementa��o deste m�todo tamb�m define a implementa��o da opera��o AND.
     * Para maiores informa��es ver coment�rios n� m�todo apply desta classe.
     * 
     * @see QEEF.predicate.evaluator.Node#evaluate(DataUnit, DataUnit)
     */
	public Object evaluate(Instance t1, Instance t2) throws PredicateEvaluatorException {
		
		Object leftChildResult, rightChildResult;
		boolean result;
		
		if(leftChild instanceof DataNode)
			leftChildResult  = leftChild.evaluate(t1, null);
		else
			leftChildResult  = leftChild.evaluate(t1, t2);
		
		if( ((Boolean)leftChildResult).booleanValue() == false)
		    return leftChildResult;//false
		
		if(rightChild instanceof DataNode)
			rightChildResult  = rightChild.evaluate(t2, null);
		else
			rightChildResult  = rightChild.evaluate(t1, t2);
		
		return rightChildResult;
	}
	
    /**
     * Implementa a opera��o AND da l�gica.
     * Implementa��o desta opera��o foi tranferida para o m�todo evaluate por quest�es de performance.
     * Nem sempre � necess�rio a avalia��o de ambos os lados da express�o. Se a esquerda j� for falsa,
     * o resultado j� � falso.
     * 
     * @param Booleano com resultado da avalia��o do filho esquerdo.
     * @param Booleano com resultado da avalia��o do filho direito.
     * 
     * @return Boolean True se a avalia��o dos dois filhos for igual a true, false caso contr�rio.
     * 
     * @see QEEF.predicate.evaluator.Operator#apply(Object, Object)
     */
	protected Object apply(Object obj1, Object obj2){return null;}
	
	/**
	 * 
	 */
	public String toString(){
	    return "AND";	    
	}
	
    public static void main(String[] args) {

        And e = new And();
        Pattern p = Pattern.compile(e.getParserRepresentation()); 
        Matcher m = p.matcher(" fghfdgh =    AND     gfhhf");
        
        System.out.println(e.getParserRepresentation());
        while( m.find() )
        	System.out.println( m.group() );

    }
}
