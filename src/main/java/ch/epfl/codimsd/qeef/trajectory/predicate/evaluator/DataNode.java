package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator;

/**
 * Classe abstrata que representa uma folha n� �rvore de avalia��o.
 * As sub-classes dessa classe implementar�o formas de se acessar um 
 * dado em uma inst�ncia.
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 01 FEV 2005
 */
public abstract class DataNode extends Node {
    
    /**
     * Define o que pode ser um nome de um dado. Deve come�ar por letra, e pode ter letra
     * ou numero ou _  em seguida.
     */
    public static final String NAME_PATTERN = "[a-zA-Z_][a-zA-Z_1-9]*";
        
    /**
     * 
     */
	public DataNode(){
		super();
	}			
}

