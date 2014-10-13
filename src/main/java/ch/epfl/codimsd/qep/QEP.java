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
package ch.epfl.codimsd.qep;

import java.util.Hashtable;

import org.dom4j.Document;

import ch.epfl.codimsd.qeef.Operator;


/**
 * Version 1
 * 
 * The QEP defines a common structure for CoDIMS when we store operators we read from a
 * query exeuction plan (xml qep, strin qep, etc.).
 * 
 * @author Othman Tajmouati
 * 
 * @date April 13, 2006
 */

public class QEP {

	/**
	 * XML Document of the query execution plan.
	 */
	private Document document;
	
	/**
	 * Hashtable holding the opNode structure of the operators.
	 */
	private Hashtable<String, OpNode> operatorList;
	
	/**
	 * The initialized operators.
	 */
	private Hashtable<Integer, Operator> concreteOpList;
	
	/**
	 * Flag used to determine if the query execution plan is to be executed remotely.
	 */
	public boolean existRemote;
	
	/**
	 * Constructor.
	 */
	public QEP() {

		// Initializations.
		this.existRemote = false;
		operatorList = new Hashtable<String, OpNode>();
	}
	
	/**
	 * Set the xml dom4j document.
	 * 
	 * @param document the xml document.
	 */
	public void setDocument(Document document) {
		this.document = document;
	}
	
	/**
	 * Set the hashtable containing opNode structures.
	 * 
	 * @param hashtable contains the opNode structures.
	 */
	public void setOpList(Hashtable<String, OpNode> hashtable) {
		this.operatorList = hashtable;
	}
	
	/**
	 * @return the xml document.
	 */
	public Document getDocument() {
		return document;
	}
	
	/**
	 * @return the hashtable containing opNode structures.
	 */
	public Hashtable<String, OpNode> getOperatorList() {
		return operatorList;
	}
	
	/**
	 * @return the hashtable containing initialized operators.
	 */
	public Hashtable<Integer, Operator> getConcreteopList() {
		return concreteOpList;
	}
	
	/**
	 * Set the operator hashtable.
	 * 
	 * @param concreteOpList hashtable containing intialized operators.
	 */
	public void setConcreteOpList(Hashtable<Integer, Operator> concreteOpList) {
		this.concreteOpList = concreteOpList;
	}
}

