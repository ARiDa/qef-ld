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

import java.net.InetAddress;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSetMetaData;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.dataSource.DataSourceException;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.types.OracleType;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qeef.relational.Column;

/**
 * Version 1
 * 
 * RelationalDataSource reads the datasources from a given Database. 
 * It executes an sql query on the database and returns tuple objects.
 * 
 * @author Othman Tajmouati
 * 
 * @date May 24, 2006
 */
public class RelationalDataSource extends DataSource {

	/**
	 * The url of the database to read from.
	 */
	private String url;
	
	/**
	 * The user of this database.
	 */
	private String user;
	
	/**
	 * Password associated to this user.
	 */
	private String pwd;
	
	/**
	 * DriverName required to access to the database.
	 */
	private String driverName;
	
	/**
	 * Sql connection to the database.
	 */
	private java.sql.Connection con;
	
	/**
	 * The query executed on the database to retreive the results.
	 */
	private String sqlStringQuery;
	
	/**
	 * Sql ResultSet object storing the tuples.
	 */
	private ResultSet rset = null;
	
	/**
	 * Sql Statement object.
	 */
	private Statement stmt = null;
	
	/**
	 * Sql ResultSet Metadata object.
	 */
	private ResultSetMetaData rsetMetadata;
	
	/**
	 * Number of columns in each row.
	 */
	private int numberOfColumns = 0;
	
	/**
	 * Log4j Logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RelationalDataSource.class.getName());
	
	/**
	 * Default constructor
	 * 
	 * @param alias the name of this DataSource, currently set to "RelationalDataSource
	 * @param metadata metadata associated with this dataSource. Metadata defines the format of sql types within
	 * each column of a jdbc ResultSet
	 */
	public RelationalDataSource(String alias, TupleMetadata metadata, String IRI, String sqlQuery) 
			throws CatalogException {
		
		// Call super constructor.
		super(alias, null);

		// Sql query used to query the database.
		this.sqlStringQuery = sqlQuery;

		// Get initial parameters needed to access the database from the Catalog.
		CatalogManager catalogManager = CatalogManager.getCatalogManager();
		this.url = (String) catalogManager.getSingleObject("ds_database", "host", "iri='"+IRI+"'");
		this.user = (String) catalogManager.getSingleObject("ds_database", "username", "iri='"+IRI+"'");
		this.pwd = (String) catalogManager.getSingleObject("ds_database", "passwrd", "iri='"+IRI+"'");
		this.driverName = (String) catalogManager.getSingleObject("ds_database", "driverName", "iri='"+IRI+"'");
	}

	/**
	 * Open a connection to a DataBase given an url, a username and a password; execute a query
	 * on the database, get the jdbc ResultSet and create the correponding metadatas. Each column of the metadata
	 * has an name, an OracleType type and a SQL Type (java.sql.Types).
	 */
	public void open() throws DataSourceException {

		try {
	
			// Load the driver class.
			Class.forName(driverName);
			
			// Build the local host IP adress and replace the substring "localhost" by
			// the real IP address of the machine.
			int indexOfLocalhost = url.indexOf("localhost");
			if (indexOfLocalhost != -1) {
				String localIP = InetAddress.getLocalHost().getHostAddress();
				url = url.substring(0, indexOfLocalhost) + 
					localIP + url.substring(indexOfLocalhost+"localhost".length(), url.length());
			}
			
			// If the drivername is a derby client one, set the connection properties.
			if (driverName.equalsIgnoreCase("org.apache.derby.jdbc.ClientDriver")) {
				Properties connProps = new Properties();
		        connProps.put("user", user);
		        connProps.put("password", pwd);
				con = DriverManager.getConnection(url, connProps);
			} else {
				con = DriverManager.getConnection(url, user, pwd);
			}

			// Create the statement and execute the query.
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
					ResultSet.CONCUR_UPDATABLE);
			
			rset = stmt.executeQuery (sqlStringQuery);
			
			// Construct the metadata of the ResultSet in a TupleMetadata object.
			metadata = new TupleMetadata();
			rsetMetadata = rset.getMetaData();
			numberOfColumns = rsetMetadata.getColumnCount();
			
			for (int i = 1; i <= numberOfColumns; i++) {
				int type = rsetMetadata.getColumnType(i);
				Column column = new Column(rsetMetadata.getColumnName(i), 
						Config.getDataType(Constants.ORACLETYPE), 1, i, false, type);
				metadata.addData(column);
			}
			
			setMetadata(metadata);
			
			// Put Metadata of the dataSource in the BlackBoard.
			BlackBoard bl = BlackBoard.getBlackBoard();
			bl.put("Metadata", metadata);
			
		} catch (SQLException ex){
			throw new DataSourceException("Could not open the RelationalDataSource : " + ex.getMessage());
		} catch (ClassNotFoundException ex) {
			throw new DataSourceException("Could not find the driver to open the dataSource : " + ex.getMessage());
		} catch (Exception ex) {
			throw new DataSourceException("Exception while opening the DataSource : " + ex.getMessage());
		}
	}
	
	/**
	 * Read a specific row from a jdbc ResultSet object and return a tuple containing the row.
	 * 
	 * @return a DataUnit containing OracleType.
	 */
	public DataUnit read() throws DataSourceException {

		// Construct a new tuple where we insert our sql object.
		Tuple tuple = new Tuple();

		try {
			// If there still exist tuples to return then do so.
			if (rset.next() == true) {
			
				// Construct the OracleType object before sending to Scan operator.
				for (int i = 1; i <= numberOfColumns; i++) {
					OracleType oracleObject = new OracleType();
					oracleObject.setValue(rset.getObject(i));
					tuple.addData((Type)oracleObject);
				}

				return tuple;
				
			} else
				return null;
			
		} catch (SQLException ex1) {
			throw new DataSourceException("Could not read the dataSource : " + ex1.getMessage());
		}
	}
	
	/**
	 * Close the DataSource
	 */
	public void close() throws DataSourceException {
		
		try {
			
			con.close();
			
		} catch (SQLException ex1){
			throw new DataSourceException("Could not close the dataSource : " + ex1.getMessage());
		}
	}

	/**
	 * @return the sql query.
	 */
	public String getSqlStringQuery() {
		return sqlStringQuery;
	}

	/**
	 * Set the sql query.
	 * @param sqlStringQuery sql query executed on the database.
	 */
	public void setSqlStringQuery(String sqlStringQuery) {
		this.sqlStringQuery = sqlStringQuery;
	}
}

