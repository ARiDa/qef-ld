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

import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.globus.examples.stubs.DQEEService_instance.DQEEPortType;
import org.globus.examples.stubs.DQEEService_instance.service.DQEEServiceAddressingLocator;

/**
 * The WebServiceFactory class creates a GT4 service. It locates the node and
 * create an end-point. For more informations see the Globus tutorial.
 * 
 * @author Othman Tajmouati.
 *
 */
public class WebServiceFactory {
	
	/**
	 * Default constructor.
	 */
	public WebServiceFactory() {}
	
	/**
	 * Creates a GT4 service.
	 * 
	 * @param GSH - The GSH (Adress) of the service.
	 * @return the DQEE Port type (interface of WS).
	 */
	public synchronized DQEEPortType createService(String GSH) {
		
		// Initialize the stub class
		// Given the service's endpoint, it returns a DQEEPortType object that will allow us 
		// to contact the DQEE portType.
		DQEEServiceAddressingLocator locator = new DQEEServiceAddressingLocator();
		DQEEPortType dqee = null;
		
		try {
			
			String serviceURI = GSH;

			// Create an EndpointReferenceType object representing the endpoint reference of this service
			EndpointReferenceType endpoint = new EndpointReferenceType();
			endpoint.setAddress(new Address(serviceURI));
			
			// Obtain a reference to the service's portType.
			dqee = locator.getDQEEPortTypePort(endpoint);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// Return the DQEE portType.
		return dqee;
	}
}

