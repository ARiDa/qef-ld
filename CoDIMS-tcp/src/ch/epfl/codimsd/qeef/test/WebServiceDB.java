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

import java.io.FileReader;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.deri.wsmo4j.common.ClearTopEntity;
import org.wsmo.common.TopEntity;
import org.wsmo.factory.Factory;
import org.wsmo.service.WebService;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.Serializer;

import com.ontotext.wsmo4j.service.WebServiceImpl;

import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.connection.TransactionMonitor;
import ch.epfl.codimsd.qeef.SystemConfiguration;
import ch.epfl.codimsd.qeef.discovery.datasource.dataSourceWsmoDB.Query;

public class WebServiceDB {

	private TransactionMonitor tm;
	private int transactionID;
	private final int maxBufferSize = 254;
	
	public WebServiceDB(String IRI) throws Exception {
		
		SystemConfiguration.loadSystemConfiguration();
		CatalogManager.getCatalogManager();
		
		tm = TransactionMonitor.getTransactionMonitor();
		transactionID = tm.open(IRI);
	}
	
	public void createWebServiceDB() throws Exception {
	
		String createWSDBString = "create table WEBSERVICES (id integer primary key, iri char(150))";
		String createContentDBString = "create table CONTENT (id integer, ws_id integer, " +
				"content char(254)," +
				"constraint contentpk primary key(id,ws_id))";
		
		Query query = new Query();
		query.setStringRequest(createWSDBString);
		tm.execute(transactionID, query);
		query.setStringRequest(createContentDBString);
		tm.execute(transactionID, query);
	}
	
	public void fillDB(String filePath) throws Exception{
		
		FileReader fReader = new FileReader(filePath);
		Parser parser = Factory.createParser(null);
		TopEntity[] topEntities = parser.parse(fReader);
		ArrayList<TopEntity> newTopEntities = new ArrayList<TopEntity>();
		ArrayList<String> singleWSList = new ArrayList<String>();
		int numberOfWebServices = 0;
		
		for (int i = 0; i < topEntities.length; i++) {
			
			if (topEntities[i] instanceof WebServiceImpl) {
				
				newTopEntities.add(topEntities[i]);
				Serializer ser = Factory.createSerializer(null);
				StringBuffer str = new StringBuffer();
		        TopEntity[] tops = { topEntities[i] };
		        WebService ws = (WebService) topEntities[i];
		        Query query = new Query();
		        numberOfWebServices++;
				query.setStringRequest("INSERT INTO WEBSERVICES Values (" + numberOfWebServices + ", '"
						+ ws.getIdentifier() + "')");
				tm.execute(transactionID, query);
		        ser.serialize(tops, str);
		        System.out.println(str);
		        singleWSList.add(str.toString());
		        ClearTopEntity.clearTopEntity((WebServiceImpl)topEntities[i]);
			}
		}
		
		for (int i = 1; i <= singleWSList.size(); i++) {
			
			String[] webServicesPieces = cutWebServiceString(singleWSList.get(i-1));
			Query query = new Query();
			
			for (int j = 1; j <= webServicesPieces.length; j++) {
				query.setStringRequest("INSERT INTO Content Values ("+i+","+j+",'"+webServicesPieces[j-1]+"')");
				String test = "INSERT INTO Content Values ("+i+","+j+",'"+webServicesPieces[j-1]+"')";
				System.out.println(test);
				tm.execute(transactionID, query);
			}
		}
		
		Query query = new Query();
		query.setStringRequest("COMMIT");
		tm.execute(transactionID,query);
		tm.close(transactionID);
	}
	
	public ArrayList<WebService> readDB() throws Exception {
		
		Query query = new Query();
		query.setStringRequest("SELECT * FROM CONTENT");
		ResultSet rset = tm.executeQuery(transactionID, query);
		ArrayList<WebService> wsList = new ArrayList<WebService>();
		rset.next();
		boolean _continue = true;
		
		while (_continue) {
			
			int idWebServiceID = (Integer) rset.getInt(1);
			int oldWebServiceID = idWebServiceID;
			ArrayList<String> wsStringList = new ArrayList<String>();
			
			while (true) {
				
				oldWebServiceID = (Integer) rset.getInt(1);
				if (oldWebServiceID != idWebServiceID) {
					break;
				}
				
				wsStringList.add((String)rset.getObject(3));
				
				if (!rset.next()) {
					_continue = false;
					break;
				}
			}
			
			String wsString = pasteWebServiceStrings(wsStringList);
			
			StringBuffer str = new StringBuffer(wsString);
			Parser parser = Factory.createParser(null);
			TopEntity[] topEntities1 = parser.parse(str);
			WebService webService = (WebServiceImpl) topEntities1[0];
			wsList.add(webService);
		}
		
		
		return wsList;
	}
	
	private String pasteWebServiceStrings(ArrayList webServices) {
		
		String webService = "";
		
		for (int i = 0; i < webServices.size(); i++) {
			webService += webServices.get(i);
		}
		
		return webService;
	}
	
	private String[] cutWebServiceString(String webService) {
		
		float numberOfPieces = webService.length() / maxBufferSize;
		int newNumberOfPieces = ((int) numberOfPieces) + 1;
		String[] webServicesPieces = new String[newNumberOfPieces];
		
		for (int i = 0; i < newNumberOfPieces-1; i++) {
			webServicesPieces[i] = webService.substring(i*254, (i+1)*254);
		}
		webServicesPieces[newNumberOfPieces-1] = webService.substring((newNumberOfPieces-1)*254, webService.length());
		
		return webServicesPieces;
	}
	
	public static void main(String args[]) {
		
		try {
		
			WebServiceDB wsDB = new WebServiceDB("IRI_WEBSERVICES_DERBY");
			wsDB.createWebServiceDB();
			String webServicesFile = "C:/SubVersion/SubVersionProject/Workspace/Projects/" +
			"QoSDiscoveryComponent/src/codims-home/dataSources/webServices.wsml";
			wsDB.fillDB(webServicesFile);

		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
}

