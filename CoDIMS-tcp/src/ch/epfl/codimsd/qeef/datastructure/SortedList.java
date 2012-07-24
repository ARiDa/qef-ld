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
import java.util.LinkedList;
import java.util.ListIterator;


/**
 * Implementa uma Lista Ordenada.
 *
 * @author Vinicius Fontes
 * 
 * @date Jun 27, 2005
 */
public class SortedList extends List{

    private LinkedList list;
    
    public SortedList(){
        super();
        list = new LinkedList();
    }
    
    public SortedList(int capacity){
        super(capacity);
        list = new LinkedList();
    }
    
    public int size(){
        return list.size();
    }
    
    public boolean isEmpty(){
        return list.size()==0?true:false;
    }
    
    public boolean isFull(){
        return list.size()==capacity?true:false;
    }
    
    public Iterator iterator(){
        return list.listIterator();
    }
    
    public synchronized Object get(){
        
        try {
            while (list.size() == 0)
                wait();
        } catch (InterruptedException iExc) {
            iExc.printStackTrace();
            //Nunca acontecer�
        }
        
        return list.removeLast();
    }
    
    public synchronized boolean remove(Object o){
        try {
            while (list.size() == 0)
                wait();
        } catch (InterruptedException iExc) {
            iExc.printStackTrace();
            //Nunca acontecer�
        }
        return list.remove(o);
    }
    
    @SuppressWarnings("unchecked")
	public synchronized void removeAllElements(){
        list.removeAll(list);
    }
    
    /**
     * Adiciona este objeto na lista em sua posi��o correta.
     * Se este objeto j� existir na lista ser� substituido.
     */
    @SuppressWarnings("unchecked")
	public synchronized void add(Object o){

        ListIterator it;
        Comparable curr;

        //Se lista cheia aguarda
        try {
            while (list.size() == capacity)
                wait();
        } catch (InterruptedException iExc) {
            iExc.printStackTrace();
            //Nunca acontecer�
        }
        
        //Se vazia insere logo
        if(list.size() == 0){
            list.addFirst(o);
            notify();
            return;
        }
        
        it = (ListIterator)list.listIterator();
        
        while(it.hasNext()){
            curr = (Comparable)it.next();
            if(curr.compareTo(o)>0){
                it.previous();
                break;	
            }                
        }        
                
        it.add(o);        
        notify();
    }
    
    public synchronized boolean changeOrder(Comparable o){
        
        if (!list.remove(o))
        	return false;
        	
        add(o);
        return true;
    }
    
    public synchronized Comparable removeFirst(){
        
        try {
            while (list.size() == 0)
                wait();
        } catch (InterruptedException iExc) {
            iExc.printStackTrace();
            //Nunca acontecer�
        }
        
        return (Comparable)list.removeFirst();
    }
    
    public synchronized Comparable removeLast(){
        
        try {
            while (list.size() == 0)
                wait();
        } catch (InterruptedException iExc) {
            iExc.printStackTrace();
            //Nunca acontecer�
        }
        
        return (Comparable)list.removeLast();
    }
    
    public String toString(){
        String aux;
        ListIterator it;
        Comparable curr;
        
        aux = "";
        it = (ListIterator)list.iterator();
        
        while(it.hasNext()){
            curr = (Comparable)it.next();
            aux += ";" + curr;
        }
        return aux;
    }
}

