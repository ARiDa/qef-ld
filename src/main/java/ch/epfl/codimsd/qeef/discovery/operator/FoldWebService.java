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

import org.wsmo.common.Identifier;
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
 * The FoldWebService operator converts WebServicesExt objects to WsmoWebService objects. It is used
 * to preapare data for the input of FunctionalDiscovery operator.
 * 
 * @author Othman Tajmouati
 *
 */
public class FoldWebService extends Operator {
	
	/**
	 * Constructor.
	 * 
	 * @param id identifier of the operator.
	 * @param opNode opNode structure of this operator.
	 */
	public FoldWebService(int id, OpNode opNode) {
		
		// Call super constructor.
		super(id);
	}

	/**
	 * Get next result from previous operator in the chain. The method
	 * 
	 * @param consumerId id of the consumer (not used).
	 * @throws Exception
	 */
	 public DataUnit getNext(int consumerId) throws Exception {
		 
		 try {
			 
			 // Get next result (WebServiceExt object).
			DataUnit dataUnitWebService = super.getNext(0);
			
			// if the tuple is null there's no more produced tuples from the producer.
			if (dataUnitWebService == null)
				return null;
			
			// Get the WebService from the WebServiceExt object.
			Tuple tupleWebService = (Tuple) dataUnitWebService;
			WebServiceExt webServiceExt = (WebServiceExt) tupleWebService.getData(0);
			Identifier firstIdWebServiceExt = webServiceExt.getWebService().getIdentifier();
			WebService webServiceOfExt = webServiceExt.getWebService();
			
			// Flag used to check if we have receveid a new WebService.
			boolean newWS = false;
			
			// Flag used to check if we have received a null tuple.
			boolean dataUnitNull = false;
						
			while (!newWS && !dataUnitNull) {
			
				// Get other WebServiceExt object in order to construct the WsmoWebService containing
				// WebService objects.
				DataUnit dataUnit = super.getNext(0);
			 
				if (dataUnit == null) {
	
					dataUnitNull = true;
					
				} else {
				 
					Tuple tuple = (Tuple) dataUnit;
					WebServiceExt newWebServiceExt = (WebServiceExt) tuple.getData(0);
					Identifier idWebServiceExt = webServiceExt.getWebService().getIdentifier();
				 
					if (idWebServiceExt != firstIdWebServiceExt) {
					 
						newWS = true;
				
					} else {
					 
						Interface interfaceOfExt = newWebServiceExt.getInterface();
						webServiceOfExt.addInterface(interfaceOfExt);
					}
				}
			}
			
			// Construct the WsmoWebService.
			WsmoWebService wsmoWebService = new WsmoWebService(webServiceOfExt);
			Tuple tuple = new Tuple();
			tuple.addData((Type)wsmoWebService);
			
			return tuple;
			 
		} catch (Exception ex) {
			throw new Exception("Error in FoldWebService execution : " + ex.getMessage());
		}
	 }
	 
	public void setMetadata(Metadata[] thisMetadata) {
			
		try {
		
			metadata = new Metadata[1];
			metadata[0] = new TupleMetadata();
			Column column = new Column("WsmoWebService", Config.getDataType(Constants.WEBSERVICE_WSMO), 1, 0, false, 0);
			metadata[0].addData(column);
		
		} catch (Exception ex) {
			// This is never the case
		}
	}
}

