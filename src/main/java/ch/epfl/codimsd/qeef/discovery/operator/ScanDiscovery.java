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
package ch.epfl.codimsd.qeef.discovery.operator;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Access;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qep.OpNode;
import ch.epfl.codimsd.qeef.DataSourceManager;

/**
 * The ScanDiscovery operator reads tuples from a certain datasource. In the QEP, such operators
 * are of type "Scan". When defining this operator in the QEP, one should specify the dataSources
 * and the parameters of this dataSource.
 * 
 * @author Othman Tajmouati.
 */
public class ScanDiscovery extends Access {
	
	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ScanDiscovery.class.getName());
	
	/**
	 * Default constructor.
	 * 
	 * @param id identifier of the operator.
	 * @param opNode opNode structure of the operator.
	 */
	public ScanDiscovery(int id, OpNode opNode) {

		super(id);
		
		// Get the DataSource of this Scan operator.
		DataSourceManager dsManager = DataSourceManager.getDataSourceManager();
		dataSource = (DataSource) dsManager.getDataSource(opNode.getOpTimeStamp());
	}
	
	/**
	 * Open the operator.
	 */
	public void open() throws Exception{
	    
	    super.open();
	} 

	/**
	 * Get next results from the dataSource.
	 * 
	 * @param consumerId not used.
	 */
	public DataUnit getNext(int consumerId) throws Exception {
	    
		  instance = (dataSource).read();
		  return instance;
	}
	
	/**
	 * Close the operator.
	 */
	public void close() throws Exception{
	    
	    super.close();
	}
}

