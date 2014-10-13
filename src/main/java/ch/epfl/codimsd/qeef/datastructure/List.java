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


/**
 * Classe abstrata que generaliza as fun��es de uma estrutura de recupera��o simples. Por exmplo uma fila, 
 * pilha e um buffer.
 * 
 * @author Vinicius Fontes
 * 
 * @date Jun 19, 2005
 */
public abstract class List extends Estrutura {

    /**
     * Inicializa uma lista sem restri��o de capacidade. Ou seja, n�o tem
     * restri��o quanto ao n�meros de inst�ncias que podem estar em mem�ria
     * simultaneamente.
     */
    public List() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Inicializa uma lista com restri��o de capacidade.
     * 
     * @param capacity
     *            N�mero m�ximo de elementos que podem estar em mem�ria ao mesmo
     *            tempo.
     */
    public List(int capacity) {
        this.capacity = capacity;
    }

    
    /**
     * Insere uma inst�ncia na estrutura.
     * 
     * @param newInstance Inst�ncia a ser inserida na estrutura.
     */
    public abstract void add(Object newInstance);

    /**
     * Remove um elemento da estrutura. Cabe a cada implementa��o definir qual a inst�ncia a ser retirada.
     * 
     * @return Inst�ncia removida.
     */
    public abstract Object get();

    
    /** 
     * Remove a inst�ncia instance da lista.
     * 
     * @param instance Inst�ncia a ser removida.
     * 
     * @return True se conseguiu remover a inst�ncia da lista, else caso contr�rio.
     */
    public abstract boolean remove(Object instance);
}

