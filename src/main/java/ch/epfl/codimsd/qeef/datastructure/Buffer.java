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

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

public class Buffer extends List{

    protected Vector elements;

    /**
     * Inicializa um buffer sem restri��o de capacidade. Ou seja, n�o tem
     * restri��o quanto ao n�meros de inst�ncias que podem estar em mem�ria
     * simultaneamente.
     */
    public Buffer() {
        super();
        elements = new Vector();
    }

    /**
     * Inicializa um buffer com restri��o de capacidade.
     * 
     * @param capacity
     *            N�mero m�ximo de elementos que podem estar em mem�ria ao mesmo
     *            tempo.
     */
    public Buffer(int capacity) {
        super(capacity);

        elements = new Vector();
    }

    public int size() {
        return this.elements.size();
    }

    @SuppressWarnings("unchecked")
	public synchronized void add(Object t) {

        try {
            while (capacity > 0 && elements.size() >= capacity)
                wait();
        } catch (InterruptedException iExc) {
            iExc.printStackTrace();
            //Nunca acontecer�
        }
        elements.add(t);
        notify();
    }

    public synchronized void removeAllElements() {
        elements.clear();
        notify();
    }

    public synchronized Object get() {
        Object t;

        try {
            while (elements.isEmpty())
                wait();
        } catch (InterruptedException iExc) {
            iExc.printStackTrace();
            //Nunca acontecer�
        }
        t = (Object) elements.remove(0);
        notify();

        return (t);
    }
    
    public synchronized boolean remove(Object instance){
        boolean removed;
        
        removed = elements.remove(instance);       
        notify();
        return removed;
    }

    public Iterator iterator() {
        return elements.iterator();
    }

    public boolean isEmpty() {
        return (elements.isEmpty());
    }

    public boolean isFull() {
        return elements.size() >= capacity ? true : false;
    }

    /*
     * Retorna um bloco de tuplas de tamanho size ou se size < buffer.getSize()
     * todas as tuplas do buffer
     * 
     * OBS: Bloco pode sair com um numero menor de tuplas
     */
    @SuppressWarnings("unchecked")
    public synchronized Collection getBlock(int size) {

        java.util.List auxList;
        Vector block;

        auxList = elements.subList(0, size <= elements.size() ? size : elements
                .size());
        block = new Vector(auxList);
        auxList.clear();

        notify();

        return block;
    }

}

