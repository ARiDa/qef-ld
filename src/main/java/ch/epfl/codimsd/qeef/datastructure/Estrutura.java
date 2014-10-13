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
package ch.epfl.codimsd.qeef.datastructure;

import java.util.Iterator;


/**
 * Classe abstrata que generaliza todas as estruturas.
 * Em todas as estruturas, capacity representa o nn�mero m�ximo de elementos que estar�o em mem�ria simultaneamente, e size o tamanho atual da estrutura.
 * Cabe a cada estrutura definir o que fazer quando o n�mero de elementos na estrutura ultrapassar capacity. 
 * 
 * @author Fausto Ayres, Vinicius Fontes
 * 
 * @date Jun 19, 2005
 */

public abstract class Estrutura {

    /**
     * N�mero m�ximo de elementos que podem estar em mem�ria simultaneamente.
     */
    protected int capacity;

    /**
     * Inicializa uma estrutura sem restri��o de capacidade. Ou seja, n�o tem
     * restri��o quanto ao n�meros de inst�ncias que podem estar em mem�ria
     * simultaneamente.
     */
    public Estrutura() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Inicializa uma estrutura com restri��o de capacidade.
     * 
     * @param capacity
     *            N�mero m�ximo de elementos que podem estar em mem�ria ao mesmo
     *            tempo.
     */
    public Estrutura(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Obtem a capacidade desta estrutura. 
     * @return A capacidade desta estrutura.
     */
    public int capacity() {
        return capacity;
    }

    /**
     * Obtem o n�mero de inst�ncias que a estrutura contem.
     * @return O tamanho da estrutura.
     */
    public abstract int size();

    /**
     * Obtem um iterador para as inst�ncias armazenadas nesta estrutura.
     * 
     * @return um iterador para esta estrutura.
     */
    public abstract Iterator iterator();

    /**
     * 
     * Informa se a estrutura est� vazia.
     * 
     * @return True se a estrutura estiver vazia, false caso contr�rio.
     */
    public abstract boolean isEmpty();

    /**
     * 
     * Informa se a estrutura est� cheia.
     * 
     * @return True se a estrutura estiver cheia, false caso contr�rio.
     */
    public abstract boolean isFull();
    
    /**
     * Remove todos os elementos desta estrutura. 
     */
    public abstract void removeAllElements();

}

