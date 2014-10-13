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
package ch.epfl.codimsd.qeef.util;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.globus.examples.stubs.DQEEService_instance.BlackBoardParams;
import org.globus.examples.stubs.DQEEService_instance.DQEEPortType;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.SystemConfiguration;
import ch.epfl.codimsd.query.Request;

/**
 * The RemoteBlackBoardManager copies the local blackBoard to the remote one.
 * In the current version, only object with string keys are copied. This is due,
 * to serialization of objects in SOAP.
 * 
 * @author Othmna Tajmouati.
 *
 */
public class RemoteBlackBoardManager {

	/**
	 * Log4 logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RemoteBlackBoardManager.class.getName());
    
	
	/**
	 * Copy the local BlackBoard to the remote one.
	 * @param qenodes remote node port type.
	 * @throws Exception
	 */
	public static synchronized void copyToRemoteBlackBoard(DQEEPortType qenodes, int numNode) throws RemoteException {
		
		// Get the local blackBoard hashmap.
		BlackBoard bl = BlackBoard.getBlackBoard();
		HashMap hash = bl.getHashtable();
		
		if (numNode != 0)
			bl.put(Constants.THIS_NODE, numNode+"");
		
		// Get the set of keys.
		Set blackBoardStrings = hash.keySet();
		Iterator itt = blackBoardStrings.iterator();
		
		while (itt.hasNext()) {
			
			// Get the object corresponding to this key.
			String key1 = (String) itt.next();
			Object valueObj = (Object) bl.get(key1);
			String value1 = "";
			
			// if the object is a string one, it could be copied together with its key
			// to the remote blackBoard.
			if (valueObj instanceof String) {
				
				value1 = (String) valueObj;
				BlackBoardParams params = new BlackBoardParams(key1, value1);
				qenodes.copyToRemoteBlackBoard(params);
				
			} else if (valueObj instanceof String[]) {
				
				String[] newValueObj = (String[]) valueObj;
				
				for (int j = 0; j < newValueObj.length; j++) {
					value1 += newValueObj[j] + ",";
				}
				
				value1 = value1.substring(0, value1.length()-1);
				BlackBoardParams params = new BlackBoardParams(key1, value1);
				qenodes.copyToRemoteBlackBoard(params);
			}
		}
	}
	
	public static void prepareBlackBoard(Request request) {
		
		try {
			
			BlackBoard bl = BlackBoard.getBlackBoard();
			String iriCatalog = SystemConfiguration.getSystemConfigInfo(Constants.IRICatalog);
			int indexOfLocalhost = iriCatalog.indexOf("localhost");
			String localIP = InetAddress.getLocalHost().getHostAddress();
			iriCatalog = iriCatalog.substring(0, indexOfLocalhost) + localIP + iriCatalog.substring(indexOfLocalhost+"localhost".length(), iriCatalog.length());
			bl.put(Constants.IRICatalog, iriCatalog);
			
			bl.put(Constants.REQUEST_ID, request.getRequestID() + "");
			bl.put(Constants.ENVIRONMENT_ID, Constants.REQUEST_ID);
			
			if (request.getRequestParameter().containsKey(Constants.LOG_EXECUTION_PROFILE)) {
				String log = (String) request.getRequestParameter().getParameter(Constants.LOG_EXECUTION_PROFILE);
				if (log.equalsIgnoreCase("TRUE")) {
					bl.put(Constants.LOG_EXECUTION_PROFILE, "TRUE");
				}
			}
			
		} catch (Exception ex) {
			// This is never the case.
		}
	}

	public static void main(String args[]) {
		
		BlackBoard bl = BlackBoard.getBlackBoard();
		String[] values = {"first", "second", "third"};
		bl.put("TEST1", "VALUE1");
		bl.put("STRINGARRAY",values );
		try {
			//RemoteBlackBoardManager.copyToRemoteBlackBoard(null);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}

