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

import ch.epfl.qosdisc.database.Connection;
import ch.epfl.qosdisc.database.WSMLStore;
import ch.epfl.qosdisc.operators.PropertySet;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.QueryManager;
import ch.epfl.codimsd.qeef.QueryManagerImpl;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.query.Request;
import ch.epfl.codimsd.query.RequestParameter;
import ch.epfl.codimsd.query.RequestResult;

public class RunTest {

	 /**
     * Load the contents of the file pointed by the URL into a string.
     * 
     * @param url The URL to query.
     * @return String containing the file, or null if some error occured.
     */
    private static String loadStringFromURL(String url) {

    	try {
    		
			// Change iri as required.
			url = WSMLStore.fixIRI(url);
			
			// Read the WSML text from the file.
			BufferedReader r = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
			StringBuffer buffer = new StringBuffer();
			String line;
			while((line = r.readLine()) != null) {
				buffer.append(line);
				buffer.append("\n");
			}
			r.close();
			
			return buffer.toString();
    	} catch(Exception ex) {
    		
    		ex.printStackTrace();
    	}
    	return null;
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Load the properties.
		PropertySet.setup(".");
		
		Logger logger = Logger.getLogger(QueryManagerImpl.class.getName());
		
		try {
			
			// Get the QueryManagerImpl singleton.
			QueryManagerImpl queryManagerImpl = QueryManagerImpl.getQueryManagerImpl();
						
			// Specify your goal parameters here.
			int requestType = Constants.REQUEST_TYPE_SERVICE_DISCOVERY;
			RequestParameter requestParameter = new RequestParameter();
			requestParameter.setParameter(Constants.LOG_EXECUTION_PROFILE, "TRUE");
			requestParameter.setParameter(Constants.NO_DISTRIBUTION, "TRUE");
			
			// Load the goal WSML and store it in the properties
			requestParameter.setParameter("goalstring", loadStringFromURL(PropertySet.props.getProperty("goal")));
			
			// Fill requestParameter with initial properties.
			for (Map.Entry<Object, Object> p : PropertySet.props.entrySet())
				requestParameter.setParameter((String) p.getKey(), (String) p.getValue());
			
			Request request = new Request(null, requestType, requestParameter);
			@SuppressWarnings("unused")
			RequestResult result = null;

			// Open connection.
			Connection.open(PropertySet.props);

			// Execute a request.
			QueryManager queryManager = new QueryManager(request);
			RequestResult finalRequestResult = queryManager.getRequestResult();

			finalRequestResult.getResultSet().open();
			logger.debug("Found "+finalRequestResult.getResultSet().linkedList.size()+" items.");
			Tuple t = (Tuple) finalRequestResult.getResultSet().next();
			String s = t.getData(0).toString();
	    	String[] vals = s.split("\\\\");
	        // Create sorted output list.
	    	logger.info("Output list:");
	    	for(int i=0; i<vals.length; i+=3) {

	    		logger.debug(""+vals[i+2]+" "+vals[i+1]);	    		
	    	}

			// Close the QueryManagerImpl.
			queryManagerImpl.shutdown();
		
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}

