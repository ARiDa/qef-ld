package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.operators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.BinaryOperator;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.DataNode;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Operator;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;

/**
 * Define opera��o OR d� l�gica booleana.
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 01 FEV 2005
 */

/**
 * Implementa��o do m�todo apply transferido para evaluate por quest�es de performance.
 * Nem sempre � necess�rio a avalia��o de ambos os lados da express�o. Se a esquerda j� for true,
 * n�o � necess�rio a avalia��o do restante.
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 25 FEV 2005
 */

public class Or extends BinaryOperator {

    /**
     * 
     */
	public Or(){
		super();
	}
	
	/**
	 * @see QEEF.predicate.evaluator.Operator#newInstance()
	 */
	public Operator newInstance(){
	    return new Or();
	}
	
	/**
	 * @see QEEF.predicate.evaluator.Node#getParserRepresentation()
	 */
	public String getParserRepresentation(){
		
	    return "(\\p{Space}+(?i)OR(?-i)\\p{Space}+)";		
	}
	
    /**
     * Implementa a opera��o AND da l�gica.
     * Implementa��o desta opera��o foi tranferida para o m�todo evaluate por quest�es de performance.
     * Nem sempre � necess�rio a avalia��o de ambos os lados da express�o. Se a esquerda j� for falsa,
     * o resultado j� � falso.
     * 
     * @param left Booleano com resultado da avalia��o do filho esquerdo.
     * @param right Booleano com resultado da avalia��o do filho direito.
     * 
     * @return Boolean True se a avalia��o de um dos filhos for igual a true, false caso contr�rio.
     * 
     * @see QEEF.predicate.evaluator.Operator#apply(Object, Object)
     */
	protected Object apply(Object left, Object right){return null;}
	
    /**
     * Implementa��o deste m�todo tamb�m define a implementa��o da opera��o OR.
     * Para maiores informa��es ver coment�rios n� m�todo apply desta classe.
     * 
     * @see QEEF.predicate.evaluator.Node#evaluate(DataUnit, DataUnit)
     * 
     */
	public Object evaluate(Instance t1, Instance t2) throws PredicateEvaluatorException{
		
		Object leftChildResult, rightChildResult;
		boolean result;
		
		if(leftChild instanceof DataNode)
			leftChildResult  = leftChild.evaluate(t1, null);
		else
			leftChildResult  = leftChild.evaluate(t1, t2);
		
		if( ((Boolean)leftChildResult).booleanValue() == true)
		    return leftChildResult;//true 
		
		if(rightChild instanceof DataNode)
			rightChildResult  = rightChild.evaluate(t2, null);
		else
			rightChildResult  = rightChild.evaluate(t1, t2);
		
		return rightChildResult;
	}

	public String toString(){
	    return "OR";	    
	}
	
    public static void main(String[] args) {

        Or e = new Or();
        Pattern p = Pattern.compile(e.getParserRepresentation()); 
        Matcher m = p.matcher(" fghfdgh =    or     gfhhf");
        
        System.out.println(e.getParserRepresentation());
        while( m.find() )
        	System.out.println( m.group() );

    }
}
