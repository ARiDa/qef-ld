/*
* CoDIMS version 1.0 
* Copyright (C) 2006 Othman Tajmouati
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package ch.epfl.codimsd.qeef.relational;

import ch.epfl.codimsd.qeef.discovery.datasource.RelationalDataSource;
import ch.epfl.codimsd.qeef.types.Type;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.Serializable;
import java.io.Writer;
import java.util.*;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;
//import vtk.vtkDirectory;

/**
 * Classe que implementa a defini��o de uma tupla do modelo relacional.<p>
 * Uma tupla � composta de um conjunto de valores que est�o associados cada um a uma coluna definida 
 * nos metadados desta tupla.<p>
 * Os valores devem estar disposttos na mesma ordem em as colunad foram definidas no metadado.
 * 
 * 'The values must be disposed in the same order defined  in metadata'
 * 'One tuple is composed of a set of values that are associates each one to a definite column * in metadata of this tuple' 
 * 
 * @author Fausto Ayres, Vinicius Fontes
 */
@SuppressWarnings("serial")
public class Tuple extends Instance implements Serializable {
    

	/**
	 * Log4j Logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RelationalDataSource.class.getName());
	
	
    /**
     * Lista com os valores de cada coluna.
     * Os valores devem estar ordenados pela posi��o da respectiva coluna nos metadados.
     * 
     * List with the values of each column
     * The values must be commanded for the position of the respective column in the metadata ones
     * 
     */
	protected LinkedList<Type> values;

	/**
	 * Construtor padrão. 
	 * 
	 * Construction standard 
	 * 
	 */
	public Tuple() {
		super();                                
		values = new LinkedList<Type>();
	}
	
	/**
	 * Retorna uma cole��o com os valores de cada coluna.<p> 
	 * Valores encontram-se ordenados segundo metadados da tupla.
	 * @return Valores das colunas.
	 * 
	 * It returns a collection with the values from each column
	 * Values meet commanded according to metadata of the tuple
	 * 
	 */
	public Collection<Type> getValues() {
		
		return values;
	}

	/**
	 * Atribui valores as colunas desta tupla.<p>
	 * Os valores manter�o a ordem original.
	 * 
	 * Tuple attributes to values the columns of this.  The values will keep the original order 
	 * 
	 * @param values Valores a serem atribu�dos as colunas. Values to be attributed the columns 
	 */
	public void setData(Collection<Type> values){
	    this.values = new LinkedList<Type>(values);
	}
	
	/**
	 * Atribui valor a primeira coluna que não possuir valor. 
	 * It attributes to value the first column that do not possess a value. 
	 * @param value Valor a ser atrubuído a prrimeira coluna sem valor. 
	 */
	public void addData(Type value) {
	    values.add(value);
	}
	
	/**
	 * Obtem o valor da coluna que ocupa a n-ésima posição.
	 * @param nth Posição ocupada pela coluna que se deseja saber o valor.
	 * @return Valor da coluna desejada.
	 */
	public Type getData(int nth) {

		return (Type)values.get(nth);
	}
	
	/**
	 * Remove o valor da coluna que ocupa a n-�sima posi��o.
	 * @param nth Posi��o ocupada pela coluna que se deseja remover seu valor.
	 */
	public void removeData(int nth) {
	    values.remove(nth);
	}
	
    /**
     * Modifica o valor da coluna que ocupa a n-�sima posi��o.
     * @param newData Novo valor.
     * @param nth Posi��o ocupada pela coluna.
     */
    public void updateData(Type newData, int nth) {
        values.set(nth, newData);
    }

	/**
	 * Cria uma nova tupla que ser� o resultado da jun��o de left com right.
	 * It creates a new tuple that will be the result of the junction of left with right 
	 * 
	 * @param left Tupla cujos valores ocuparam as primeiras posi��es.
	 * @param right Tupla cujos valores ocuparam as �ltimas posi��es. 
	 * 
	 * @return Tupla resultante da jun��o de left com right.
	 */
	public Instance join(Instance left, Instance right) {

		Tuple tuple = new Tuple();
		Iterator<Type> itDados;
		Type curr;
		
		//set new data from t1
		itDados = ((Tuple)left).values.iterator();

		while(itDados.hasNext()){
			curr = (Type)itDados.next();
			tuple.addData( curr==null?null:(Type)curr.clone() );
		}
		
		//set new data from t2
		itDados = ((Tuple)right).values.iterator();
		while(itDados.hasNext()){
			curr = (Type)itDados.next();
			tuple.addData( curr==null?null:(Type)curr.clone() );
		}
				
		//set the new properties from t1
		tuple.properties = (Properties)left.getProperty().clone();

		//set the new properties from t2
		tuple.properties.putAll( (Properties)right.getProperty().clone() );
		
		return tuple;		
	}

	
    /**
     * Define como uma tupla deve ser serializada. Optou-se por n�o usar a
     * implementa��o default por quest�es de performance e ocupa��o de mem�ria.
     * A implementa��o default mant�m em mem�ria os objetos serializados o que 
     * gera erros se o volume de dados manipulado for muito grande.
     * 
     * It defines if one tuple must be serialized.  
     * It was opted to not using the default implementation 
     * for performance reasons and memory occupation.  
     * The default implementation keeps in memory serialized objects 
     * what it generates errors if the volume of data manipulated will be very large. 
     * 
     * @param Fluxo para onde se deseja serializar o dados. for where if it desires to serializar the data 
     * @param metadata Metadados da tupla a ser serializada.
     * 
     * @throws IOException Se acontecer algum problema durante grava��o dos dados no fluxo.
     * 
     * @see java.io.ObjectOutputStream#writeObject(java.lang.Object)
     */
    public void write(DataOutputStream out, Metadata metadata)
            throws IOException {
       
        Type value;
        int size;

        size = metadata.size();
        for(int i=0; i<size; i++){
            
            value = (Type)values.get(i);
            
            Data data = metadata.getData(i);
            Column column = (Column) data;
            value.setMetadata(column);
            
            value.write(out);
        }
        
        super.write(out, metadata);
    }

