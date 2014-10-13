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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import javax.activation.DataHandler;

public class Serializer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DataOutputStream dtOut = new DataOutputStream(out);
			String test = "ceci est un test";
			byte[] b = test.getBytes("8859_1");
			dtOut.write(b);
			byte[] returnBytes = out.toByteArray();
			//DataHandler dh = new DataHandler(new org.apache.turbine.util.mail.ByteArrayDataSource(returnBytes,"application/octet-stream"));

		
			//InputStream input = dtOut
			
			
			byte[] b2 = null;
			ByteArrayInputStream  bin =  new ByteArrayInputStream(returnBytes);
			DataInputStream dinput = new DataInputStream(bin);

//			dinput.re;
//	    	String s2 = new String (returnBytes,"8859_1");
//	    	System.out.println(s2);
	    	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    	
	}
}

