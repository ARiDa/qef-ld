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
package ch.epfl.codimsd.qeef.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.common.ClearTopEntity;
import org.wsmo.common.Identifier;
import org.wsmo.common.TopEntity;
import org.wsmo.factory.Factory;
import org.wsmo.service.WebService;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.Serializer;

import ch.epfl.codimsd.qeef.SystemConfiguration;
import ch.epfl.codimsd.qeef.discovery.datasource.DataSourceWsmoText;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.WebServiceExt;

public class ComplexType<Obj> implements Serializable {

	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(WebServiceExt.class.getName());
	
	/**
	 * Undefined object to store in this complex type.
	 */
	private transient Obj obj;

	/**
	 * For the serialiazer.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor of a complex type.
	 */
	public ComplexType(Obj obj) {
		this.obj = obj;
	}
	
	public void writeObject(ObjectOutputStream s) throws IOException, ClassNotFoundException {
		
		Serializer ser = Factory.createSerializer(null);
		StringBuffer stringBuffer = new StringBuffer();
        TopEntity[] tops = { (WebService) obj };
        ser.serialize(tops, stringBuffer);
        s.writeUTF(stringBuffer.toString());
        logger.debug(stringBuffer.toString());
        
        try {
        	ClearTopEntity.clearTopEntity((WebService)obj);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
        obj = null;
        s.flush();
		s.close();
	}
	
	@SuppressWarnings("unchecked")
	public void readObject(ObjectInputStream s) throws IOException {
	
		String webServiceString = s.readUTF();
		StringBuffer stringBuffer = new StringBuffer(webServiceString);
		Parser parser = Factory.createParser(null);
		try {
			TopEntity[] topEntities1 = parser.parse(stringBuffer);
			obj = (Obj) topEntities1[0];
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		WebService webService = (WebService) obj;
		Identifier id = webService.getIdentifier();
		String idString = id.toString();
		logger.debug(idString);
	}
	
	/**
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
//		String fileName = "C:\\QoS-DiscoveryComponent\\Workspace\\Projects\\" +
//				"QoSDiscoveryComponent\\src\\codims-home\\dataSources\\webServices.wsml";
//		
		try {
			
			SystemConfiguration.loadSystemConfiguration();
			
			DataSourceWsmoText dtSource = new DataSourceWsmoText("WSMO", null, "webServices.wsml");
			
			ByteArrayOutputStream out;
			ObjectOutputStream objOut;
			out = new ByteArrayOutputStream();
		
			dtSource.open();
			Tuple tuple = (Tuple) dtSource.read();
			WebServiceExt webServiceExt = (WebServiceExt) tuple.getData(0);
			WebService wb = webServiceExt.getWebService();
			ComplexType<WebService> complexType = new ComplexType<WebService>(wb);
				
			objOut = new ObjectOutputStream(out);
			complexType.writeObject(objOut);
			
			byte[] returnBytes = out.toByteArray();
			DataHandler dh = new DataHandler(
					new org.apache.turbine.util.mail.ByteArrayDataSource
						(returnBytes,"application/octet-stream"));

			DataHandler dh2 = dh;
			
			//Object o = (new ObjectInputStream(dh2.getInputStream())).readObject(); 
			ComplexType<WebService> complexType2 = new ComplexType<WebService>(null);
			complexType2.readObject(new ObjectInputStream(dh2.getInputStream()));
			// logger.debug(o.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}

