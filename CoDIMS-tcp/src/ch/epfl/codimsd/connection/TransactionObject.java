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
package ch.epfl.codimsd.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorException;
import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorSQLException;

/**
 * The TransactionObject class defines a transaction within CODIMS. Each transaction has an IRI,
 * a timeStamp, an sql connection object and an sql statement object.
 * 
 * @author Othman Tajmouati.
 *
 */
public class TransactionObject {
	
	/**
	 * The timeStamp associated to this transaction. It represents the transaction idle time.
	 */
	private long timeStamp;
	
	/**
	 * The database IRI.
	 */
	private String IRI;
	
	/**
	 * The sql connection.
	 */
	private Connection con;
	
	/**
	 * The sql statement.
	 */
	private Statement stmt;
	
	/**
	 * Default constructor.
	 * 
	 * @param con - The sql connection.
	 * @param IRI - The database IRI.
	 * @param timeStamp - The timeStamp.
	 * @param stmt - The sql Statement.
	 */
	public TransactionObject(Connection con, String IRI, long timeStamp, Statement stmt) {
		
		this.con = con;
		this.timeStamp = timeStamp;
		this.IRI = IRI;
		this.stmt = stmt;
	}
	
	/**
	 * Retunrs the sql connection.
	 * @return - The connection.
	 */
	public Connection getConnection() {
		return con;
	}
	
	/**
	 * Returns the timeStamp.
	 * @return - The timeStamp.
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
	
	/**
	 * Retunrs the database IRI.
	 * @return - The database IRI.
	 */
	public String getIRI() {
		return IRI;
	}
	
	/**
	 * Returns the sql statement.
	 * @return - The statement.
	 */
	public Statement getSQLStatement() {
		return stmt;
	}
	
	/**
	 * Set the IRI.
	 * @param IRI - The database IRI.
	 */
	public void setIRI(String IRI) {
		this.IRI = IRI;
	}
	
	/**
	 * Set the timeStamp.
	 * @param timeStamp - The timeStamp.
	 */
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	/**
	 * Set the sql connection.
	 * @param con - The sql connection.
	 */
	public void setConnection(Connection con) {
		this.con = con;
	}
	
	/**
	 * Set the sql statement.
	 * @param stmt - The sql statement.
	 */
	public void setSQLStatement(Statement stmt) {
		this.stmt = stmt;
	}
	
	/**
	 * Closes the sql statement.
	 * 
	 * @throws TransactionMonitorException
	 */
	public void closeSQLStatement() throws TransactionMonitorException {
		
		try {
			stmt.close();
		} catch (SQLException ex) { 
			throw new TransactionMonitorSQLException("SQLException : " + ex.getMessage());
		}
	}
}

