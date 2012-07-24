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

public abstract class G2NNode  {
	
	/**
	 * Fragment identifier.
	 */
	public int id;

	/**
	 * Number of instances to be processed by each fragment.
	 */
	public int nrInstances;
	
	/**
	 * Nr rate ms/tupla
	 * Troughput used by  G2N
	 */
	public int rate;	
	
	public G2NNode(int id, int nrInstances, int rate){
		this.id = id;
		this.nrInstances = nrInstances;
		this.rate = rate;
	}
	
	/**
	 * 
	 * @return node evaluation cost in miliseconds
	 */
	public abstract float evaluationCost(int nrNode);

	/**
	 * How many tuples this node can evaluate in time
	 * @param time execution time in ms
	 * @return tuples that this node can evaluate during this time
	 */
	public abstract int ableToProcess( float cost);


}

