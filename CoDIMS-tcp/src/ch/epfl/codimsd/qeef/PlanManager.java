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

import ch.epfl.codimsd.exceptions.initialization.InitializationException;
import ch.epfl.codimsd.exceptions.operator.OperatorException;
import ch.epfl.codimsd.exceptions.optimizer.OptimizerException;
import ch.epfl.codimsd.qep.QEP;
import ch.epfl.codimsd.query.Request;

/**
 * The PlanManager interface defines common functionanlites for CoDIMS plan managers.
 *
 * @author Othman Tajmouati.
 */
public interface PlanManager {
	
	/**
	 * Creaates a plan based on a request.
	 * 
	 * @param request the request to execute.
	 * @return a query execution plan.
	 * @throws InitializationException
	 * @throws OptimizerException
	 * @throws OperatorException
	 */
	public QEP instantiatePlan(Request request)  
		throws InitializationException, OptimizerException, OperatorException;
}

