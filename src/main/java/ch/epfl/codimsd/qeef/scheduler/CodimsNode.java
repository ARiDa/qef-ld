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
package ch.epfl.codimsd.qeef.scheduler;

/**
 * This structure is used as a wrapping between G2NNode and FragmentInfo. It is used by
 * the DiscoveryOptimizer to constrcut the set of remoted nodes that will handle the
 * execution of a request.
 * 
 * @author Othman Tajmouati.
 */
public class CodimsNode {

	/**
	 * Identifier of one remote node.
	 */
	private int id;
	
	/**
	 * Complete address of the node.
	 */
	private String address;
	
	/**
	 * Sum of rates of the operators that execute on this node.
	 */
	private int rate;
	
	/**
	 * The number of initial tuples that are allocated to this node.
	 */
	private int nrInstances;
	
	/**
	 * Default constructor.
	 *   
	 * @param id id of the node.
	 * @param address address of the node.
	 * @param rate rate of the operators.
	 * @param nrInstances number of initial tuples.
	 */
	public CodimsNode(int id, String address, int rate, int nrInstances) {
		
		this.address = address;
		this.id = id;
		this.rate = rate;
		this.nrInstances = nrInstances;
	}
	
	/**
	 * @return the identifier of the node.
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * @return the address of the node.
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * @return the rate allocated to this node.
	 */
	public int getRate() {
		return rate;
	}
	
	/**
	 * @return initial number of tuples.
	 */
	public int getNrInstances() {
		return nrInstances;
	}

}

