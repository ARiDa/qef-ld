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

import java.util.HashMap;
import java.util.Set;
/**
 * Version 0.1
 * 
 * @author Othman Tajmouati
 * 
 * @date April 13, 2006
 * 
 * This class defines request parameters for a query
 */

public class RequestParameter {

	/**
	 * HashMap containing the list of parameters.
	 */
	private HashMap<String,Object> params;

	/**
	 * Default constructor. It creates the HashMap that will contain the request parameters.
	 */
	public RequestParameter() {
		params = new HashMap<String,Object>();
	}

	/**
	 * Returns a parameter associated with a key from the Hashmap
	 * 
	 * @param key the String key associated with the parameter
	 * @return the parameter object, you should cast to the right type
	 */
	public Object getParameter(String key) {
		return params.get(key);
	}

	/**
	 * Put a parameter into the HashMap. If the HashMap already contains a parameter with 
	 * the same key the parameter object is overwritten.
	 * 
	 * @param key the String key associated with the parameter
	 * @param object the parameter object
	 */
	public void setParameter(String key, Object object) {
		params.put(key, object);
	}

	/**
	 * Returns the number of parameters associated to the query (size of HashMap)
	 * 
	 * @return number of parameters
	 */
	public int getParametersCount() {
		return params.size();
	}
	
	/**
	 * Check if a paramter is defined in the RequestParameter hashtable.
	 * 
	 * @param key - name of the parameter
	 * @return - true if the key is defined in the hashTable, false otherwise
	 */
	public boolean containsKey(String key) {
		
		if (params.containsKey(key))
			return true;
		else
			return false;
	}
	
	/**
	 * @return - Returns a set view of the keys contained in the params hashmap.
	 */
	public Set keySet() {
		return params.keySet();
	}
}

