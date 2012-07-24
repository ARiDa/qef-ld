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

import ch.epfl.codimsd.qeef.Metadata;

/**
 * This class defines the result of a codims request. The main results are encapsulated in the
 * ResultSet object.
 * 
 * @author Othman Tajmouati
 * @date April 13, 2006
 * @version 1
 */

@SuppressWarnings("serial")
public class RequestResult implements Serializable {

	/**
	 * Metadata (definition) of the result. It specifies for example the types of results, so the
	 * user knows how to read them.
	 */
	private Metadata resultMetada;
	
	/**
	 * The result code flag.
	 */
	private int resultCode;
	
	/**
	 * Elapsed time of the execution
	 */
	private long elapsedTime;
	
	/**
	 * The main results encapsulated in a resultSet objects.
	 */
	private ResultSet resultSet;
	
	/**
	 * First constructor. You should use the setXXX methods to fill the ResultSet object.
	 */
	public RequestResult() {}
	
	/**
	 * Second constructor.
	 *
	 */
	public RequestResult(ResultSet resultSet, Metadata resultMetada, int resultCode) {
		setResultCode(resultCode);
		setMetadata(resultMetada);
		setResultSet(resultSet);
	}
	
	/**
	 * Set the metadatas associated to this resultSet.
	 * 
	 * @param resultMetada the metadatas associated to this resultSet.
	 */
	public void setMetadata(Metadata resultMetada) { 
		this.resultMetada = resultMetada; 
	}
	
	/**
	 * Set the elapsedTime for the corresponding execution.
	 * 
	 * @param elapsedTime the elapsedTime associated to this resultSet.
	 */
	public void setElapsedTime(long elapsedTime) { 
		this.elapsedTime = elapsedTime; 
	}
	
	/**
	 * Set the resultCode associated to this resultSet.
	 * 
	 * @param resultMetada the resultCode associated to this resultSet.
	 */
	public void setResultCode(int resultCode) { 
		this.resultCode = resultCode; 
	}
	
	/**
	 * Set the resultSet associated to this resultSet.
	 * 
	 * @param resultMetada the resultSet associated to this resultSet.
	 */
	public void setResultSet(ResultSet resultSet) { 
		this.resultSet = resultSet; 
	}

	/**
	 * Returns the elapsedTime.
	 * 
	 * @return the elapsedTime.
	 */
	public long getElapsedTime() { 
		return elapsedTime; 
	}
	
	/**
	 * Returns the resultSet object.
	 * 
	 * @return the resultSet object.
	 */
	public ResultSet getResultSet() { 
		return resultSet; 
	}
	
	/**
	 * Returns the resultCode.
	 * 
	 * @return the resultCode
	 */
	public int getResultCode() {
		return resultCode; 
	}
	
	/**
	 * Returns the resultMetada object.
	 * 
	 * @return the resultMetada object.
	 */
	public Metadata getResultMetadata() { 
		return resultMetada; 
	}
}

