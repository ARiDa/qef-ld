package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;

/**
 * Classe abstrata que representa um operador bin�rio.
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 01 FEV 2005
 */

public abstract class BinaryOperator extends Operator {

    /**
     * 
     */
    public BinaryOperator() { 
        super();
    }

    /**
     * @see Node#evaluate(DataUnit , DataUnit )
     */
    public Object evaluate(Instance leftInstance, Instance rightInstance) throws PredicateEvaluatorException {

        Object leftChildResult, rightChildResult;
        boolean result;

        //Realiza a avalia��o dos filhos - esquerdo e direito.
        leftChildResult = super.leftChild.evaluate(leftInstance, rightInstance);
        rightChildResult = rightChild.evaluate(leftInstance, rightInstance);
        
        //Aplica a opera��o definida por este operador.
        return apply(leftChildResult, rightChildResult);
    }
  
}
