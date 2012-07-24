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
package ch.epfl.codimsd.query;

import java.io.Serializable;
import java.util.*;

import ch.epfl.codimsd.qeef.DataUnit;

/**
 * The ResultSet class defines the result of a CoDIMS query. It is encapsulated in the RequestResult
 * object and implements the Iterator class. The results are represented by a java LinkedList containing
 * {@link ch.epfl.codimsd.qeef.DataUnit} objects (DataUnit objects).
 * 
 * ResultSet utilization :
 * <li> Open the ResultSet
 * <li> While the ResultSet has a next parameter get next parameter 
 * <li> Close the ResultSet
 * 
 * ResultSet creation :
 * <li> Create a new ResultSet object
 * <li> Use the @see ch.epfl.codimsd.query.ResultSet.add() method to write to the list
 * <li> Close the ResultSet
 * 
 * @author Othman Tajmouati
 * @date April 13, 2006
 * @version 1
 */

@SuppressWarnings({ "serial", "rawtypes" })
public class ResultSet implements Iterator, Serializable {
	
	/**
	 * Java LinkedList containing the results.
	 */
	public LinkedList<DataUnit> linkedList;
	
	/**
	 * A cursor that moves through each value of the list.
	 */
	private int cursor;
	
	/**
	 * Size of the list.
	 */
	private int size;
	
	/**
	 * Boolean flag indicating if the ResultSet object is opened or closed.
	 */
	private boolean opened;
	
	/**
	 * Default constructor. It creates a new java LinkedList containing the data units.
	 */
	public ResultSet() {

		linkedList = new LinkedList<DataUnit>();
		size = 0;
	}
	
	/**
	 * @return true if there exists another element in the list, false otherwise.
	 */
	public boolean hasNext() {
		
		if ((cursor == size) || (opened==false))
			return false;
		else 
			return true;
	}
	
	/**
	 * Get the next element from the list.
	 * 
	 * @return the next element.
	 */
	public Object next() {
		
		if ((cursor <= size) || (opened == true)) {
			
			DataUnit dataUnit = linkedList.get(cursor);
			cursor++;
			
			return dataUnit;
		}
		
		return null;	
	}
	
	/**
	 * Remove the current element from the list.
	 */
	public void remove() {
		
		if ((cursor <= size) || (opened == true)) {
		
			linkedList.remove(cursor);
			cursor--;
		}
	}
	
	/**
	 * Open the ResultSet object.
	 *
	 */
	public void open() {
		
		cursor = 0;
		opened=true;
	}
	
	/**
	 * Close the ResultSet object
	 *
	 */
	public void close() {
		opened=false;
	}
	
	/**
	 * Add a DataUnit to the end of the list.
	 * 
	 * @param dataUnit the dataUnit result.
	 */
	public void add(DataUnit dataUnit) {

		size++;
		linkedList.add(dataUnit);
	} 
}

