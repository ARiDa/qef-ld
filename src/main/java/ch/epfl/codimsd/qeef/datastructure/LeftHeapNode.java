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

    // Basic node stored in leftist heaps
    // Note that this class is not accessible outside
    // of package DataStructures

    class LeftHeapNode
    {
            // Constructors
        LeftHeapNode( Comparable theElement )
        {
            this( theElement, null, null );
        }

        LeftHeapNode( Comparable theElement, LeftHeapNode lt, LeftHeapNode rt )
        {
            element = theElement;
            left    = lt;
            right   = rt;
            npl     = 0;
        }
        
        public String toString(){
            return element.toString();
        }

            // Friendly data; accessible by other package routines
        Comparable   element;      // The data in the node
        
        LeftHeapNode left;         // Left child
        LeftHeapNode right;        // Right child
        LeftHeapNode previous;     // child father
        
        int          npl;          // null path length
    }


