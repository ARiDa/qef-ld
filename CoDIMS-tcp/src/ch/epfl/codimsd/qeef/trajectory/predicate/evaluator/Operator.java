
package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator;

/**
 * digite aqui descricao do tipo 
 *
 * @author Vinicius Fontes
 * 
 * @date Jun 4, 2005
 */
public abstract class Operator extends Node{
    
    /**
     * 
     */
    public Operator(){
        super();
    }
    
    /**
     * 
     * Cria uma nova inst�ncia desta classe.
     * 
     * @return Nova inst�ncia deste operador.
     */
    public abstract Operator newInstance();
    
    /**
     * M�todo asbtrato que define a a��o a ser implementada pelo operador. O
     * componente Predicate Evaluator oferece suporte a operadores Binarios e
     * Un�rios, que s�o comuns na maioria dos banco de dados. Por isso recebe
     * dois parametros que s�o as inst�ncias a serem comparadas. No caso de
     * operadores un�rios o segundo parametro ser� null.
     * 
     * @param left  O resultado da avalia��o feita pelo filho esquerdo. Normalmente um Booleano ou uma inst�ncia de QEEF.types.Type.
     * @param right O resultado da avalia��o feita pelo filho direito. Normalmente um Booleano ou uma inst�ncia de QEEF.types.Type.
     * 
     * @return Um objeto que informa o resultado da avalia��o realizada por este
     *         operador. 
     * 
     * @throws PredicateEvaluatorException
     *             Se ocorrer algum erro durante a avalia��o do predicado.
     *             Provavelmente erro no tipo esperado.
     */
    protected abstract Object apply(Object left, Object right)  throws PredicateEvaluatorException;

    
    
}
