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

import java.util.HashMap;
import java.util.Vector;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.ExecutionState;
import ch.epfl.codimsd.qeef.util.Constants;

/**
 * Structure containing useful informations on the remote nodes. This structure is put
 * in the BlackBoard and access by CoDIMS component.
 * 
 * @author Othman Tajmouati.
 */
public class G2NInfoNodes {
	
	/**
	 * Vector of remote nodes.
	 */
	private Vector<CodimsNode> codimsNodes;
	
	/**
	 * Constructor. It sets the set of remote nodes according to what G2N had scheduled.
	 * 
	 * @param nodes set of nodes already scheduled by G2N.
	 * @param nodeAddresses addresses of the nodes.
	 */
	public G2NInfoNodes(G2NNode[] nodes, HashMap<Integer, String> nodeAddresses, long id) {
		
		// Initializations.
		codimsNodes = new Vector<CodimsNode>();
		BlackBoard bl = BlackBoard.getBlackBoard();
		
		// If there's only one remote node, then the execution is done locally
		if (nodes == null)
			return;

		// If there's only one remote node, use the local node to execute the request.
		if (nodes.length != 1) {
	
			// Build the real set of remote nodes. A node is considred as remote, if G2N
			// has allocated to it a non null initial numer of tuples.
			for (int i = 0; i < nodes.length; i++) {
				if (nodes[i].nrInstances != 0) {
					CodimsNode codimsNode = new CodimsNode(nodes[i].id, nodeAddresses.get(new Integer(nodes[i].id)), 
							nodes[i].rate, nodes[i].nrInstances);
					codimsNodes.add(codimsNode);
				}
			}
                        
			// Put the execution into the BlackBoard.
//			bl.put(Constants.EXEC_ASYNC+id, new ExecutionState(codimsNodes));
//

			// Put the number of nodes, the number of initial tuples and the production rates in the BlackBoard.
			bl.put(Constants.NR_NODES, this.getNodes().length + "");
			bl.put(Constants.NR_TUPLES, this.getNumberOfTuples());
			bl.put(Constants.PROD_RATES, this.getProdRates());
		}
	}
	
	/**
	 * @return the number of remote nodes.
	 */
	public int getNumberOfNodes() {
		return codimsNodes.size();
	}
	
	/**
	 * @return the addresses of the remote nodes.
	 */
	public String[] getNodes() {
		
		String[] nodes = new String[codimsNodes.size()];
		
		for (int i = 0; i < nodes.length; i++) {
			CodimsNode cn = codimsNodes.get(i);
			nodes[i] = cn.getAddress();
		}
		
		return nodes;
	}
	
	/**
	 * @return the inial number of tuples for each node.
	 */
	public String[] getNumberOfTuples() {
		
		String[] nrOfTuples = new String[codimsNodes.size()];
		
		for (int i = 0; i < nrOfTuples.length; i++) {
			CodimsNode cn = codimsNodes.get(i);
			nrOfTuples[i] = cn.getNrInstances() + "";
		}
		
		return nrOfTuples;
	}
	
	/**
	 * @return the production rates for each node.
	 */
	public String[] getProdRates() {
		
		String[] prodRates = new String[codimsNodes.size()];
		
		for (int i = 0; i < prodRates.length; i++) {
			CodimsNode cn = codimsNodes.get(i);
			prodRates[i] = cn.getRate() + "";
		}
		
		return prodRates;
	}
}

