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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Iterator;

import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.Metadata;

import ch.epfl.codimsd.qeef.types.IntegerType;

/**
 * Define o formato de uma tupla do modelo relacional.<p> 
 * Uma tupla � definida a partir de um
 * um conjunto de colunas que ocupam posi��o fixa.<p>
 * 
 * Tuple of the relationary model defines the one format.  
 * One tuple is defined from one set of columns that occupy a fix position.
 * 
 * @author Vinicius Fontes
 * 
 * @date Mar 12, 2005
 */
@SuppressWarnings("serial")
public class TupleMetadata implements Metadata, Serializable{

    /**
     * Lista de colunas.
     */
    protected LinkedList<Data> data;
    
    
    /**
     *  Construtor padr�o.
     */
    public TupleMetadata() {
        super();

        data = new LinkedList<Data>();
    }

    /**
     * Obtem o dado que descrve a coluna que possui este nome.
     * It gets the data that describe the column that possesss this name. 
     * 
     * @param name Nome da coluna.
     * @return Descri��o desa coluna. 
     */
    public Data getData(String name) {

        Iterator<Data> itAttr;
        Column attr;

        itAttr = data.iterator();
        while (itAttr.hasNext()) {

            attr = (Column) itAttr.next();
            if (attr.getName().equalsIgnoreCase(name))
                return attr;
        }
        return null;
    }

    /**
     * Obtem o dado que descrve a coluna que ocula a n-�sima posi��o.
     * It gets the data that describe the column that ocuppies the n-th position. 
     * 
     * @param name Posi��o da coluna.
     * @return Descri��o desa coluna. 
     */
    public Data getData(int order) {

        return (Column) data.get(order);
    }

    /**
     * Obtem o conjunto de colunas que descrevem uma tupla.
     * It gets the set of columns that describe one tupla. 
     * 
     * @return Conjunto ed colunas que descreve uma tupla. 
     */
    public Collection<Data> getData() {
        return data;
    }

    /**
     * Obtem o n�mero de columnas desta tupla.
     * @return N�mero de colunas.
     */
    public int size() {
        return data.size();
    }
    
    /**
     * Substitui a descri��o das colunas de uma tupla por este conjunto de colunas.
     * Tuple for this set of columns substitutes the description of the columns. 
     * 
     * @param data Conjunto de colunas.
     */
    public void setData(Collection<Data> data){
        this.data = new LinkedList<Data>(data);
    }
    

    /**
     * Substitui a descri��o da coluna que ocupa a n-�sima posi��o.
     * It substitutes the description of the column that occupies the n-th position. 
     * 
     * @param newData Nova descri��o da coluna.
     * @param nth Posi��o da coluna a ser substituida. 
     */
    public void setData(Data newData, int nth) {

        Column previous;
        Column currAttr;

        data.set(nth, newData);
        previous = (Column) newData;

        ListIterator<Data> itAttrs = data.listIterator(nth + 1);
        while (itAttrs.hasNext()) {

            currAttr = (Column) itAttrs.next();

            currAttr.setPosition(previous.getPosition() + previous.getSize());

            previous = currAttr;
        }
    }

    /**
     * Retorna a posi��o da coluna com nome name.
     * It returns the position from the column with name name. 
     * 
     * @param Nome da coluna.
     * @return Posi��o ocupada. 
     */
    public int getDataOrder(String name) {
        
        for (int i = 0; i < data.size(); i++) {
            if (((Column) data.get(i)).getName().equalsIgnoreCase(name))
                return i;
        }

        return -1;
    }

    /**
     * Adiciona uma nova coluna.
     * @param attr Nova coluna. 
     */
    public void addData(Data attr) {

        Column last;
        int pos;

        pos = data.size() - 1;

        if (pos >= 1) {
            last = (Column) data.get(pos);

            ((Column) attr).setPosition(last.getPosition() + last.getSize());
            //((Column)attr).setOrder(last.getOrder() + 1);

        } else {
            ((Column) attr).setPosition(0);
        }

        data.add(attr);
    }

    /**
     * Remove a coluna que ocupa a nth posi��o.
     * @param nth Posi��o da coluna que se deseja remover. 
     */
    public void removeData(int nth){
        
        Column currAttr, previousAttr;

        if( data.size() > nth + 1 ) {

            ListIterator<Data> itAttrs = data.listIterator( nth + 1 );
            while( itAttrs.hasNext() ){
                
                currAttr = (Column)itAttrs.next();
                
                if( nth == 0){
                    currAttr.setPosition(0);
                
                } else {
            
                    previousAttr = (Column)itAttrs.previous();
                    itAttrs.next();
	            currAttr.setPosition( previousAttr.getPosition() + previousAttr.getSize() );
                }
            }
        }

        data.remove(nth);
    }
    
