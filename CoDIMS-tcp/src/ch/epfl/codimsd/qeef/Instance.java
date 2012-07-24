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

import java.util.Collection;
import java.util.Properties;

import ch.epfl.codimsd.qeef.types.Type;

/**
 * Classe abstrata que define o conceito de inst�ncia no ch.epfl.codimsd.qeef. Uma inst�ncia � uma unidade de 
 * dados unit�ria a ser processada que � espec�fica para um modelo de dados. No modelo relacional, por exemplo,
 * uma inst�ncia � representada por uma tupla.
 * 
 * Abstract classroom that defines the concept of instance in the ch.epfl.codimsd.qeef.  
 * An instance is a unit of data to be processed that she is specific for a model of data.  
 * In the relationary model, for example, an instance is represented by a tuple.  
 *
 * @author Vinicius Fontes
 */

public abstract class Instance extends DataUnit implements Cloneable{

    /**
     * Construtor padrão.
     */
    public Instance() {
        properties = new Properties();
    }
    
    /**
     * Define quantas unidade de dados uma inst�ncia representa.
     * @return 1, pois uma inst�ncia sempre representa uma unidade de dados.
     */
    public int size(){
        return 1;
    }

    /**
     * Obtem uma lista de elementos com os valores dos dados desta instância. 
     */
    public abstract Collection<Type> getValues();

    /**
     * Atribui um conjunto de valores as dados representados por esta instância.
     * @param values Valores a serem atribuidos.
     */
    public abstract void setData(Collection<Type> values);

    /**
     * Atribui um valor para o primeiro dado nesta inst�ncia que ainda n�o possui um valor.
     * @param value Valor a ser atribuido.
     */
    public abstract void addData(Type value);

    /**
     * Obtem o valor do dado que ocupa a n-�sima posi��o.
     * @param nth Posi��o ocupada pelo dado.
     * @return Valor do dado que ocupa a n-�sima posi��o. 
     */
    public abstract Type getData(int order);

    /**
     * Remove o valor do dado que ocupa a n-�sima posi��o.
     * @param nth Posi��o ocupada pelo dado que se deseja retirar seu valor. 
     */
    public abstract void removeData(int nth);

    /**
     * Atribui um novo valor ao dado que ocupa a n-�sima posi��o.
     * @param newValue Novo valor a ser atribu�do.
     * @param nth Posi��o ocupada pelo dado que se deseja retirar seu valor.
     */
    public abstract void updateData(Type newValue, int nth);

    /**
     * Cria uma nova inst�ncia a partir dos dados das inst�ncias t1 e t2.
     * @param leftInstance Inst�ncia cujos valores ser�o os primeiros na inst�ncia resultante da jun��o.
     * @param rightInstance Inst�ncia cujos valores ser�o os �ltimos na inst�ncia resultante da jun��o.
     * @return Nova inst�ncia que � resultado da fus�o de t1 com t2. 
     */
    public abstract Instance join(Instance leftInstance, Instance rightInstance);
}


