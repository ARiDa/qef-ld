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
package ch.epfl.codimsd.qeef;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Collection;

/**
 * Define o formato de uma unidade de dados/fonte de dados a ser procesada pela
 * máquina de execução. Um metadado deve ser composto por um conjunto de dados
 * que descreve cada atributo de uma unidade de dados. No modelo relacional por
 * exemplo, um metadado descreveria o formato de um conjunto de tuplas e um dado
 * descreveria uma de suas colunas.
 * <p>
 * Os dados presentes em um metadado podem ser indexados por nome ou posição que
 * ocupa na unidade de dados.
 * 
 * Source of data defines the format of a unit of data to be processed by the 
 * machine execution.  Metadada must be composed of a data set that each attribute 
 * of a unit of data describes.  In the relationary model for example, metadada would 
 * describe the format of a set of tuples and data would describe one of its columns.
 * 
 * The data present in metadada can be indexed by name or position that occupies in 
 * the unit of data. 
 * 
 * @see ch.epfl.codimsd.qeef.Data
 * 
 * @author Vinicius Fontes
 */
public interface Metadata extends Serializable {

    /**
     * Obtem deste metadado que tem o nome name.
     * 
     * @param name Nome do dado.
     * 
     * @return Dado com nome name.
     */
    public abstract Data getData(String name);

    /**
     * Obtem deste metadado que ocupa a n-ésima posição.
     * @param nth Posição do dado.
     * @return Dado que ocupa a nth posição.
     */
    public abstract Data getData(int nth);

    /**
     * Obtem a coleção de dados presente neste metadado.
     * @return coleção de dados.
     */
    public abstract Collection<Data> getData();

    /**
     * Obtem o número de dados deste metadado.
     * @return Número de dados.
     */
    public abstract int size();

    /**
     * Substitui o valor dos dados deste metadado por esta coleção de dados.
     * @param  data Dados a serem inseridos.
     */
    public abstract void setData(Collection<Data> data);

    /**
     * Substitui o valor dado que ocupa a n-ésima posição.
     * @param newData Valor do novo dado.
     * @param nth Posição do dado a ser substituido.   
     */
    public abstract void setData(Data newData, int nth);

    /**
     *  
     */
    public abstract int getDataOrder(String name);

    /**
     * Adiciona um dado a este metadado. 
     * @param attr Dado a ser inserido. 
     */
    public abstract void addData(Data attr);

    /**
     * Remove deste metadado o dado que possui este nome.
     * @param name Nome do dado a ser removido. 
     */    
    public abstract void removeData(String name);

    /**
     * Remove deste metadado o dado que ocupa a n-ésima posição.
     * @param nth Posição do dado a ser removido. 
     */
    public abstract void removeData(int nth);

    /**
     * Realiza a junção deste metadado com um conjunto de dados.
     * 
     * @param newAttributes Conjunto de dados que se deseja unir. 
     */
    public abstract void join(Collection<Data> newAttributes);

    /**
     * Imprime o nome dos dados deste metadado no fluxo out de maneira formatada. 
     * @param out Fluxo no qual os dados serão gravados.
     */
    public abstract void displayNames(Writer out) throws IOException;

    /**
     * @see Object#clone().
     */
    public abstract Object clone();

    /**
     * Retorna o tamanho em bytes de uma instância descrita por este metadado.
     * O framework não oferece suporte a tipo de dados com tamanho variável. 
     * @return Tamanho em bytes desta instância.
     */
    public abstract long instanceSize();
}

