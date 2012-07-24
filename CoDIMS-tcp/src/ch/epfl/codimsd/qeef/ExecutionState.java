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

import java.util.Vector;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.scheduler.CodimsNode;
import ch.epfl.codimsd.query.RequestResult;

/**
 * The ExecutionState class encapsulates the state of the execution. Current states :
 * - opened
 * - closed
 * 
 * @author Othman Tajmouati.
 */
public class ExecutionState {

	private long requestID;
	
	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ExecutionState.class.getName());
	
	/**
	 * Indicates if the execution is finished, ie the requestResult is ready for consumption by the user.
	 */
	private boolean isFinished;
	
	/**
	 * The result of this execution.
	 */
	private RequestResult requestResult;
	
	/**
	 * Default constructor.
	 */
	public ExecutionState(long requestID) {
		
		this.requestID = requestID;
		this.requestResult = null;
		this.isFinished = false;
	}
	
	/**
	 * @return true if the execution is finished, false otherwise.
	 */
	public synchronized boolean isFinished() {
		return isFinished;
	}
	
	public synchronized void close() {
		isFinished = true;
	}
	
	public synchronized long getRequestID() {
		return this.requestID;
	}
	
	public synchronized void setRequestID(long requestID) {
		this.requestID = requestID;
	}
	
	/**
	 * Set the results of this execution.
	 * 
	 * @param requestResult the results.
	 */
	public synchronized void setRequestResult(RequestResult requestResult) {
		this.requestResult = requestResult;
	}
	
	/**
	 * @return the result of the execution.
	 */
	public synchronized RequestResult getRequestResult() {
		
		if (isFinished)
			return this.requestResult;
		else
			return null;
	}
}

