package ch.epfl.codimsd.qeef.trajectory.algebraic;

import java.util.Iterator;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.datastructure.Buffer;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateParser;
import ch.epfl.codimsd.qeef.relational.Column;
import ch.epfl.codimsd.qeef.types.PointListType;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qep.OpNode;

// Outer Relation - producers[0]
// Inner Relation - producers[1]

public class Fold extends Operator {

	//==========================================================================================
	//Declara��o de Vari�veis
	//==========================================================================================
	private ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Predicate predicate;
	private String sameColumns[];
	private String differentColumns[];
	private int differentColumnsOrder[];
	private Buffer buffer;
	private int occurrence;
	private int bufferSize;
	
	private Type newType[];
	
	//==========================================================================================
	// Construtores e fun��es utilizadas por ele
	//==========================================================================================

	//----------------------------------------------------------------------------------------
	//----------------------------------------------------------------------------------------
	public Fold(int id, OpNode opNode) throws Exception{
		super(id);

                String prmSameCol, prmDifCol, prmType;

                prmSameCol = opNode.getParams()[0].trim().toUpperCase();
                prmDifCol = opNode.getParams()[1].trim().toUpperCase();
                prmType = opNode.getParams()[4].trim().toUpperCase();

                this.sameColumns = prmSameCol.split(",");
		this.differentColumns = prmDifCol.split(",");
		this.occurrence = Integer.parseInt(opNode.getParams()[2].trim());
		this.bufferSize = Integer.parseInt(opNode.getParams()[3].trim());

                String type[] = prmType.split(",");
                newType = new Type[type.length];
                for(int i = 0; i < type.length; i++)
                    this.newType[i] = Config.getDataType(type[i]);
	}
	//=========================================================================================
	//Overrrided functions
	//=========================================================================================
	
	//-----------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------
	public void open() throws Exception{

		super.open();

		//Inicializa algumas variaveis
		buffer = new Buffer(bufferSize); 

		//cria predicado
		String strPredicate;
		strPredicate = sameColumns[0] + " = " + sameColumns[0];
		for (int i = 1; i < sameColumns.length; i++) {
			 strPredicate += " AND " + sameColumns[i] + " = " + sameColumns[i]; 
		}

		PredicateParser parser = new PredicateParser();
		predicate = parser.parse(strPredicate, metadata[0], metadata[0]);

	}
	
    public void setMetadata(Metadata prdMetadata[]){
        //obtem metadados
        Column data;        
        this.metadata[0] = (Metadata)prdMetadata[0].clone();
        	        
        //Seta metadados deste operador
		differentColumnsOrder = new int[differentColumns.length];
        for(int i=0; i < differentColumns.length; i++){
	        differentColumnsOrder[i] = metadata[0].getDataOrder(differentColumns[i]);
	        data = (Column)metadata[0].getData(differentColumnsOrder[i]);	        
	        data.setType(newType[i]); //Atualiza tipo	        
	        metadata[0].setData(data, differentColumnsOrder[i]);
        }
    }

	public void close() throws Exception{
		super.close();
		buffer = null;
	}

	//-----------------------------------------------------------------------------------------
	//-----------------------------------------------------------------------------------------
	public DataUnit getNext(int consumerId) throws Exception{

          //  System.out.println("FOLDNext = " + id + "consumerId"+ consumerId);
	    Instance instance = null;
	    
		while( hasNext && instance == null){
				    
		    instance = (Instance)super.getNext(id);
		    
		    if( instance != null){
		        instance = fold(instance);
		        
		    } else{
		        System.out.println("Fold(" + id + "): recebeu null");
		    }
		}

	    return instance;
	}


	//==========================================================================================
	//Other Functions
	//=========================================================================================
	private Instance fold(Instance instance) throws Exception{
		
		Iterator iterator;
		Instance aux;
		PointListType list;
		Type value;
		Integer instanceOccurrence;
		boolean match;
		
		if (buffer.isEmpty()){

			buffer.add( changeFirstInstance(instance, differentColumns) );
			return null;
		}
		
		//Procura por tuplas para realizar fold
		match = false;
		iterator = buffer.iterator(); 
		while(iterator.hasNext()){
			aux = (Instance)iterator.next();
			
			//achou correspondente
			if(predicate.evaluate(instance, aux)){
				
				//Fundi as tuplas
			    //System.out.println("Fold(" + id + "): fundiu " + instance + " com " + aux);
				aux = mergeInstances(aux, instance, differentColumns);
				
				//Aumenta nr de vezes que essa tuplas realizou fold
				instanceOccurrence = new Integer( aux.getProperty("OCURRENCE") );
				instanceOccurrence = new Integer(instanceOccurrence.intValue() + 1);
				
				aux.setProperty("OCURRENCE", instanceOccurrence.toString());
				match = true;
				
				//se completou nr de tuplas que formarao uma retirna tupla
				if(instanceOccurrence.intValue() == occurrence){
					aux.removeProperty("OCURRENCE");
					buffer.remove(aux);

					return aux;
				}

				break;
			}
		}
		
		//Se tupla n�o realizou nenhum match
		//Ela � a primeira da serie
		if (match==false){
		    if(buffer.isFull())
		        throw new Exception("Fold(" + id + "): Exception Fold buffer is full");
			buffer.add( changeFirstInstance(instance, differentColumns) );
		}
			
		return null;
	}
	
	/*
	 * modifiedTuple - tupla com cole��o
	 * newTuple - tupla com novos valores a serem inseridos na tupla com cole��o
	 */
	private Instance mergeInstances(Instance modifiedInstance, Instance newInstance,  String differentColumns[]) throws Exception{
	    
		String columnName;
		PointListType list;
		
		for(int i=0; i<differentColumns.length; i++){
			
			list = (PointListType)modifiedInstance.getData(differentColumnsOrder[i]);
			list.add( newInstance.getData(differentColumnsOrder[i]) );
		}
		
		return modifiedInstance;
	}
	
	//Altera a primeira tupla da sequencia
	//instancia elements nesta tupla
	private Instance changeFirstInstance(Instance instance, String differentColumns[]){
		
		String columnName;
		PointListType list;
		Instance aux;
		Type singleValue;
		
		//Clona tupla, ela pode estar sendo utilizado por outros operadores
		aux = (Instance)instance.clone();

		//Cria elements e insere valor do 1 elemento na elements
		for(int i=0; i < differentColumns.length; i++){
			
		    //Obtem valor para colocar na elements
		    singleValue =  instance.getData(differentColumnsOrder[i]);
		    
		    //Cria tipo cole��o e insere singlevalue na colecao 
			list = instantiateList();
			list.add(singleValue);
			
			//Altera de aux para elements
			aux.updateData(list, differentColumnsOrder[i]);
		}
		
		//Cria propriedade que contara qtas tuplas se uniram a esta
		aux.setProperty("OCURRENCE", (new Integer("1")).toString());
		
		return aux;
	}
	
	private PointListType instantiateList(){
		return new PointListType(occurrence);
	}
}
