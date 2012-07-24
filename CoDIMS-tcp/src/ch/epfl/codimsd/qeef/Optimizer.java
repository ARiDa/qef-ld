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
package ch.epfl.codimsd.qeef;

import ch.epfl.codimsd.exceptions.optimizer.OptimizerException;
import ch.epfl.codimsd.qep.QEP;

/**
 * The Optimizer class defines common functions for all CoDIMS optimizers.
 * 
 * Version 1
 * 
 * @author Othman Tajmouati
 * 
 * @date April 13, 2006
 */

public abstract class Optimizer {
	
	/**
	 * Default constructor.
	 */
	public Optimizer() {}
	
	/**
	 * Optimze the query execution plan.
	 * @param qep query execution plan to optimize.
	 * @return true if the QEP is sent to remote nodes, false otherwise.
	 * @throws OptimizerException
	 */
	public abstract boolean optimize(QEP qep) throws OptimizerException;
}

