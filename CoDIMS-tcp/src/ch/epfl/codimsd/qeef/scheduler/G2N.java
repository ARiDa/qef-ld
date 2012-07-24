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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.datastructure.BinaryHeap;

import ch.epfl.codimsd.qeef.operator.control.FragmentInfo;


/**
 * Othman changes :
 * - remove int newCost in scheduleMultipleNodes(...)
 * @author lbdadmin
 *
 */
public class G2N {
		
	/**
	 * Log4 logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(G2N.class.getName());
    
	
	public G2N(){
		super();
	}
	/**
	 * schedule.
	 * Entry point for the parallel optimizer. G2N receives aset of available nodes with
	 * information on their thorughput and the total number of instances to be evaluated.
	 * It computes a set Q={Q1,Q2,..Qn} identifying the selected nodes and the total 
	 * number of tuples to be sent for each node. 
	 */
	public synchronized G2NNode[] schedule(Collection nodes, int nrInstances){
		 		
		int currNode;
		G2NNode aux[];
		
		if(nodes.size() < 1){
			logger.warn("The number of nodes to be scheduled in G2N must be greater than 1.");
			return null;
		}
		
		//Prepare the nodes list
		//set number of instance to be processed by each node to 0.
		Iterator it = nodes.iterator();
		aux = new G2NNode[nodes.size()];
		for(int i=0; it.hasNext();i++) {
			 
			aux[i] = (G2NNode)it.next(); 
			aux[i].nrInstances = 0;						
		}
		Arrays.sort(aux, new RateComparator());
		
		//Try to use new nodes
		currNode = 0;
		for(int i=0; i < aux.length; i = currNode) {				
						
			// logger.debug("Trying to Allocate node ID ;" + aux[i].id);
			logger.debug("--------------------");
			currNode = selectNode(aux, i, nrInstances); 
			logger.debug("Round number : " + i);
			for (int j = 0; j <= i; j++) {
				logger.debug("Node " + j + " : " + aux[j].evaluationCost(j) + " ; with instances : " + aux[j].nrInstances);
			}
			logger.debug("--------------------");
			//printNodes(aux);

			if ( currNode <= i )
				break;				 
		}
		
		//Prepares the result, put it into a new vector, 
//		selNodes = new Vector();
//		for(int i=0; i < currNode; i++){
//			selNodes.add(aux[i]);
//		}		
		
		// logger.info("G2N-----------------------------------------------------------------------------");
		// logger.info("G2N: Final Allocation with  Q = " + nrInstances );
		// printNodes(aux);
				
		return aux;
	}
	
