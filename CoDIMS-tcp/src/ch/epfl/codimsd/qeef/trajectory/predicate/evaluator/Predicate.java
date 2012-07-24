package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator;


import ch.epfl.codimsd.qeef.Instance;
/**
 * Representa um predicado que poder� ser avaliado.
 * Encapsula uma �rvore de avalia��o.
 *
 * @author Vin�cius Fontes Vieira da Silva
 *
 * @date 01 FEV 2005
 */

public class Predicate {

    /**
     * @clientCardinality 1
     * @directed true
     * @label root
     * @link aggregation
     * @supplierCardinality 0..1
     */

    /**
     * Raiz da �rvore de avalia��o.
     */
    private Node root;

    public Predicate(Node root) {
        this.root = root;
    }

    /**
     *
     * Realiza a avalia��o de uma express�o de predicados com base nas inst�ncias passadas como par�metro.
     * Obs: Expres�o deve ser escrita de forma que os dados refer�nciados de uma inst�ncia estejam sempre a
     * Esquerda ou a direita nos predicados.
     *
     * @param t1 inst�ncia referenciada pelo lado esquerdo nos predicados da express�o
     * @param t2 inst�ncia referenciada pelo lado direito  nos predicados da express�o
     */
    public boolean evaluate(Instance left, Instance right)
            throws PredicateEvaluatorException {
        
        if (root != null)
            return ((Boolean) root.evaluate(left, right)).booleanValue();
        else
            return true;
    }

    /**
     * Obtem a raiz da �rvore de avalia��o.
     *
     * @return Retorna a raiz da �rvore de avalia��o.
     */
    public Node getRootOperator() {
        return root;
    }
    
    public void print(){
        print(root, 0);
    }
    
    private void print(Node node, int space){
        if( node!=null ) {
            printSpace(space);
	        System.out.println(node); 
	        
	        print(node.leftChild, space+4);
	        print(node.rightChild, space+4);	        
        }        
    }
    
    private void printSpace(int i){
        for(int j=0; j<i; j++)
            System.out.print(" ");
    }
}