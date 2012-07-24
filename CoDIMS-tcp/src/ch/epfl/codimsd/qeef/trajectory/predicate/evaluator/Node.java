package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator;

import ch.epfl.codimsd.qeef.Instance;

/**
 * Define estrutura de um n� da �rvore de Avalia��o.
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 01 FEV 2005
 */

public abstract class Node {

    /**
     * Define quem � o pai deste n� na �rvore de avalia��o.
     * 
     * @directed true
     * @label parent
     */

    protected ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Node parent;

    /**
     * Define o filho esquerdo de um n� na �rvore de avalia��o.
     * 
     * @directed true
     * @label leftChild
     *  
     */

    protected ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Node leftChild;

    /**
     * Define o filho direito de um n� na �rvore de avalia��o.
     * 
     * @alias rightChild
     * @directed true
     * @label rightChild
     *  
     */

    protected ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Node rightChild;

    /**
     *  
     */
    protected Node() {
        super();

        parent = (null);
        leftChild = (null);
        rightChild = (null);
    }

    /**
     * M�todo asbtrato respons�vel por realizar a avalia��o dos filhos deste n�
     * na �rvore de avalia��o. Define uma interface de comunica��o entre os n�s
     * da �rvore de avalia��o. O componente Predicate Evaluator oferece suporte
     * a operadores Binarios e Un�rios, que s�o comuns na maioria dos banco de
     * dados. Por isso recebe dois parametros que s�o as inst�ncias a serem
     * comparadas. No caso de operadores un�rios o segundo parametro ser� null.
     * 
     * @param left
     *            DataUnit cujos dados s�o referenciados do lado esquerdo do
     *            predicado.
     * @param right
     *            DataUnit cujos dados s�o referenciados do lado direito do
     *            predicado.
     * 
     * @return Um objeto que informa o resultado da avalia��o realizada por este
     *         n�.
     * 
     * @throws PredicateEvaluatorException
     *             Se ocorrer algum erro durante a avalia��o do predicado.
     */
    public abstract Object evaluate(Instance left, Instance right)
            throws PredicateEvaluatorException;

    /**
     * Define uma express�o regular utilizada no reconhecimento de um tipo de n�
     * na �rvore de avalia��o. Sua utiliza��o tem como objetivo a adi��o de
     * novos tipos de n�s sem que seja feita altera��o no Parser(classe
     * PredicateParser).
     */
    public abstract String getParserRepresentation()
            throws PredicateEvaluatorException;

}