    /** 
     * Define como uma inst�ncia deve ser serializada. Optou-se por n�o usar a
     * implementa��o default por quest�es de performance e ocupa��o de mem�ria.
     * A implementa��o default mant�m em mem�ria os objetos serializados o que 
     * gera erros se o volume de dados manipulado for muito grande.
     * 
     * @param Fluxo no qual a tupla se encontra serializada.
     * @param metadata Metadados da tupla a ser deserializada.
     *
     * @throws IOException Se acontecer algum problema durante leitura dos dados no fluxo.
     * @throws EOFException Se chegou ao final do fluxo antes que a tupla fosse completamente deserializada.
     *  
     * @see ObjectInput#readObject()
     */
    public void read(java.io.DataInputStream in, Metadata metadata)
            throws IOException, EOFException {        
        
        int size;
        Type value;

        size = metadata.size();
        for(int i=0; i<size; i++){
            value = ((Column)metadata.getData(i)).type;
            values.add(value.read(in));
        }

        super.read(in, metadata);
    }

    public void readSmooth(String in, Metadata metadata)
            throws IOException, EOFException {

        int size;
        Type value;

        size = metadata.size();

        for(int i=0; i<size; i++){

            value = ((Column)metadata.getData(i)).type;
            values.add(value.readSmooth(in));
        }

        super.readSmooth(in, metadata);
    }

    /*public void readImages(vtkDirectory in, Metadata metadata, String dir, int nrOfDivisions)
            throws IOException, EOFException {

        int size;
        Type value;

        size = metadata.size();

        for(int i=0; i<size; i++){

            value = ((Column)metadata.getData(i)).type;
            values.add(value.readImages(in, dir, nrOfDivisions));
        }

        super.readImages(in, metadata);
    }*/

    /**
     * Libera as refer�ncias feitas por esta tupla. 
     * the tuple liberates the references made for this. 
     */
    public void finalize(){
        this.values.clear();
        this.values = null;
    }
    
	/**
     * Cria uma nova tupla id�ntica a esta. It creates new tupla identical to this
     * 
     * @return Nova inst�ncia.
     * 
     * @see Object#clone()
     */
	public Object clone() {

		Tuple newTuple = new Tuple();

		//clona dados
	    Type currValue;
	    for(int i=0; i < values.size(); i++){
	        
	        currValue = (Type)values.get(i); 
	        newTuple.addData( currValue==null?null:(Type)currValue.clone() );
	    }

		
		//Insere properties
		Enumeration<Object> enumeration;
		enumeration = properties.keys();
		String key;
		while (enumeration.hasMoreElements()) {
			key = (String) enumeration.nextElement();
			newTuple.setProperty(key, this.getProperty(key));
		}

		return newTuple;
	}

	/**
	 * Grava os valores das colunas desta tupla no fluxo out para serem visualizados.
	 * 
	 * The tuple in the flow records the values of the columns of this out to be visualized. 
	 * 
	 * @param out Fluxo para onde os valores ser�o gravados. Flow for where the values will be recorded 
	 * @throws IOException Se algum erro acontecer durante a gra��o dos valores.
	 */
	public void display(Writer out) throws IOException{

	    for(int i=0; i < values.size(); i++){
	    	Type value = (Type)values.get(i);
	        if (value == null) {
	        	out.write("null ");
	        } else {
	        	value.display(out);
	        }
	    }

	    out.write('\n');
	}
	
	/**
	 * 
	 */
	public String toString(){
	    
	    String out = "";

	    for(int i=0; i < values.size(); i++){
	    	Type value = (Type)values.get(i);
	        if (value == null) {
	        	out += "null ; ";
	        } else {
	        	out += value.toString() + " ; ";
	        }
	    }
	    
	    return out;
	}


}

