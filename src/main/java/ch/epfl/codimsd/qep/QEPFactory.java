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
package ch.epfl.codimsd.qep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;

import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.initialization.QEPInitializationException;
import ch.epfl.codimsd.exceptions.initialization.RequestTypeNotFoundException;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.DataSourceManager;
import ch.epfl.codimsd.qeef.SystemConfiguration;
import ch.epfl.codimsd.qeef.util.Constants;

/**
 * The QEPFactory is responsible for building QEP objects. It transforms what it reads from
 * formated QEP to hashtable of opNode structures.
 * 
 * @author Othman Tajmouati.
 */
public class QEPFactory {

	/**
	 * Indicates if its the first time we call the factory.
	 */
	private static boolean firstCall = false;
	
	/**
	 * Log4j logger.
	 */
	private static Logger logger = Logger.getLogger(QEPFactory.class.getName());

	/**
	 * Retrieve the requested QEP path, according to its templateType and to the requestType.
	 * 
	 * @param requestType type of the request (@see ch.epfl.codims.qeef.util.Constants).
	 * @param templateType type of the template (see Catalog)
	 * @return the path of the query execution plan.
	 * @throws RequestTypeNotFoundException
	 * @throws CatalogException
	 */
	public static String getQEP(int requestType, int templateType) throws RequestTypeNotFoundException, CatalogException {
		
		String qepInitialFile = null;

		// Get the CatalogManager and construct the query
		CatalogManager catalogManager = CatalogManager.getCatalogManager();
		String query = "SELECT Template.path FROM Template, RequestTypeTemplate WHERE " +
				"RequestTypeTemplate.idRequest = " + requestType + " AND " +
				"RequestTypeTemplate.idTemplate =  Template.idTemplate AND " +
				"Template.templateType = " + templateType;
		try {
			// get the qep path.
			ResultSet rset = catalogManager.executeQueryString(query);
			if (rset.next()) {
			    qepInitialFile = SystemConfiguration.getSystemConfigInfo(Constants.HOME)+ rset.getString(1);
			} else {
			    logger.debug("Query: " + query);
			    throw new CatalogException("Could not find the path of the query execution plan for RequestType=" + requestType + " and TemplateType=" + templateType);
			}
		} catch (SQLException ex) {
			throw new CatalogException("SQLException while reading template from catalog : " + ex.getMessage());
		} catch (CatalogException ex) {
			throw new CatalogException("CatalogException while reading template from catalog : " + ex.getMessage());
		}
		return qepInitialFile;
	}
	
	/**
	 * Reads a qep file in xml or string format, and build the hashtable of opNode structures
	 * representing the operators.
	 * 
	 * If this method is called remotely, the parameter "type" should be set to Constants.qepAccessTypeRemote;
	 * in order to read from a string file.
	 * 
	 * @param qepFile path to QEP file.
	 * @param type type of access (remote mode or centralized mode).
	 * @param qep this qep. It is send in order to be filled with corresponding hashtable.
	 * @return hashtable containing opNode structures.
	 * @throws QEPInitializationException
	 */
	public static Hashtable<String, OpNode> loadQEP(String qepFile, String type, QEP qep) 
		throws QEPInitializationException {

		// Initializations.
		Hashtable<String, OpNode> operatorList = null;
		Document document = null;
		
		try {
	
			// Build the dom4j according to CoDIMS centralized or remote modes.
			if (type.equalsIgnoreCase(Constants.qepAccessTypeLocal)) {
		
				// Call Sax reader in order to parse the xml document.
				SAXReader reader = new SAXReader();
				document = reader.read(qepFile);
				qep.setDocument(document);

			} else if (type.equalsIgnoreCase(Constants.qepAccessTypeRemote)) {
	
				// Parse the xml file in string format using dom4j API.
				document = DocumentHelper.parseText(qepFile);
				qep.setDocument(document);
			}
			
			// Build the hashtable of opNodes.
			operatorList = buildOpNodeTable(document);

		} catch (DocumentException ex) {
			ex.printStackTrace();
			throw new QEPInitializationException("DocumentException error while loadind the XML QEP : " + ex.getMessage());
		}
		
		return operatorList;
	}
	
