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
package ch.epfl.codimsd.query;

import java.util.Hashtable;

import ch.epfl.codimsd.qeef.util.Constants;

/**
 * The IRIParser parses the IRI sent to the CacheManager in order to build an sql query. Those IRIs could
 * contains conditions written in a certain way, and this component allows to modify the condition
 * or the query format without changing all the implementation and the way it reads the request.
 * 
 * @author Othman Tajmouati
 */
public class IRIParser {
	
	/**
	 * The IRI.
	 */
	private String IRI;
	
	/**
	 * Modified IRI.
	 */
	private String internalIRI;
	
	/**
	 * The type of the query.
	 */
	private int queryType;
	
	/**
	 * Here we store the IRIs and their corresponding request.
	 */
	private Hashtable<String, String> queryParametersHashtable;

	/**
	 * Constructor.
	 * 
	 * @param IRI
	 */
	public IRIParser(String IRI) {
		
		// Initialializations.
		this.IRI = IRI;
		queryParametersHashtable = new Hashtable<String, String>();
		
		// Fill the hashtable with right mapping (IRI to request).
		fillParameterHashtable();
	}
	
	/**
	 * XXX TODO Add javadoc and change method name.
	 */
	private void fillParameterHashtable() {
		
		internalIRI = getInternalIRI();
		queryParametersHashtable.put(Constants.IRI, internalIRI);
		
		String queryBag = getQueryBag();
		String[] fullParams = queryBag.split(Constants.parametersSeparator);
		String[] lightParamsName = new String[fullParams.length];
		String[] lightParamsContent = new String[fullParams.length];
		
		for (int i=0; i< fullParams.length; i++) {
			
			lightParamsName[i] = fullParams[i].
				substring(0, fullParams[i].indexOf(Constants.parameterContentDefChar));
			
			lightParamsContent[i] = fullParams[i].
				substring(fullParams[i].indexOf(Constants.parameterContentDelimiterChar)+1, 
						fullParams[i].lastIndexOf(Constants.parameterContentDelimiterChar));
			
			queryParametersHashtable.put(lightParamsName[i], lightParamsContent[i]);			
		}
		
		if (lightParamsName[0].equalsIgnoreCase(Constants.sqlQueryTag))
			queryType = Constants.IS_SQL_QUERY;
		else
			queryType = Constants.IS_XMLDATASOURCE_QUERY;
	}
	
	/** 
	 * @return the query type.
	 */
	public int getQueryType() { 
		return queryType; 
	}
	
	/**
	 * @param paramName name of the parameter.
	 * @return value of this key.
	 */
	public String getParameter(String paramName) { 
		return queryParametersHashtable.get(paramName);
	}
	
	/**
	 * @return what is specified between the query open tag and the query close tag.
	 */
	private String getQueryBag() { 
		return IRI.substring(IRI.indexOf(Constants.queryOpenTag)+Constants.queryOpenTag.length(), IRI.indexOf(Constants.queryCloseTag));
	}
	
	/**
	 * @return the internal IRI. Example complete IRI :
	 * IRI_WEBSERVICE?[SELECT * FROM MyTABLE] -> internal iri = IRI_WEBSERVICE
	 */
	public String getInternalIRI() {
		return IRI.substring(0, IRI.indexOf(Constants.queryOpenTag));
	}
}

