/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.config.AppConfig;

public class CodimsEnv {

	private Properties props;
	private File codimsEnv;
    private static Logger logger = Logger.getLogger(CodimsEnv.class.getName());

	public CodimsEnv() throws Exception {
		codimsEnv = null;

		System.out.println(AppConfig.CODIMS_ENV_FILE);
		codimsEnv = new File(AppConfig.CODIMS_ENV_FILE);

		if (codimsEnv == null)
			throw new Exception("ERROR : codims.env not found.");

		props = new Properties();
		props.load(new FileInputStream(codimsEnv));
	}

	public void updateProperties() throws Exception {
		updateCodims();
	}

	private void updateCodims() throws Exception {

		File codimsTemplateProps = new File(AppConfig.CODIMS_TEMPLATE_PROPS_FILE);
		File codimsProps = new File(AppConfig.CODIMS_PROPS_FILE);

		if (!codimsTemplateProps.exists())
			throw new Exception("ERROR : codimsTemplate.properties not found in Scripts directory.");

		Properties oldProperties = new Properties();
		oldProperties.load(new FileInputStream(codimsTemplateProps));
		Properties newProperties = new Properties();

		codimsProps.delete();

		for (Map.Entry<Object, Object> p : oldProperties.entrySet()) {

			String thisKey = (String) p.getKey();
			if (props.containsKey(thisKey)) {
				newProperties.setProperty(thisKey,props.getProperty(thisKey));
			} else {
				newProperties.setProperty(thisKey,oldProperties.getProperty(thisKey));
			}
		}

		String localWebService = "http://" + newProperties.getProperty("LOCAL_WEB_SERVICE") +
			"/wsrf/services/examples/core/first/DQEEService";
		newProperties.setProperty("LOCAL_WEB_SERVICE", localWebService);

		//String derbyCatalogFile = newProperties.getProperty("IRI_CATALOG_DERBY");
		//String derbyCatalogFile = "org.apache.derby.jdbc.ClientDriver;jdbc:derby://127.0.0.1:1527/" + rootPath + "codims-home/DerbyCatalog;create=false;CODIMS;CODIMS";
        //System.out.println("derbyCatalogFile: " + derbyCatalogFile);
		//newProperties.setProperty("IRI_CATALOG_DERBY",derbyCatalogFile);
        newProperties.setProperty("IRI_CATALOG_DERBY", AppConfig.CATALOG_URL);

		BufferedWriter out = new BufferedWriter(new FileWriter(AppConfig.CODIMS_PROPS_FILE));
		out.write("# Codims property file");
		out.newLine();
		for (Map.Entry<Object, Object> p : newProperties.entrySet()) {
        	        out.write(p.getKey() + "=" + p.getValue());
                        out.newLine();
		}
		out.close();
	}

	public void updateCatagalog() throws Exception {

		Connection con = null;
	    try {
            DerbyServerStarter.start();
            Class.forName(AppConfig.CATALOG_DB_CLIENT_DRIVER);
            logger.info("Getting connection to: " + AppConfig.CATALOG_URL);
            con = DriverManager.getConnection(AppConfig.CATALOG_URL, AppConfig.CATALOG_USER, AppConfig.CATALOG_PASSWORD);
			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			con.setAutoCommit(false);

			String nodesString = props.getProperty("NODES");
			String env = props.getProperty("ENVIRONMENT_ID");

			String[] nodes = nodesString.split(";");


			String nodeFlag = "";
			int nodeID = 0;

			String selectEnv = "SELECT * FROM environment WHERE environment.idenvironment = " + env;
			ResultSet rset = stmt.executeQuery(selectEnv);
			if (rset.next()) {
                            
				stmt.execute("DELETE FROM AppNodeRate WHERE idenvironment = " + env);
				stmt.execute("DELETE FROM node WHERE idenvironment = " + env);
				stmt.execute("DELETE FROM environment WHERE idenvironment = " + env);
			}

			stmt.execute("INSERT INTO environment Values (" + env + ", '" + nodes.length + " nodes')");
			for (String node : nodes) {

                                System.out.println("node  = " + node + " node.substring(0,6) = " + node.substring(0,6));
				if (node.substring(0,9).equalsIgnoreCase("localhost")) {
					String localIP = InetAddress.getLocalHost().getHostAddress();
					node = localIP + node.substring(9,node.length());
				}

				nodeID++;
				nodeFlag = node;
                int rate = 0;

                rate = 500;

                /*
                 * ATTENTION ! NODE RATE HAS BEEN DEFINED AS CONSTANT
                 *
                 * */

				String insertAppNodeRateTCP = "INSERT INTO AppNodeRate values (" + env + ", " + nodeID + ", 31, " + rate + ")";
                String insertAppNodeRateEDDY = "INSERT INTO AppNodeRate values (" + env + ", " + nodeID + ", 34, " + rate + ")";
                String insertAppNodeRatePROJECT = "INSERT INTO AppNodeRate values (" + env + ", " + nodeID + ", 17, " + rate + ")";
                String insertAppNodeRateTHJ = "INSERT INTO AppNodeRate values (" + env + ", " + nodeID + ", 32, " + rate + ")";
                String insertAppNodeRateUNFOLD = "INSERT INTO AppNodeRate values (" + env + ", " + nodeID + ", 37, " + rate + ")";
                String insertAppNodeRateFOLD = "INSERT INTO AppNodeRate values (" + env + ", " + nodeID + ", 36, " + rate + ")";
                String insertAppNodeRateSHJ = "INSERT INTO AppNodeRate values (" + env + ", " + nodeID + ", 33, " + rate + ")";
                String insertAppNodeRateSCAN = "INSERT INTO AppNodeRate values (" + env + ", " + nodeID + ", 35, " + rate + ")";
                String insertAppNodeRateSMOOTH = "INSERT INTO AppNodeRate values (" + env + ", " + nodeID + ", 38, " + rate + ")";
                String insertAppNodeRateVR = "INSERT INTO AppNodeRate values (" + env + ", " + nodeID + ", 43, " + rate + ")";

				String insertNode = "INSERT INTO node Values " + "(" + env + ", " + nodeID + ", '" + nodeFlag + "', 'http://" + nodeFlag + "/wsrf/services/examples/core/first/DQEEService', 220)";

				stmt.execute(insertNode);
				stmt.execute(insertAppNodeRateTCP);
                stmt.execute(insertAppNodeRateEDDY);
                stmt.execute(insertAppNodeRatePROJECT);
                stmt.execute(insertAppNodeRateTHJ);
                stmt.execute(insertAppNodeRateUNFOLD);
                stmt.execute(insertAppNodeRateFOLD);
                stmt.execute(insertAppNodeRateSHJ);
                stmt.execute(insertAppNodeRateSCAN);
                stmt.execute(insertAppNodeRateSMOOTH);
                stmt.execute(insertAppNodeRateVR);
			}
			con.commit();

		} catch (SQLException ex) {
		    con.rollback();
			ex.printStackTrace();
		} finally {
		    con.close();
		}
		
	}

	public static void main(String[] args) {
		try {
			System.out.println("Initializing CoDIMS ...");
			CodimsEnv codimsEnv = new CodimsEnv();
            codimsEnv.updateCatagalog();
			System.out.println("Catalog updated with new environment.");
			codimsEnv.updateProperties();
			System.out.println("Property files updated.");
			System.out.println("CoDIMS ready!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
