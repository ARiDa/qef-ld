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

import java.util.HashMap;

/**
 * The BlackBoard is a CoDIMS structure that stores some objects referenced by a key.
 * It is sent to remote nodes, when the request needs parallelization. Access to the
 * BlackBoard are similar to those for a java HashMap.
 * 
 * @author Vinicius Fontes, Othman Tajmouati.
 */
public class BlackBoard {
    
	/**
	 * Main hashtable containing the objects.
	 */
	private static HashMap<String, Object> blackBoardHashMap;
	
	/**
	 * BlackBoard singleton.
	 */
	private static BlackBoard ref;
    
	/**
	 * Private default constructor.
	 */
	private BlackBoard() {
		blackBoardHashMap = new HashMap<String, Object>();
	}
	
	/**
	 * @param key key of the object.
	 * @return the requested object.
	 */
	public synchronized Object get(String key) {
    	return blackBoardHashMap.get(key);
    }
	
	/**
	 * Put an object in the BlackBoard.
	 * 
	 * @param key key of the object.
	 * @param object the object to store.
	 */
	public synchronized void put(String key, Object object) {
		
		blackBoardHashMap.put(key, object);
    }
	
	/**
	 * @return the blackBoard reference.
	 */
	public static synchronized BlackBoard getBlackBoard() {
		
		if (ref == null)
			ref = new BlackBoard();
		
		return ref;
	}
	
	/**
	 * @param key key of an object.
	 * @return true if the key is in the BlakcBoard, false otherwise.
	 */
	public synchronized boolean containsKey(Object key) {
		return blackBoardHashMap.containsKey(key);
	}
	
	/**
	 * Fill the BlackBoard with all the entries of the passed hashmaps.
	 * @param hashtable the hashmap to copy.
	 */
	public synchronized void copyBlackBoard(HashMap<String, Object> hashMap) {
		blackBoardHashMap.putAll(hashMap);
	}
	
	/**
	 * @return the blackBoard hashmap.
	 */
	public synchronized HashMap<String, Object> getHashtable() {
		return blackBoardHashMap;
	}
	
	/**
	 * Remove an object from the BlackBoard.
	 * 
	 * @param key corresponding to remove.
	 */
	public synchronized void remove(String key) {
		
		blackBoardHashMap.remove(key);
	}
}



