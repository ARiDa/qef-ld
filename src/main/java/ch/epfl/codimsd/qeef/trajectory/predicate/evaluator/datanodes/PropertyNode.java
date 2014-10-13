package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.datanodes;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.types.Point;
import ch.epfl.codimsd.qeef.util.Util;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.IndexedData;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;

/**
 * Representa uma folha na �rvore de avalia��o capaz de acessar a propriedade de um objeto que est� em um dado de uma inst�ncia.
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 01 FEV 2005
 */

public class PropertyNode extends IndexedData {

    /**
     * Caminho de navega��o at� uma propriedade de um objeto armazenado em um dado de uma inst�ncia.
     */
    protected String propertyName[];

	/**
	 * 
	 */
	public PropertyNode(){
	    
	}
	
    /**
     * @param navigationPath
     *            nome do dado a ser acessado por este n�. A representa��o
     *            utilizada �, "Tabela.Dados([.]Relacionamento)*.Propriedade" .
     *            Caminho de navega��o definido na OQL.
     * @param metadata
     *            Metadados da inst�ncia que este n� vai receber
     * 
     * @throws PredicateEvaluatorException Se a coluna ou propriedade referenciada n�o existir.
     */
    public PropertyNode(String navigationPath, Metadata metadata) throws PredicateEvaluatorException{

        super();
        
        String names[], columnName;
        names = navigationPath.split("[.]");

        //Obtem a posicao da dado para melhorar o desempenho no acesso ao dado
        columnName = names[0].toUpperCase() + "." + names[1].toUpperCase();
        dataPos = metadata.getDataOrder(columnName);
        
		if(dataPos == -1)
		    throw new PredicateEvaluatorException("ColumnNode: Invalid Column: " + columnName);

        //Cria uma elements de properties a serem acessadas a partir
        //do caminho informado at� esta propriedade
        propertyName = new String[names.length - 2];
        for (int i = 0; i < propertyName.length; i++) {
            propertyName[i] = names[i + 2];
        }

    }
    
	/**
	 * Cria uma nova inst�ncia de QEEF.predicate.PropertyNode
	 * @throws PredicateEvaluatorException Se a coluna ou propriedade referenciada n�o existir.
	 * @see IndexedData#newInstance(String, Metadata)
	 */
	public IndexedData newInstance(String navigationPath, Metadata instanceMetadata) throws PredicateEvaluatorException{
	    return new PropertyNode( navigationPath, instanceMetadata);
	}

	/**
	 * @see QEEF.predicate.evaluator.Node#getParserRepresentation() 
	 */
    public String getParserRepresentation() {

        return "(" + NAME_PATTERN + "([.]" + NAME_PATTERN + "){2,})";
    }

    /**
     * Obtem o valor de uma propriedade de um objeto que est� em um dado da inst�ncia.
     * Esta implementa��o deve ser corrigida para outros problemas. Ver codigo comentado.
     * 
     * @param instance Inst�ncia a partir do qual o dado vai ser obtido.
     * 
     * @return O valor da propriedade referenciada encapsulado em um objeto do tipo Type.

     * @see QEEF.predicate.evaluator.IndexedData#apply(DataUnit)
     * 
     */
    public Object apply(Instance instance) throws PredicateEvaluatorException {

        long i = System.currentTimeMillis();
        Object obj = null;
        
        obj = (instance).getData(dataPos);

        //Acessa propriedade
//        try {
            Class objClass;
            Field field;

            //Navega pelos relacionamentos at� chegar na propriedade desejada
            for (int j = 0; j < propertyName.length; j++) {
        
//				objClass = obj.getClass();
//				field = objClass.getDeclaredField(propertyName[j]);
//				obj = field.get(obj);
			    obj = ((Point)obj).get(propertyName[j]);
            }

            //Converte, se necess�rio, o valor da propriedade acessada
            //para um valor recponhecido pela m�quina. Utilizada qdo for
            // acessar
            //um atributo de um obj que � de um tipo primitivo.
            //return Util.ConvertToDataType(obj);
            return obj;

//        } catch (NoSuchFieldException nsfexc) {
//            nsfexc.printStackTrace();
//            return null;
//
//        } catch (IllegalAccessException iaexc) {
//            iaexc.printStackTrace();
//            return null;
//        }

    }
    
    /**
     *  @see Object#toString()
     */
	public String toString(){
	    String aux;
	    
	    if(propertyName ==  null){
	        aux = "PropertyNode --";
	    } else {
		    aux= "PropertyNode data Number " + dataPos + " data Name " + dataName;
	
		    for(int i=0; i < propertyName.length; i++)
		        aux += propertyName[i] + ".";
	    }
	    return aux;
	}
	
	
    public static void main(String[] args) {

        PropertyNode e = new PropertyNode();
        Pattern p = Pattern.compile(e.getParserRepresentation()); 
        Matcher m = p.matcher("PARTICULA.POnto.c.d");
        
        System.out.println(e.getParserRepresentation());
        while( m.find() )
        	System.out.println( m.group() );

    }
}
