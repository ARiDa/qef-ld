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

import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.types.IntegerType;

// BinaryHeap class
//
// CONSTRUCTION: with optional capacity (that defaults to 100)
//
// ******************PUBLIC OPERATIONS*********************
// void insert( x )       --> Insert x
// Comparable deleteMin( )--> Return and remove smallest item
// Comparable findMin( )  --> Return smallest item
// boolean isEmpty( )     --> Return true if empty; else false
// boolean isFull( )      --> Return true if full; else false
// void makeEmpty( )      --> Remove all items
// ******************ERRORS********************************
// Throws Overflow if capacity exceeded

/**
 * Implements a binary heap.
 * 
 * @author Mark Allen Weiss
 */
public class Heap {

	@SuppressWarnings("unused")
	private Logger logger;

	private static final int DEFAULT_CAPACITY = 100;

	private int currentSize; // Number of elements in heap

	private Comparable[] array; // The heap array

	private Hashtable elements;

	/**
	 * Construct the binary heap.
	 */
	public Heap() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Construct the binary heap.
	 * 
	 * @param capacity
	 *            the capacity of the binary heap.
	 */
	public Heap(int capacity) {
		logger = Logger.getLogger("ch.epfl.codimsd.qeef.datastructure.binaryheap");
		currentSize = 0;
		array = new Comparable[capacity + 1];

		elements = new Hashtable(capacity + 1);
	}

	/**
	 * Insert into the priority queue, maintaining heap order. Duplicates are
	 * allowed.
	 * 
	 * @param x
	 *            the item to insert.
	 * @exception Overflow
	 *                if container is full.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void insert(Comparable x) {
		
		if( !elements.containsKey(x) ){
			if (isFull()) {
				try{
					wait();
				} catch(InterruptedException iExc){
					//never occurs
					iExc.printStackTrace();
				}
			}
	
			int hole = ++currentSize;
			array[hole] = x;
			elements.put(x, new Integer(hole));
	
			promote(hole);
			notify();
		} else {
			update(x);
		}
	}

	@SuppressWarnings({"unchecked","unchecked", "unchecked"})
	private void promote(int hole) {

		Comparable x;

		x = array[hole];

		for (; hole > 1 && x.compareTo(array[hole / 2]) > 0; hole /= 2) {
			array[hole] = array[hole / 2];
			elements.put(array[hole], new Integer(hole));
		}

		array[hole] = x;
		elements.put(x, new Integer(hole));
	}

	/**
	 * Find the greater item in the priority queue.
	 * 
	 * @return the smallest item, or null, if empty.
	 */
	public Object findMax() {
		if (isEmpty())
			return null;
		return array[1];
	}

	/**
	 * Remove the greater item from the priority queue.
	 * 
	 * @return the smallest item, or null, if empty.
	 */
	public synchronized Object deleteMax() {

		if (isEmpty()){
			try{
				wait();
			} catch(InterruptedException iExc){
				//never occurs
				iExc.printStackTrace();
			}
		}

		Object minItem = findMax();
		array[1] = array[currentSize--];
		percolateDown(1);

		elements.remove(minItem);
		notify();
		
		return minItem;
	}

	/**
	 * Establish heap order property from an arbitrary arrangement of items.
	 * Runs in linear time.
	 * 
	 * @param objs
	 *            Array in wich the heap will be created.
	 */
	public synchronized void buildHeap(Comparable[] objs) {
		currentSize = objs.length;
		array = objs;

		for (int i = currentSize / 2; i > 0; i--)
			percolateDown(i);
	}

	/**
	 * Test if the priority queue is logically empty.
	 * 
	 * @return true if empty, false otherwise.
	 */
	public boolean isEmpty() {
		return currentSize == 0;
	}

	/**
	 * Test if the priority queue is logically full.
	 * 
	 * @return true if full, false otherwise.
	 */
	public boolean isFull() {
		return currentSize == array.length - 1;
	}

	/**
	 * Make the priority queue logically empty.
	 */
	public void makeEmpty() {
		currentSize = 0;
		elements = new Hashtable();
	}

	/**
	 * Internal method to percolate down in the heap.
	 * 
	 * @param hole
	 *            the index at which the percolate begins.
	 */
	@SuppressWarnings("unchecked")
	private void percolateDown(int hole) {
		int child;
		Comparable tmp = array[hole];

		for (; hole * 2 <= currentSize; hole = child) {
			
			child = hole * 2;
			if (child != currentSize
					&& array[child + 1].compareTo(array[child]) > 0){
				child++;
			} 
			if (array[child].compareTo(tmp) > 0) {
				array[hole] = array[child];
				elements.put(array[hole], new Integer(hole));
			} else
				break;
		}
		
		array[hole] = tmp;
		elements.put(tmp, new Integer(hole));
	}
	
	@SuppressWarnings("unchecked")
	private void update(Comparable x){
		
		int hole = ((Integer)elements.get(x)).intValue();
		
		if( hole > 1 && array[hole].compareTo(array[hole/2]) > 0) { //promote
			promote(hole);
		} else {
			percolateDown(hole);
		}
	}

	public static void main(String[] args) throws Exception{

		Heap h = new Heap();

		IntegerType e1 = new IntegerType(5);
		IntegerType e2 = new IntegerType(10);
		IntegerType e3 = new IntegerType(7);
		IntegerType e4 = new IntegerType(1);
		IntegerType e5 = new IntegerType(2);

		h.insert(e1);
		h.insert(e2);
		h.insert(e3);
		h.insert(e4);
		h.insert(e5);

		e4.setValue(11);
		h.update(e4);
		
		e1.setValue(12);
		h.update(e1);

		System.out.println(h.deleteMax());
		System.out.println(h.deleteMax());
		System.out.println(h.deleteMax());
		System.out.println(h.deleteMax());
		System.out.println(h.deleteMax());
	}

}

