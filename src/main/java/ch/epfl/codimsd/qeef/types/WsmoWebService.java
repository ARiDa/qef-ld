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
package ch.epfl.codimsd.qeef.types;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.common.ClearTopEntity;
import org.wsmo.common.TopEntity;
import org.wsmo.factory.Factory;
import org.wsmo.service.WebService;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.Serializer;

import ch.epfl.codimsd.qeef.Data;
//import vtk.vtkDirectory;

/**
 * WsmoWebService is a CoDIMS type containing a Wsmo WebService.
 * 
 * @author Othman Tajmouati.
 */
public class WsmoWebService implements Type {

	/**
	 * For the serializer.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The Wsmo Webservice.
	 */
	private WebService webService;
	
	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(WsmoWebService.class.getName());
	
	/**
	 * Constructor that builds the object right now.
	 * 
	 * @param webService the wsmo WebService.
	 */
	public WsmoWebService(WebService webService) {
		this.webService = webService;
	}
	
	/**
	 * Default constructor.
	 */
	public WsmoWebService() {}
	
	/**
	 * Set the WebService.
	 * @param webService wsmo WebService.
	 */
	public void setWebService(WebService webService) {
		this.webService = webService;
	}
	
	/**
	 * @return the WebService.
	 */
	public WebService getWebService() {
		return this.webService;
	}
	
	/**
	 * Serialize this WsmoWebService to a String object. The String is
	 * written afterwards to a DataOutputStream.
	 * 
	 * @param out DataOutputStream where to write the string.
	 */
	public void write(DataOutputStream out) throws IOException {

		Serializer ser = Factory.createSerializer(null);
		StringBuffer str = new StringBuffer();
		TopEntity[] tops = { webService };
		ser.serialize(tops, str);
		String myString = str.toString();
		out.writeUTF(myString);
	}
		
	/**
	 * Deserialize the WsmoWebService from string to java object.
	 * 
	 * @param in the DataInputStream containing the string representation of the WsmoWebService.
	 * @return the object we have deserialized.
	 */

	public Type read(DataInputStream in) throws IOException { 

		try {

			ClearTopEntity.clearTopEntity(webService);
			String webServiceString = in.readUTF();
						
			StringBuffer stringBuffer1 = new StringBuffer(webServiceString);
			Parser parser = Factory.createParser(null);
			TopEntity[] topEntities1 = parser.parse(stringBuffer1);
			webService = (WebService) topEntities1[0];
			parser = null;
			
		} catch (Exception ex) {
			throw new IOException("Cannot deserialize WsmoWebService object : " + ex.getMessage());
		}

		return new WsmoWebService(webService);
	}

        public Type readSmooth(String in) throws IOException {
            return null;
        }

/*        public Type readImages(vtkDirectory in, String dir, int nrOfDivisions) throws IOException {
            return null;
        }
	
	/**
	 * XXX TODO Methods inherited but not implemented.
	 */
	public void finalize() throws Throwable {}
	public int compareTo(Object o) { return 0;}
	public void display(Writer out) throws IOException {}
	public int displayWidth() { return 0; }
	public Type newInstance() { return new WebServiceExt(); }
	public String recognitionPattern() { return null; }
	public  void setValue(String value) {}
	public  void setValue(Object value) {}
	public Object clone() { return null ; }
	public void setMetadata(Data data){}
}

