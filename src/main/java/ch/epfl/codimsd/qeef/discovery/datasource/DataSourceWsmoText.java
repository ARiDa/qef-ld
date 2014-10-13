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
package ch.epfl.codimsd.qeef.discovery.datasource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;
import org.wsmo.common.TopEntity;
import org.wsmo.common.exception.InvalidModelException;
import org.wsmo.factory.Factory;
import org.wsmo.wsml.Parser;
import org.wsmo.wsml.ParserException;

import com.ontotext.wsmo4j.service.InterfaceImpl;
import com.ontotext.wsmo4j.service.WebServiceImpl;

import ch.epfl.codimsd.exceptions.dataSource.DataSourceException;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.exceptions.dataSource.UnsupportedDataSourceTypeException;

import ch.epfl.codimsd.qeef.relational.Column;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.types.WebServiceExt;
import ch.epfl.codimsd.qeef.util.Constants;

/**
 * The DataSourceWsmoText reads from a wsml file and returns all WebServiceExt objects from it.
 * Each WebServiceExt object, contains a Wsmo WebService and Wsmo Interface.
 * 
 * @author Othman Tajmouati.
 */
public class DataSourceWsmoText extends DataSource {

	/**
	 * Wsml file containining WebService definitions. The file should be wsml compliant.
	 */
	private File wsmoFile;
	
	/**
	 * Cursor indicating the current WebServiceExt in the list of WebServiceExt objects.
	 */
	private int cursor;
	
	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DataSourceWsmoText.class.getName());
	
	/**
	 * Number of Web Services in the wsml file.
	 */
	protected int numberOfInterfacesInFile;
	
	/**
	 * List containing our WebServiceExt objects.
	 */
	protected LinkedList<WebServiceExt> webServices;
	
	/**
	 * Default constrcutor. It calls the superconstructor and gets the wsmo file to read from.
	 * 
	 * @param alias name of the dataSource.
	 * @param metadata metadata of this dataSource.
	 * @param wsmoFileName the wsml file to read from.
	 */
	public DataSourceWsmoText(String alias, TupleMetadata metadata, String wsmoFileName) {
		
		// Call super constructor.
		super(alias, null);
		
		// Get the wsml file.
		BlackBoard bl = BlackBoard.getBlackBoard();
		wsmoFileName = bl.get(Constants.HOME) + File.separator + "dataSources"	+ File.separator + wsmoFileName;
		this.wsmoFile = new File(wsmoFileName);
	}
	
	/**
	 * Open the DataSource.
	 */
	public void open() throws DataSourceException {
		
		try {
			
			// Initializations.
			FileReader fReader = new FileReader(wsmoFile);
			webServices = new LinkedList<WebServiceExt>();
			numberOfInterfacesInFile = 0;
			
			// Parse the wsml file.
			Parser parser = Factory.createParser(null);
			TopEntity[] topEntities = parser.parse(fReader);
			
			// Construct the WebServiceExt objects and put them in a list.
			for (int i = 0; i < topEntities.length; i++) {
				
				if (topEntities[i] instanceof WebServiceImpl) {
					
					WebServiceImpl wsmoWebService = (WebServiceImpl) topEntities[i];
					Set interfaces = wsmoWebService.listInterfaces();
					Iterator itInterfaces = interfaces.iterator();
					
					while(itInterfaces.hasNext()) {
						
						WebServiceExt webServiceExt = new WebServiceExt();
						webServiceExt.setWebService(wsmoWebService);
						webServiceExt.setInterface((InterfaceImpl) itInterfaces.next());
						webServices.add(webServiceExt);
					}
				}
			}

			numberOfInterfacesInFile = webServices.size();
			parser = null;
			cursor = 0;
			
			// Set the metadatas.
			metadata = new TupleMetadata();
			Column column = new Column("WebServiceExt", Config.getDataType(Constants.WEBSERVICE_EXT), 1, 0, false);
			metadata.addData(column);
			setMetadata(metadata);
			
			// Put the metadata in the BlackBoard.
			BlackBoard bl = BlackBoard.getBlackBoard();
			bl.put("Metadata", metadata);
			
		} catch (ParserException ex) {
			throw new DataSourceException("ParserException in DataSourceWsmoText : " + ex.getMessage());
		} catch (InvalidModelException ex) {
			throw new DataSourceException("InvalidModelException in DataSourceWsmoText : " + ex.getMessage());
		} catch (UnsupportedDataSourceTypeException ex) {
			throw new DataSourceException("UnsupportedDataSourceTypeException in DataSourceWsmoText : " + ex.getMessage());
		} catch (FileNotFoundException ex) {
			throw new DataSourceException("FileNotFoundException in DataSourceWsmoText : " + ex.getMessage());
		} catch (IOException ex) {
			throw new DataSourceException("IOException in DataSourceWsmoText : " + ex.getMessage());
		} catch (Exception ex) {
			throw new DataSourceException("Exception in DataSourceWsmoText : " + ex.getMessage());
		}
	}
	
	/**
	 * Reads tuple by tuple.
	 * 
	 * @return the current WebServiceExt.
	 */
	public DataUnit read() throws DataSourceException {
       
		// If there are no more interfaces then return null tuple.
		if (numberOfInterfacesInFile == 0) {
        	return null;
		}

		// Construct a new tuple.
		Tuple tuple = new Tuple();
        tuple.addData((Type)webServices.get(cursor));

        numberOfInterfacesInFile --;
        cursor ++;
        
        return tuple;
	}
	
	/**
	 * Close the DataSource.
	 */
	public void close() throws IOException {
		cursor = 0;
		logger = null;
	}
}

