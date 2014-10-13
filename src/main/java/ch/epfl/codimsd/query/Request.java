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

/**
 * Version 0.1
 * 
 * @author Othman Tajmouati
 * 
 * @date April 13, 2006
 * 
 * This class defines a request within Codims. Each request should contain a query that implements
 * the interface {@link ch.epfl.codimsd.query.Query}.
 * 
 */

public class Request {
	
	/**
	 * The requestType. See codims constants for a complete list of request type values.
	 */
	private int requestType;
	
	/**
	 * The requestParameter of this request.
	 */
	private RequestParameter requestParameter;
	
	/**
	 * The query to execute.
	 */
	private Query query;
	
	/**
	 * The request ID, used to get informations on a specific execution.
	 */
	private long requestID;
	
	/**
	 * Default Constructor.
	 */
	public Request() {}
	
	/**
	 * Builds the codims request with a requestID. This constructor is used if codims runs in multi user mode.
	 * 
	 * @param query a supported query in codims.
	 * @param requestType the requestType associated to this request, you should verify that the requestType
	 * is defined in codims constants, as well as in the catalog.
	 * @param requestParameter parameters of this request.
	 */
	public Request(Query query, int requestType, RequestParameter requestParameter, long requestID) {
		
		this.query = query;
		this.requestParameter = requestParameter;
		this.requestType = requestType;
		this.requestID = requestID;
	}
	
	/**
	 * Builds the codims request.
	 * 
	 * @param query a supported query in codims.
	 * @param requestType the requestType associated to this request, you should verify that the requestType
	 * is defined in codims constants, as well as in the catalg.
	 * @param requestParameter parameters of this request.
	 */
	public Request(Query query, int requestType, RequestParameter requestParameter) {
		
		this.query = query;
		this.requestParameter = requestParameter;
		this.requestType = requestType;
	}
	
	/**
	 * Returns the requestType.
	 * 
	 * @return requestType
	 */
	public int getRequestType() {
		return requestType;
	}
	
	/**
	 * Returns the requestID.
	 * 
	 * @return requestID
	 */
	public long getRequestID() {
		return requestID;
	}
	
	/**
	 * Returns the requestParameter.
	 * 
	 * @return requestParameter
	 */
	public RequestParameter getRequestParameter() {
		return requestParameter;
	}
	
	/**
	 * Returns the query.
	 * 
	 * @return query
	 */
	public Query getQuery() {
		return query;
	}
	
	/**
	 * Returns the number of parameters for this query.
	 * 
	 * @return number of parameters
	 */
	public int getParametersCount() {
		return requestParameter.getParametersCount();
	}
	
	/**
	 * Sets the requestType.
	 */
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}
	
	/**
	 * Sets the requestID.
	 */
	public void setRequestID(long requestID) {
		this.requestID = requestID;
	}
	
	/**
	 * Sets the requestParameter.
	 */
	public void setRequestParameter(RequestParameter requestParameter) {
		this.requestParameter = requestParameter;
	}
	
	/**
	 * Sets the query.
	 */
	public void setQuery(Query query) {
		this.query = query;
	}
}

