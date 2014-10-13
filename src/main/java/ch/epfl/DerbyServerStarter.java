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
package ch.epfl;

import java.io.PrintWriter;
import java.net.InetAddress;

import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.codimsd.config.AppConfig;


/**
 * Starts the Derby server once.
 * 
 * @author Othman Tajmouati.
 *
 */
public class DerbyServerStarter {

    final static Logger logger = LoggerFactory.getLogger(DerbyServerStarter.class);

	
	/**
	 * Boolean flag used to check if the server is started.
	 */
	private static boolean isServerStarted = false;
	
	/**
	 * Start the derby network server.
	 * 
	 * @throws Exception
	 */
	public static void start() throws Exception {
		try {
			// If server started return.
			if (isServerStarted == true)
				return;
			
			// Start derby.
			InetAddress addr = InetAddress.getByName(AppConfig.CATALOG_SERVER_HOST);
			logger.info("Catalog Server: {}:{}", addr, AppConfig.CATALOG_SERVER_PORT);
			
	        NetworkServerControl server = new NetworkServerControl(addr, AppConfig.CATALOG_SERVER_PORT);
			server.start(new PrintWriter(System.out));
			
			logger.info("Derby server started.");
			isServerStarted = true;
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}

