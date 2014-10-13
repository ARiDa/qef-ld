package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.datanodes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.IndexedData;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;
import ch.epfl.codimsd.qeef.types.IntegerType;

/**
 * Representa um n� da �rvore de avalia��o capaz de obter o 
 * valor de uma coluna em uma tupla. Dado referenciado na forma "Tabela.Dado".
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 01 FEV 2005
 */

public class ColumnNode extends IndexedData{

	/**
	 * 
	 */
	public ColumnNode(){
	    super();
	}
	
    /**
     * @param name Nome da coluna a ser acessada. Na forma Tabela.Coluna, ou seja, o caminho absoluto.
     *
     * @param metadata
     *            Metadados da inst�ncia que este n� vai receber.
     * 
     * @throws PredicateEvaluatorException Se a coluna n�o existir.
     */
	public ColumnNode(String name, Metadata metadata) throws PredicateEvaluatorException{
		super();
		//this.dataName   = name.toUpperCase();
                
		this.dataPos = metadata.getDataOrder(name);
		if(dataPos == -1)
		    throw new PredicateEvaluatorException("ColumnNode: Invalid Column: " + name);
	}

	/**
	 * Cria uma nova inst�ncia de QEEF.predicate.ColumnNode.
	 *
	 * @throws PredicateEvaluatorException Se a coluna referenciada n�o existir.
	 * 
	 * @see IndexedData#newInstance(String navigationPath, Metadata instanceMetadata)
	 */
	public IndexedData newInstance(String columnName, Metadata instanceMetadata) throws PredicateEvaluatorException{
	    return new ColumnNode( columnName, instanceMetadata);
	}

	/**
	 * @see QEEF.predicate.evaluator.Node#getParserRepresentation()
	 */
	public  String getParserRepresentation(){
		
		return "(("+ NAME_PATTERN + "[.]" + NAME_PATTERN + ")(?![.a-zA-Z_]))";		
	}
	
    /**
     * Obtem o valor de uma coluna que est� em uma inst�ncia.
     * 
     * @param instance Inst�ncia a partir do qual o dado vai ser obtido.
     * 
     * @return O valor do dado referenciado.
     * @see IndexedData#apply(Object leftValue, Object rightValue)
     */
	public Object apply(Instance instance){
             //   if(id == 6)
               //     System.out.println("ColumnNode instance = " + instance + " dataPos = " + dataPos + " matadata = " +(instance).getData(dataPos));
		return (instance).getData(dataPos);
	}

    /**
     *  @see Object#toString()
     */
	public String toString(){
	    return "ColumnNode Column Number " + dataPos + " Column Name " + dataName;
	}

	
    public static void main(String[] args) {

        ColumnNode e = new ColumnNode();
        Pattern p = Pattern.compile(e.getParserRepresentation()); 
        Matcher m = p.matcher("PARTICULA.Ponto.c");
        
        System.out.println(IntegerType.INTEGER_PATTERN);
        while( m.find() )
        	System.out.println( m.group() );

    }
}
