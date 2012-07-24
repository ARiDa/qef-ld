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
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.wsmo4j.common.ClearTopEntity;
import org.wsmo.common.IRI;
import org.wsmo.common.TopEntity;
import org.wsmo.factory.Factory;
import org.wsmo.service.Interface;
import org.wsmo.service.WebService;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.Serializer;

import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.types.Type;
//import vtk.vtkDirectory;

/**
 * The WebServiceExt is a CoDIMS types containing a Wsmo WebService + 
 * a Wsmo Interface. It implements the class Type of CoDIMS which defines
 * all the functionalities of a CoDIMS type.
 * 
 * @author Othman Tajmouati.
 *
 */
public class WebServiceExt implements Type {
	
	/**
	 * For the serializer.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The Wsmo Webservice.
	 */
	private WebService webService;
	
	/**
	 * The Wsmo interface.
	 */
	private Interface wsmoInterface;
	
	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(WebServiceExt.class.getName());
	
	/**
	 * Default constructor.
	 */
	public WebServiceExt() {}
	
	/**
	 * Constructor using the webservice and the interface to construct the
	 * WebServiceExt object.
	 * 
	 * @param webService wsmo Webservice.
	 * @param wsmoInterface wsmo Interface.
	 */
	public WebServiceExt(WebService webService, Interface wsmoInterface) {
		
		this.webService = webService;
		this.wsmoInterface = wsmoInterface;
	}
	
	/**
	 * Set the WebService.
	 * @param webService wsmo Webservice.
	 */
	public void setWebService(WebService webService) {
		this.webService = webService;
	}
	
	/**
	 * Set the Interface.
	 * @param wsmoInterface wsmo Interface.
	 */
	public void setInterface(Interface wsmoInterface) {
		this.wsmoInterface = wsmoInterface;
	}
	
	/**
	 * @return the wsmo Webservice.
	 */
	public WebService getWebService() {
		return this.webService;
	}

	/**
	 * @return the wsmo Interface.
	 */
	public Interface getInterface() {
		return this.wsmoInterface;
	}
	
	/**
	 * Serialize this WebServiceExt to a String object. The String is
	 * written afterwards to a DataOutputStream.
	 * 
	 * @param out DataOutputStream where to write the string.
	 */
	public void write(DataOutputStream out) throws IOException {

		// Create a wsmo serializer and serialize the webservice.
		Serializer ser = Factory.createSerializer(null);
		StringBuffer str = new StringBuffer();
        TopEntity[] tops = { webService };
        ser.serialize(tops, str);
        
        // Get the IRI of the Interface and serialize the name.
        IRI id = (IRI) wsmoInterface.getIdentifier(); 
        String myString2 = id.getLocalName();
        String myString = str.toString();
        
        // Write the strings to the output stream.
        out.writeUTF(myString);
        out.writeUTF(myString2);
	}
	
	/**
	 * Deserialize the WebServiceExt from string to java object.
	 * 
	 * @param in the DataInputStream containing the string representation of the WebServiceExt.
	 * @return the object we have deserialized.
	 */
	public Type read(DataInputStream in) throws IOException { 
		
		try {

			// Clears the webservice from wsmo cache. If we don't perform
			// this operation, the string is written twice.
			ClearTopEntity.clearTopEntity(webService);
	    	
			// Read the webservice and the interface iri.
			String webServiceString = in.readUTF();
			String localName = in.readUTF();
			
			// Construct the Webservice object.
			StringBuffer stringBuffer1 = new StringBuffer(webServiceString);
			Parser parser = Factory.createParser(null);
			TopEntity[] topEntities1 = parser.parse(stringBuffer1);
			webService = (WebService) topEntities1[0];
			
			// Get the list of interfaces and construct the one corresponding to the iri we has
			// deserialized before.
			Set<Interface> interfaceSet = webService.listInterfaces();
			Iterator<Interface> itInterfaces = interfaceSet.iterator();
			boolean interfaceFound = false;

			while (itInterfaces.hasNext() && (interfaceFound == false)) {
				
				Interface tempWsmoInterface = (Interface) itInterfaces.next();
				IRI iri = (IRI) tempWsmoInterface.getIdentifier();
				
				if (iri.getLocalName().equalsIgnoreCase(localName)) {
					wsmoInterface = tempWsmoInterface;
					interfaceFound = true;
				}
			}
			
			parser = null;
			
		} catch (Exception ex) {
			throw new IOException("Cannot deserialize WebServiceExt object : " + ex.getMessage());
		}

		return new WebServiceExt(webService, wsmoInterface);
	}

        public Type readSmooth(String in) throws IOException {
            return null;
        }

/*        public Type readImages(vtkDirectory in, String dir, int nrOfDivisions) throws IOException {
            return null;
        }
	
	/**
	 * XXX TODO Those methods are not implemented but inherited.
	 */
	public  void setValue(String value) {}
	public  void setValue(Object value) {}
	public Object clone() {return null;}
	public void finalize() throws Throwable {}
	public int compareTo(Object o) { return 0;}
	public void display(Writer out) throws IOException {}
	public int displayWidth() { return 0; }
	public Type newInstance() { return new WebServiceExt(); }
	public String recognitionPattern() { return null; }
	public void setMetadata(Data data){}
}