	private int selectNode(G2NNode[] nodes, int newNode, int nrInstances){

		BinaryHeap heap;
		G2NNode highestCostNode;
		float highestCost, newCost, newNodeCost, startCost;
		
		if( newNode == 0 ){
			nodes[newNode].nrInstances = nrInstances;
			return newNode+1;
		}
		
		//Creates the heap
		heap = new BinaryHeap(nodes.length, new EvaluationCostComparator());
		for(int i=0; i < newNode; i++)
			heap.insert(nodes[i]);
		
		highestCostNode = (G2NNode)heap.deleteMax();
		startCost = highestCostNode.evaluationCost(newNode);
		newCost = highestCostNode.evaluationCost(newNode);

		do{			

			//If the cost of the worst node was reduced and 
			//now it doesn't represent the greater cost .
			//Them you chage it.
			// Othman : (G2NNode)heap.findMax()).evaluationCost(newNode) > newCost  : it exists a node in the heap that is greater than the actual
			// compute cost node (which is newCost : highestCostNode updated).
			if ( !heap.isEmpty() && ((G2NNode)heap.findMax()).evaluationCost(newNode) > newCost ) {

				G2NNode auxNode = highestCostNode;
				highestCostNode = (G2NNode)heap.deleteMax();
				heap.insert(auxNode);
			} 
			
			highestCost = highestCostNode.evaluationCost(newNode);
			
			//if there are more than one node with the same cost
			//The nodes to be scheduled have to support at least one instance 
			//of each node with the same cost. Otherwise the elapsed
			//time will not be reduced. 		
			int aux=0;
			while( !heap.isEmpty() && ((G2NNode)heap.findMax()).evaluationCost(newNode) == highestCost ){							
				heap.insert(highestCostNode);

				aux = scheduleMultipleNodes(nodes, newNode, heap);
				
				highestCostNode = (G2NNode)heap.deleteMax();
				highestCost = highestCostNode.evaluationCost(newNode); 
					
				if(aux == -1){
					//Apenas para compensar na saida
					highestCostNode.nrInstances--;
					nodes[newNode].nrInstances++;
					break;
					
				} else {
					newNode = aux;
				}
			}
			
			if(aux == -1)
				break;
			
			highestCostNode.nrInstances--;
			nodes[newNode].nrInstances++;
			
			newCost = highestCostNode.evaluationCost(newNode);
			newNodeCost = nodes[newNode].evaluationCost(newNode);
			
			//printNodes(nodes);
			//logger.debug(" Custo No Mais Caro " + newCost + " Custo Antigo " + highestCost + " newNodeCost " + newNodeCost );
			
		} while(newCost < highestCost && newNodeCost < highestCost);
		
		//redo the last distribution
		highestCostNode.nrInstances++;
		nodes[newNode].nrInstances--;
		
		//If new Node Contributes to reduce the cost
		if(highestCost >= startCost)
			return newNode;
		else
			return newNode + 1;
	}
	
	private int scheduleMultipleNodes(G2NNode nodes[], int currNode, BinaryHeap heap){
		
		Vector<Object> highCostNodes;
		G2NNode mostExpNode;
		float cost, mostExpCost, reducedCost, auxCost;
		int ret;
		//find out nodes presenting the same highest cost
		highCostNodes = new Vector<Object>();
		cost = ((G2NNode)heap.findMax()).evaluationCost(currNode);
		
		// logger.debug("Tentativa de Desempate Custo: " + cost);
		
		while( !heap.isEmpty() && ((G2NNode)heap.findMax()).evaluationCost(currNode) == cost ){
			
			highCostNodes.add( heap.deleteMax() );
		}
		//Find among nodes already selected the one with highest cost.
		// (It may be the case that there is a set made of equally costy 
		//nodes once one tuple has been substracted.)
		// and picks the first one
		mostExpNode = ((G2NNode)highCostNodes.get(0));
		mostExpNode.nrInstances--;
		mostExpCost = mostExpNode.evaluationCost(currNode);
		mostExpNode.nrInstances++;
		
		for(int i=0; i < currNode; i++) {
			
			G2NNode auxNode = nodes[i]; 
			
			//Is one of equal cost
			if( highCostNodes.contains(auxNode)) {
				//equal cost nodes hold a single tuple, can't be reduced
				if( auxNode.nrInstances == 1 ){ 
					mostExpNode = auxNode;
					mostExpCost = mostExpNode.evaluationCost(i);
					break;
				}				
				auxNode.nrInstances--;
				auxCost = auxNode.evaluationCost(i);
				auxNode.nrInstances++;
			} else {
				auxCost = auxNode.evaluationCost(i);
			}
			
			if( auxCost > mostExpCost ) {
				mostExpNode = auxNode;
				mostExpCost = auxCost;
			}
		}
		
		//if the highest cost is still kept by one of the previous
		// highest cost nodes, verifies whether reduction has been made
		// and computes its amount.
		//If the highest cost is not in the original set, then a reduction
		// has been obtained, no matter how much has been reduced.
		// we can finish this set analysis.
		//
		if( highCostNodes.contains(mostExpNode))
			reducedCost = mostExpNode.evaluationCost(currNode) - mostExpCost;
		else
			reducedCost = 1;
			
		
		//Try to allocate K instances to the nodes to be scheduled
		//Each node may receive at least 2B instances
		int k = highCostNodes.size();		
		
		if(reducedCost <= 0){
			ret = -1;
			// logger.info("It wasn't possible to reduce the cost by substracting one tuple from the highest cost node.");			
		} else {							
			ret = tryAllocate(k, mostExpCost, currNode, nodes);			
		}
		
		//insert the elements into the heap again
		//Reduces one instance from each node if the allocation was possible
		for(int i=0; i < k; i++){
			G2NNode aux = (G2NNode)highCostNodes.get(i);
			if(ret > -1)
				aux.nrInstances--;
			heap.insert( aux );
		}
		//if more than one node was allocated, insert all of them but the last.
		//
		for(int i=currNode; i < ret; i++){
			heap.insert( nodes[i] );
		}
		
		return ret;
	}
	
