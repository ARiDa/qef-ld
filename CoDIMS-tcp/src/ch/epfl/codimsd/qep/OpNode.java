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

/**
 * The OpNode structure in CoDIMS defines all the informations needed to construct an operator.
 * It contains :
 * - the name of the operator;
 * - the identififer of the operator;
 * - the list of producers;
 * - list of parameters;
 * - the timeStamp associated to this operator;
 * - the type of operator (Scan, ...);
 * - the parallelizable information.
 * 
 * This structure is constructed when the QEPFactory reads the QEP. The factory builds a hashtable of OpNode
 * structures, which is further used to instantiate operators. The constructor of each operator receive this
 * opNode object and retrieve all informations needed to construct itself. In this model, the sequence of
 * parameter definitions from the QEP is the same as the sequence of parameters in the "params" list; it is
 * therefore the responsibility of the operator to retrieve its parameters correctly.
 * 
 * @author Othman Tajmouati.
 */
public class OpNode {

	/**
	 * Parameters of the operator.
	 */
	private String params[];
	
	/**
	 * Operator name.
	 */
	private String opName;
	
	/**
	 * 
	 */
	private int opID;
	private int[] producerID;
	private String timeStamp;
	private String type;
	private boolean parallelizable;

	
	public OpNode(int opID, String opName, int[] producerID, String params[], String timeStamp, String type, boolean parallelizable) {

		this.opID = opID;
		this.opName = opName;
		this.producerID = producerID;
		this.params = params;
		this.timeStamp = timeStamp;
		this.type = type;
		this.parallelizable = parallelizable;
	}
	
	
	public String[] getParams() {
		return params;
	}
	
	public String getType() {
		return type;
	}
	
	public String getOpName() {
		return opName;
	}
	
	public int getOpID() {
		return opID;
	}
	
	public String getOpTimeStamp() {
		return timeStamp;
	}
	
	public int[] getProducerIDs() {
		return producerID;
	}
	
	public boolean getParallelizableIno() {
		return parallelizable;
	}
	
	public void setProducerIDs(int[] ids) {
		this.producerID = ids;
	}
	
	public void setParams(String[] params) {
		this.params = params;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setOpID(int opID) {
		this.opID = opID;
	}

        public void setOpName(String opName) {
		this.opName = opName;
	}
	
	public void setParallelizableInfo(boolean parallelizable) {
		this.parallelizable = parallelizable;
	}
}

