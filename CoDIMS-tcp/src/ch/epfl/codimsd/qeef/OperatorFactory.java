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

import ch.epfl.codimsd.qep.OpNode;

/** 
 * The OperatorFactory class defines common functionalities for CoDIMS operator factories.
 *
 * Added by Othman :
 * - change the public abstract Operator  createOperator(int id, OpNode opNode, BlacBoard blacBoard) to createOperator(int id, OpNode opNode)
 * - change public abstract Operator createOperator(int opId, String opName, String[] params, DataSourceManager dsManager, BlackBoard blackBoard) throws Exception; 
 * to public abstract Operator createOperator(int opId, String opName, String[] params, DataSourceManager dsManager) throws Exception
 * - Translated javadoc.
 * 
 * @author Fausto Ayres, Vinicius Fontes, Othman Tajmouati.
 */
public abstract class OperatorFactory {
   
    /**
     * Create an operator.
     * 
     * @param opId operator id.
     * @param opNode the OpNode structure of the operator. It contains all the information needed to construct an operator.
     * @param dsManager the dataSourceManager for dataSources creation.
     * @return the operator instance.
     * @throws Exception
     */
	public abstract Operator createOperator(int opId, String opName, String[] params, DataSourceManager dsManager) throws Exception;

	/**
	 * Create an operator.
	 * 
	 * @param opId
	 * @param opNode
	 * @return the operator instance.
	 */
	public abstract Operator createOperator(int opId, OpNode opNode);
}   