	/**
	 * Retrieve the IRI from which the operator of type "Scan" reads from. This method is called
	 * in order to compute the initial number of tuples in the DiscoveryOptimizer.
	 * 
	 * @param qep the query execution plan.
	 * @return IRI to read from.
	 */
	public static String getScanSource(QEP qep) {
		
		Hashtable opNodeHashtable = qep.getOperatorList();
		String IRI = "";

		for (int i = 1; i <= qep.getOperatorList().size(); i++ ) {
			
			OpNode opNode = (OpNode) opNodeHashtable.get(i+"");
			if (opNode.getType().equalsIgnoreCase("Scan")) {
				IRI = opNode.getParams()[0];
			}
		}

		return IRI;
	}
	
	public static void logFile(String fileName, String fileContent) {
		
		try {
			
			String qepDir = SystemConfiguration.getSystemConfigInfo(Constants.HOME) +  
			File.separator + "logs" + File.separator + "qeps";
			
			if (firstCall == false) {
			
				File qep = new File(qepDir);
				deleteDir(qep);	
				boolean success = (new File(qepDir)).mkdir();
				
				if (!success)
					logger.debug("Cannot create qeps directory (containing generated qeps).");
				
				firstCall = true;
			}
			
			String filePath = qepDir + File.separator + fileName;
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
	        out.write(fileContent);
	        out.close();
	    
		} catch (IOException ex) {
	    	ex.printStackTrace();
	    }
	}
	
	/**
	 * Delete directories and subdirectories.
	 * 
	 * @param dir - the file to delete
	 * @return true if the database is deleted, false otherwise
	 */
	private static boolean deleteDir(File dir) {
        
		if (dir.isDirectory()) {
                    String[] children = dir.list();
                    for (int i=0; i<children.length; i++) {
                        boolean success = deleteDir(new File(dir, children[i]));
                        if (!success) {
                            return false;
                         }
                    }
                }

                return dir.delete();
        }
	
	/**
	 * Build a hashtable containing operator opNode structures, according to what is defined
	 * in the query execution plan.
	 * 
	 * @param document dom4j document of the QEP.
	 * @return the opnode hashtable.
	 * 
	 * @throws QEPInitializationException
	 */
	private static Hashtable<String, OpNode> buildOpNodeTable(Document document) 
			throws QEPInitializationException {
		
		Hashtable<String, OpNode> operatorList = new Hashtable<String, OpNode>();

                /*List listMod = document.selectNodes( "//qep:Module" );
                Element modElement = (Element)listMod;
                String[] stringType = modElement.attributeValue("type").split(",");
                int numberOfIterations = Integer.parseInt(modElement.attributeValue("numberOfIterations"));
*/
		List list = document.selectNodes( "//op:Operator" );
		Iterator itt = list.iterator();

		while (itt.hasNext()) {

			Element opElement = (Element)itt.next();

			// Get operator ID from xml QEP
			int opID = Integer.parseInt(opElement.attributeValue("id"));

			// Build producers from xml QEP
			String[] stringProducers = opElement.attributeValue("prod").split(",");
			int[] producers =  new int[stringProducers.length];

			for (int i = 0; i < stringProducers.length; i++)
				producers[i] = Integer.parseInt(stringProducers[i]);

			// Get operator type
			String type = opElement.attributeValue("type");

			// Get parallelizable information
			String parallelAtt = opElement.attributeValue("parallelizable");
			boolean parallelizable = false;
			if (parallelAtt != null) {
				if (parallelAtt.equalsIgnoreCase("true"))
					parallelizable = true;
			}

			// Get operator name from xml QEP
			Iterator opItt = opElement.elementIterator();
			Node xmlOpNode = (Node) opItt.next();
			String opName = xmlOpNode.getText();

			// Build operator parameters
			List<String> paramsList = new ArrayList<String>();

			if (opItt.hasNext()) {
		
				Element parameterList = (Element) opItt.next();
				Iterator paramItt = parameterList.elementIterator();

				while (paramItt.hasNext()) {
					Node parameter = (Node) paramItt.next();
					paramsList.add(parameter.getText());
				}

			}

			// Build operator timeStamp
			long idTimeStamp = System.currentTimeMillis();
			
			// Build OpNode object for this operator
			
			String[] params = paramsList.toArray(new String[paramsList.size()]);
			OpNode opNode = new OpNode(opID , opName, producers, params, idTimeStamp+"", type, parallelizable);

			// Build DataSources if necessary
			if (opNode.getType() != null) {
				
				if (opNode.getType().equalsIgnoreCase("Scan")) {
					
					// Write the number of tuples in the BlackBoard as specified in the QEP
					// This operation is not done in remote nodes as G2N has already been called
					String numberOfTuples = opElement.attributeValue(Constants.QEP_SCAN_NUMBER_TUPLES);
					
					// In remote QEP this parameter is null, so we dont write again the field numberOfTuples
					// used in the DiscoveryOptimizer when it calls G2N
					if (numberOfTuples != null) {
						
						if (!numberOfTuples.equalsIgnoreCase("?")) {
							
							BlackBoard bl = BlackBoard.getBlackBoard();
							bl.put(Constants.QEP_SCAN_NUMBER_TUPLES, numberOfTuples);
						}
					}

					try {
						DataSourceManager dsManager = DataSourceManager.getDataSourceManager();
						dsManager.createDataSources(opNode);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						throw new QEPInitializationException("Error creating the datasource : " + ex.getMessage());
					}
				}
			}

			// Put OpNode in the QEP
			operatorList.put(opID+"", opNode);
		}
		
		return operatorList;
	}
	
