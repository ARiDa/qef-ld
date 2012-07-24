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
package ch.epfl.codimsd.qeef.discovery.operator;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.wsmo.service.Interface;
import org.wsmo.service.WebService;

import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.relational.Column;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.types.WebServiceExt;
import ch.epfl.codimsd.qeef.types.WsmoWebService;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qep.OpNode;

/**
 * The UnfoldWebService operator converts a set of WsmoWebService objects to WebServicesExt objects.
 * It unfolds each Wsmo webservice to a set of WebServiceExt objects, each WebServiceExt instance 
 * containing one wsmo webservice and one wsmo interface. It has been developed for the FunctionalDiscovery
 * operator which reads a wsmo webservices.
 * 
 * @author Othman Tajmouati
 */
public class UnfoldWebService extends Operator {

	/**
	 * Buffer storing the WebServiceExt objects.
	 */
	private Vector<WebServiceExt> buffer;
	
	/**
	 * Constructor.
	 * 
	 * @param id identifier of this operator.
	 * @param opNode opNode structure for this operator.
	 */
	public UnfoldWebService(int id, OpNode opNode) {
		
		// Call superconstructor.
		super(id);
		
		// Intializations.
		this.buffer = new Vector<WebServiceExt>();
		metadata = null;
	}
	
	/**
	 * Get next result from producer. This method get a WsmoWebService and
	 * returns a WebServiceExt object. It unfolds the WsmoWebService in a buffer using the
	 * private method @see ch.epfl.codimsd.qeef.discovery.operator.UnfoldWebService#createWebServiceExtEntry(DataUnit)
	 * and returns the first WebServiceExt object in this buffer.
	 * 
	 * @param consumerId id of the consumer (not used).
	 * @throws Exception
	 */
	public DataUnit getNext(int consumerId) throws Exception {
		 
		 try {
		 
			 // Get the DataUnit containing the WwsmoWebService from the producer
			 DataUnit dataUnitWebService = super.getNext(0);
			 
			 // Add a WebServiceExt object in the buffer
			 if (dataUnitWebService != null)
				 createWebServiceExtEntry(dataUnitWebService);
			
			 // Check if the buffer is empty and return the first element if not
			 if (buffer.isEmpty()) {
				 return null;
			 } else {
				 
				 Tuple tuple = new Tuple();
				 tuple.addData((Type)buffer.firstElement());
				 buffer.remove(0);
				 return tuple;
			 }
			 
		 } catch (Exception ex) {
			 throw new Exception("Error in UnfoldWebServiceOperator execution : " + ex.getMessage());
		 }
	 }
	 
	/**
	 * Close the operator.
	 */
	public void close() {
		buffer = null;
	}
	
	/**
	 * Set the metadata for this operator. The Column contains a WebServiceExt object.
	 * 
	 * @param thisMetadata the metadata to set (not used, the operator creates its own).
	 */
	public void setMetadata(Metadata[] thisMetadata) {
		
		try {
			metadata = new Metadata[1];
			metadata[0] = new TupleMetadata();
			Column column = new Column("WebServiceExt", 
				 Config.getDataType(Constants.WEBSERVICE_EXT), 1, 0, false, 0);
			metadata[0].addData(column);
		} catch (Exception ex) {
			// This is never the case
		}
	 }
	 
	/**
	 * Convert a WsmoWebService object to a list of WebServiceExt objects and add the list
	 * to a buffer.
	 * 
	 * @param dataUnit dataUnit containing a WsmoWebService.
	 */
	private void createWebServiceExtEntry(DataUnit dataUnit) {
		 
		// Get the WsmoWebService from the DataUnit
		Tuple tupleWebService = (Tuple) dataUnit;
		WsmoWebService wsmoWebService = (WsmoWebService) tupleWebService.getData(0);
		WebService webService = wsmoWebService.getWebService();
		
		// Unfold the WebService interfaces, construct the WebServiceExt object and put each one in 
		// the buffer
		Set interfaces = webService.listInterfaces();
		Iterator itInterfaces = interfaces.iterator();

		while (itInterfaces.hasNext()) {
			 
			WebServiceExt webServiceExt = new WebServiceExt(webService, (Interface)itInterfaces.next());
			buffer.add(webServiceExt);
		}
	}
}