	/**
	 * 
	 * Considers that at least one node is available 
	 * @param nrInstances
	 * @param maxCost
	 * @param curr
	 * @param nodes
	 * 
	 * @return -1 if a new allocation wasn't pssible. A nr > curr indicates that a position
	 *     of the last node receiving tuples.
	 */
	public int tryAllocate(int nrInstances, float maxCost, int curr, G2NNode []nodes){
		
		int supportedInstances;
		int aux;
		
		if( curr >= nodes.length )
			return -1;
		
		supportedInstances = nodes[curr].ableToProcess( maxCost ); 
		
		//if supportedInstances < 0 then
		// the nr of instances it holds alaready gives it a cost> time
		//if supportedInstances == 0, we have two possibilities:
		//1-it holds some tuples and if receives one more, its cost overtakes  time
		//2-it has no tuples and can't evaluate even a single tuple with cost= time			
		if( supportedInstances < 0 ){
			return -1;
		}						
			
		//node succeeded allocation
		if( nrInstances - supportedInstances <= 0){
		
			nodes[curr].nrInstances += nrInstances;
			return curr;
			
		} else { //try others
			
			aux = tryAllocate(nrInstances-supportedInstances, maxCost, curr+1, nodes);
			if( aux > -1 ){ //succeeded allocation
				
				nodes[curr].nrInstances += supportedInstances;
				return aux;
				
			} else{ //allocation not possible
				
				return -1;
			}				
		}		
	}
	
	public static void main(String[] args) throws Exception{
	
		Vector<FragmentInfo> nodes =  new Vector<FragmentInfo>();
		FragmentInfo node;
		
		node = new FragmentInfo(1, 164, 2500, (long)52000, 100); 
		nodes.add(node);
		
		node = new FragmentInfo(2, 165, 2500, (long)52000, 0);
		nodes.add(node);
		
		node = new FragmentInfo(3, 166, 2500, (long)52000, 0);
		nodes.add(node);
		
		node = new FragmentInfo(4, 166, 2500, (long)52000, 0);
		nodes.add(node);
		
		node = new FragmentInfo(5, 167, 2500, (long)52000, 0);
		nodes.add(node);
		
		node = new FragmentInfo(6, 170, 2500, (long)52000, 0);
		nodes.add(node);
		
		node = new FragmentInfo(7, 171, 2500, (long)52000, 0);
		nodes.add(node);
		
		G2N g2n = new G2N();		
		g2n.schedule(nodes, 423);
	}
	
	
	@SuppressWarnings("unused")
	private void printNodes(G2NNode []nodes){
		
		// logger.info("G2N-----------------------------------------------------------------------------");
		// for(int i=0; i < nodes.length; i++)
			//logger.info("G2N ; ID ; " + nodes[i].id + " ; Rate ; " + nodes[i].rate +  " ; NumberOfParticules ; " +  nodes[i].nrInstances + " ; Cost ; " + nodes[i].evaluationCost());
		//logger.info("G2N-----------------------------------------------------------------------------");		
		
	}
}

