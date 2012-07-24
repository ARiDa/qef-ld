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
package ch.epfl.codimsd.exceptions.transactionMonitor;

/**
 * Thrown when the TransactionMonitor try to connect to a database and fails.
 * 
 * @author Othman Tajmouati.
 */
public class TransactionMonitorConnectionException extends TransactionMonitorException {

	/**
	 * For the Serializer.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Contstructor.
	 * 
	 * @param message exception message.
	 */
	public TransactionMonitorConnectionException(String message) {
		super(message);
	}
	
}