	/**
	 * Add an operator template to the QEP Document.
	 * 
	 * @param document dom4j document.
	 * @param opNode opNode structure of the operator to build.
	 * @param opID identifier of the operator.
	 */
	private static void createOperator(Document document, OpNode opNode, int opID) {
		
		Element root = document.getRootElement();
		Iterator ittRoot = root.elementIterator();
		Element qepElement = (Element)ittRoot.next();

		// create new operator xml template (attributes, name)
		Element newOp = qepElement.addElement("op:Operator")
                .addAttribute( "id", opID + "" )
                .addAttribute( "prod", buildProducers(opNode) );
		
		if (opNode.getType() != null)
			newOp.addAttribute("type", opNode.getType());
		
		Element newNameOp = newOp.addElement("Name");
		newNameOp.addText(opNode.getOpName());
		
		// create operator xml parameters
		if (opNode.getParams() != null) {
			if (opNode.getParams().length != 0) {
				Element newParameterList = newOp.addElement("ParameterList");
				for (int i = 0; i < opNode.getParams().length; i++) {
					Element newParam = newParameterList.addElement("Param");
					newParam.addText(opNode.getParams()[i]);
				}
			}
		}
	}
	
	/**
	 * Add producers to the operator in its "producer" attribute (see xml QEP).
	 * 
	 * @param opNode opNode structure.
	 * @return the string written to the QEP.
	 */
	private static String buildProducers(OpNode opNode) {
		
		// Get producers from the opNode structure.
		int[] producerIDs = opNode.getProducerIDs();
		String stringProducers = "";
		
		for (int i = 0; i< producerIDs.length; i++) {
			stringProducers += producerIDs[i] + ",";
		}
		
		stringProducers = stringProducers.substring(0,stringProducers.length()-1);
		
		return stringProducers;
	}
	
	/**
	 * Creates the xml string corresponding to a QEP. This string wiil be sent
	 * to remote node in order to create the remote operators.
	 * 
	 * @param qep the query execution plan.
	 * @param type type of the Document.
	 * @return the xml string.
	 */
	public static String generateString(QEP qep, String type) {
		
		Document document = createTemplate(type);
		
		for (int i = 1; i <= qep.getOperatorList().size(); i++ ) {
			OpNode opNode = (OpNode) qep.getOperatorList().get(""+i);
			createOperator(document, opNode, i);
		}
		
		return document.asXML();
	}
	
	/**
	 * Creates the xml structure where we embed the operators.
	 * 
	 * @param type type of the xml template.
	 * @return dom4j Document.
	 */
	private static Document createTemplate(String type) {

		// Create empty dom4j document.
		Document document = DocumentHelper.createDocument();
		
		// Add "QEPTemlate", "op", "qep",  tags.
		Element root = document.addElement( "QEPTemplate", "http://giga03.lncc.br/DIP/WP4/CoDIMS-D" );
		root.addNamespace("op", "http://giga03.lncc.br/DIP/WP4/CoDIMS-D/Operator");
		root.addNamespace("qep", "http://giga03.lncc.br/DIP/WP4/CoDIMS-D/QEP");
		root.addElement("qep:QEP").addAttribute( "type", type );

		return document;
	}
}

