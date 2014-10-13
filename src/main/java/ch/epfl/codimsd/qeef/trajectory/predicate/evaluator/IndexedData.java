package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator;

import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;

/**
 * Define funcionalidades b�sicas necess�rias para acesso a dados indexados pelo nome como o valor de uma coluna ou rpopriedade de um objeto.
 *
 * @author Vinicius Fontes
 * 
 * @date Jun 4, 2005
 */
public abstract class IndexedData extends DataNode {

    /**
     * Node de uma coluna de uma tupla a ser acessada.
     */
	protected String dataName;
	
	/**
	 * Posi��o ocupado por esta coluna na tupla.
	 */
	protected int dataPos;
    
    /**
     * 
     */
    public IndexedData(){
        super();
    }
    
    /**
     * 
     * Cria uma nova inst�ncia desta classe.
     * 
     * @return Nova inst�ncia deste operador.
     * 
     * @throws PredicateEvaluatorException Se ocorrer algum erro durante a instancia��o.
     */
	public abstract IndexedData newInstance(String navigationPath, Metadata instMetadata) throws PredicateEvaluatorException;
	
	
	/**
	 * @see Node#evaluate(DataUnit , DataUnit )
	 */
	public Object evaluate(Instance leftInstance, Instance rightInstance) throws PredicateEvaluatorException{
	    
		//Verifica se este operador � o filho esquerdo o direito em rela��o a seu pai
	    //depois invoca metodo para recupera��o do valor deste objeto

        if (parent.leftChild.equals(this))
            return apply(leftInstance);
        else
            return apply(rightInstance);

    }

	
    /**
     * Define uma forma de acesso a um dado em uma Inst�ncia. Pode encapsular
     * o acesso a um dado, a uma propriedade de um objeto armazenado em uma coluna,
     * ou at� mesmo a execu��o de uma fun��o.
     * 
     * @param instance Inst�ncia a partir da qual os dados v�o ser extraidos.
     * 
     * @return O valor na Inst�ncia do dado desejado.
     * 
     * @throws PredicateEvaluatorException
     *             Se ocorrer algum erro durante o acesso ao valor especificado.
     */
    protected abstract Object apply(Instance instance)  throws PredicateEvaluatorException;

    
    

}
