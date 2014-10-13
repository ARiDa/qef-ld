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

import java.util.Comparator;

import org.apache.log4j.Logger;

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
 * @author Mark Allen Weiss
 */
public class BinaryHeap
{
	
	private Logger logger;
	
    private static final int DEFAULT_CAPACITY = 100;

    private int currentSize;      // Number of elements in heap
    private Object [ ] array; // The heap array
    
    private Comparator cmp;
    
    /**
     * Construct the binary heap.
     */
    public BinaryHeap(Comparator cmp )
    {
        this( DEFAULT_CAPACITY, cmp );
    }

    /**
     * Construct the binary heap.
     * @param capacity the capacity of the binary heap.
     */
    public BinaryHeap( int capacity, Comparator cmp )
    {
    	logger = Logger.getLogger("ch.epfl.codimsd.qeef.datastructure.binaryheap");
        currentSize = 0;
        array = new Object[ capacity + 1 ];
        this.cmp = cmp;
    }

    /**
     * Insert into the priority queue, maintaining heap order.
     * Duplicates are allowed.
     * @param x the item to insert.
     * @exception Overflow if container is full.
     */
    @SuppressWarnings("unchecked")
	public void insert( Object x ) 
    {
        if( isFull( ) ){
            logger.warn("Impossible to insert element into the heap. Heap is full.");
        }

            // Percolate up
        int hole = ++currentSize;
        for( ; hole > 1 && cmp.compare(x, array[ hole / 2 ] ) > 0; hole /= 2 )
            array[ hole ] = array[ hole / 2 ];
        array[ hole ] = x;
    }

    /**
     * Find the greater item in the priority queue.
     * @return the smallest item, or null, if empty.
     */
    public Object findMax( )
    {
        if( isEmpty( ) )
            return null;
        return array[ 1 ];
    }

    /**
     * Remove the greater item from the priority queue.
     * @return the smallest item, or null, if empty.
     */
    public Object deleteMax( )
    {
        if( isEmpty( ) )
            return null;

        Object minItem = findMax( );
        array[ 1 ] = array[ currentSize-- ];
        percolateDown( 1 );

        return minItem;
    }

    /**
     * Establish heap order property from an arbitrary
     * arrangement of items. Runs in linear time.
     * 
     * @param objs Array in wich the heap will be created. 
     */
    public void buildHeap(Object []objs)
    {
    	currentSize = objs.length;
    	array = objs; 
    	
        for( int i = currentSize / 2; i > 0; i-- )
            percolateDown( i );
    }

    /**
     * Test if the priority queue is logically empty.
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty( )
    {
        return currentSize == 0;
    }

    /**
     * Test if the priority queue is logically full.
     * @return true if full, false otherwise.
     */
    public boolean isFull( )
    {
        return currentSize == array.length - 1;
    }

    /**
     * Make the priority queue logically empty.
     */
    public void makeEmpty( )
    {
        currentSize = 0;
    }

    /**
     * Internal method to percolate down in the heap.
     * @param hole the index at which the percolate begins.
     */
    @SuppressWarnings("unchecked")
	private void percolateDown( int hole )
    {
/* 1*/      int child;
/* 2*/      Object tmp = array[ hole ];

/* 3*/      for( ; hole * 2 <= currentSize; hole = child )
        {
/* 4*/          child = hole * 2;
/* 5*/          if( child != currentSize &&
/* 6*/                  cmp.compare(array[ child + 1 ], array[ child ] ) > 0 )
/* 7*/              child++;
/* 8*/          if( cmp.compare(array[ child ], tmp ) > 0 )
/* 9*/              array[ hole ] = array[ child ];
            else
/*10*/              break;
        }
/*11*/      array[ hole ] = tmp;
    }
    
    
    

}


