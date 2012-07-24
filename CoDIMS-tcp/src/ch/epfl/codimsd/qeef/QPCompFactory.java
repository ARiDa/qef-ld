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

import java.util.Iterator;
import java.util.Set;

import ch.epfl.codimsd.qeef.discovery.DiscoveryPlanManager;

import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.query.Request;
import ch.epfl.codimsd.query.QueryComponent;
import ch.epfl.codimsd.query.RequestParameter;
import ch.epfl.codimsd.qeef.PlanManager;
import ch.epfl.codimsd.qeef.operator.OperatorFactoryManager;
import ch.epfl.codimsd.qeef.trajectory.PlainFilePlanManager;
import ch.epfl.codimsd.qeef.trajectory.RelationalOpFactory;

/**
 * QpCompFactory performs a set of operations according to the request type.
 * For instance, when CoDIMS executes a Discovery request, the class loads
 * the DiscoveryPlanManager, and writes useful parameters into the BlackBoard.
 * 
 * Version 1
 * 
 * @author Othman Tajmouati
 * 
 * @date April 13, 2006
 */

public class QPCompFactory {
	
	@SuppressWarnings("unused")
	/**
	 * The request to execute.
	 */
	private Request request;
	
	/**
	 * Component containing objects depending on the reques type.
	 */
	private QueryComponent queryComponent;
	
	/**
	 * Default constructor. It performs a set of actions depending on the request type.
	 * 
	 * @param request the request.
	 */
	public QPCompFactory(Request request) {

		// Initializations.
		this.request = request;
		OperatorFactoryManager operatorFactoryManager = new OperatorFactoryManager();
              //  RelationalOpFactory relationalOpFactory = new RelationalOpFactory();
		PlanManager planManager = null;
		BlackBoard bl = BlackBoard.getBlackBoard();

		// Set of actions for a qos discovery request type.
		if (request.getRequestType() == Constants.REQUEST_TYPE_SERVICE_DISCOVERY) {

			// Construct the DiscoveryPlanManager.
			planManager = new DiscoveryPlanManager(operatorFactoryManager);

			// Get the request parameters and the goal from the request
			RequestParameter requestParameter = request.getRequestParameter();
			
			// Write the request parameters into the BlackBoard. The parameters
			// will be sent to remote nodes, hence they should be of string type.
			Set keys = requestParameter.keySet();
			Iterator itt = keys.iterator();
			while(itt.hasNext()) {
				String key = (String) itt.next();
				bl.put(key, requestParameter.getParameter(key));
			}

		} else if(request.getRequestType() == Constants.REQUEST_TYPE_TCP){

			// Construct the PlainFilePlanManager.
			planManager = new DiscoveryPlanManager(operatorFactoryManager);

                        // Get the request parameters and the goal from the request
			RequestParameter requestParameter = request.getRequestParameter();

			// Write the request parameters into the BlackBoard. The parameters
			// will be sent to remote nodes, hence they should be of string type.
			Set keys = requestParameter.keySet();
			Iterator itt = keys.iterator();
			while(itt.hasNext()) {
				String key = (String) itt.next();
				bl.put(key, requestParameter.getParameter(key));
			}
		}
                else if(request.getRequestType() == Constants.REQUEST_TYPE_SMOOTH){

			// Construct the PlainFilePlanManager.
			planManager = new DiscoveryPlanManager(operatorFactoryManager);

                        // Get the request parameters and the goal from the request
			RequestParameter requestParameter = request.getRequestParameter();

			// Write the request parameters into the BlackBoard. The parameters
			// will be sent to remote nodes, hence they should be of string type.
			Set keys = requestParameter.keySet();
			Iterator itt = keys.iterator();
			while(itt.hasNext()) {
				String key = (String) itt.next();
				bl.put(key, requestParameter.getParameter(key));
			}
		}
                else if(request.getRequestType() == Constants.REQUEST_TYPE_VOLUME_RENDERING){

			// Construct the PlainFilePlanManager.
			planManager = new DiscoveryPlanManager(operatorFactoryManager);

                        // Get the request parameters and the goal from the request
			RequestParameter requestParameter = request.getRequestParameter();

			// Write the request parameters into the BlackBoard. The parameters
			// will be sent to remote nodes, hence they should be of string type.
			Set keys = requestParameter.keySet();
			Iterator itt = keys.iterator();
			while(itt.hasNext()) {
				String key = (String) itt.next();
				bl.put(key, requestParameter.getParameter(key));
			}
		}
                else{
                        // Use the DiscoveryPlanManager as default plan manager
                        planManager = new DiscoveryPlanManager(operatorFactoryManager);
                }
		
		// Construct the query component and encapsulate the planManager in it.
		queryComponent = new QueryComponent();
		queryComponent.setPlanManager(planManager);
	}

	/**
	 * @return QueryComponent object.
	 */
	public QueryComponent getQueryComponent() {
		return queryComponent;
	}
}