    /**
     * Remove a coluna que ocupa a nth posi��o.
     * @param nth Nome da coluna que se deseja remover. 
     */
    public void removeData(String name) {

        int order = getDataOrder(name);
        removeData(order);
    }

    /**
     * Adiciona estas novas colunas ao final da lista de colunas.
     * @param newAttributes Novas colunas. 
     */
    public void join(Collection<Data> newAttributes) {

        Iterator<Data> itAttrs;
        Column currAttr;

        itAttrs = newAttributes.iterator();
        while (itAttrs.hasNext()) {

            currAttr = (Column) itAttrs.next();
            addData(currAttr);
        }
    }

    /**
     * Grava o nome destas colunas de maneira formatada no fluxo out.
     * @param out Fluxo onde se gravar� o nome.
     * @throws IOException Sea contecer algum erro durante a grava��o no fluxo. 
     */
    public void displayNames(Writer out) throws IOException {

        String currAttrName;

        for (int i = 0; i < data.size(); i++) {

            currAttrName = ((Column) data.get(i)).getName();

            //Insere espa�os em branco necessarios
            //para formatar resultado
            for (int j = currAttrName.length(); j < ((Column) data
                    .get(i)).getType().displayWidth(); j++)
                currAttrName += " ";
            
            out.write(currAttrName);
        }
        out.write("\n");
    }

    /**
     * Cria uma c�pia deste metadado. Suas colunas tamb�m ser�o clonadas.
     * @return Novo metadado id�ntico a este.
     */
    public Object clone() {

        TupleMetadata tm = new TupleMetadata();
        Column currAttribute;

        for (int i = 0; i < data.size(); i++) {

            currAttribute = (Column) data.get(i);
            tm.addData((Column) currAttribute.clone());
        }

        return tm;
    }

//    /**
//     * Redefine o processo de serializa��o implementado na linguagem.
//     * 
//     * @param out Fluxo no qual metadado ser� serializado. 
//     * @throws IOException Se acontecer algum problema durante a grava��o do metadado.
//     */
//    public void writeExternal(ObjectOutput out) throws IOException {
//
//        Data currData;
//        Iterator it;
//
//        //Grava o tamanho
//        out.writeInt(data.size());
//
//        //Grava properties
//        it = data.iterator();
//        while (it.hasNext()) {
//
//            currData = (Data) it.next();
//            out.writeObject(currData);
//        }
//    }
//
//    /**
//     * Redefine o processo de deserializa��o implementado na linguagem.
//     * 
//     * @param in Fluxo de leitura onde o metadodo est� serializado. 
//     * @throws IOException Se acontecer algum problema durante a leitura.
//     */
//    public void readExternal(ObjectInput in) throws IOException {
//
//        Column currData;
//        int size;
//
//        //Grava o tamanho
//        size = in.readInt();
//
//        //Grava properties
//        for (; size > 0; size--) {
//            try {
//                data.add((Data) in.readObject());
//            } catch (ClassNotFoundException cnfexc) {
//                throw new IOException(cnfexc.getMessage());
//            }
//        }
//
//    }

    
	/**
	 * Retorna o tamanho em bytes das tuplas descritas por este metadado.
	 * It returns the size in bytes from tuples described for this metadado. 
	 * 
	 * @return Tamanho em bytes.
	 */
	public long instanceSize(){
	    
	    Iterator<Data> itAttr = data.iterator();
	    long size = 0;
	    
	    while( itAttr.hasNext() ){
	        
	        size += ((Column)itAttr.next()).size;
	    }
	    
	    return size;
	}
	
	
    public String toString(){
        
        String aux="[";
        
        for (int i=0; i < data.size(); i++) {
            Column element = (Column) data.get(i);
            
            aux = aux + "\n\t\t" + element;            
        }
        
        return aux + "\n\t\t]";
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (! (obj instanceof TupleMetadata)) {
    		return false;
    	}
    	Metadata metadata = (Metadata) obj;
    	if (this.data.size() == metadata.size()) {
    		for (int i=0; i < this.data.size(); i++) {
    			if (! this.getData(i).equals(metadata.getData(i))) {
    				return false;
    			}
    		}
    		return true;
    	}
    	return false;
    }

    public static void main(String[] args) throws Exception {

        Column a = new Column("nome", new IntegerType(), 2, 3, true);
        TupleMetadata tm = new TupleMetadata();
        tm.addData(a);

        Metadata m = tm;

        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
                "/home/douglas/dados/teste.txt"));
        out.writeObject(m);
        out.close();

       
    }

}

