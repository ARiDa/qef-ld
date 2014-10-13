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
package ch.epfl.codimsd.connection;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.qeef.util.Constants;

import ch.epfl.codimsd.exceptions.transactionMonitor.TransactionMonitorException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

/***********************
 * This Class exports metadata concerning databases in CoDIMS system.
 * You can obtain metadata from the system catalog or from any other CODIMS' 
 * registered database.
 * At present, Metadata includes:
 *  - A list of registered databases (names);
 *  - A list of TableMetadata objects from each database;
 * @author fporto
 * @serialData 03/08/2006
 * @exception CatalogException;
 */
public class DatabaseMetadata {
		
	private CatalogManager cm=null;
	
	public DatabaseMetadata() throws CatalogException {
		
	try{
		cm=CatalogManager.getCatalogManager();
	}
	catch (CatalogException ce){
		throw new CatalogException("Error getting CatalogManager:"+ ce.getMessage());
	}
	}
	/***************************
	 * this method returns a list of databases registered in the catalog
	 * @return name of databases
	 * @throws CatalogException
	 */
	public ArrayList getDatabases() throws CatalogException {
		ArrayList<String> al=null;
		try{
			ResultSet rs= cm.getObject(Constants.IRI_DATABASECATALOG);
			String databaseName;
			al=new ArrayList<String>();
				
			for(int i=0; i < rs.getFetchSize() ;i++){
				databaseName = rs.getString(Constants.IRI);
				al.add(databaseName);
			}
		}
		catch (SQLException sqle){
			throw new CatalogException ("Error during access to the Catalog Database"+ sqle.getMessage());
		}
		catch (CatalogException ce){
			throw new CatalogException ("Error during access to the Catalog Database"+ ce.getMessage());
		}
		return al;
	}
	/**************************
	 *  This method returns a list of TableMetaData objects. Each object contains a table name and
	 *  and a list of Column metadata objects.
	 *  Tables can be obtained from the catalog (through Catalog Manager) or from the Transaction Monitor.
	 *  Once the good Database metadata has been obtained, we can build the list of table objects.
	 *  The ListTableMetaData stores a list of TableMetaData objects.
	 *  To build TableMetadaData objects we use the method getTables from the sql.DatabaseMetaData class.
	 *  This method retrieves table definitions based on a criteria comprising:catalog name,schema name, table name pattern (
	 *  a string with special caracters like (% or _) for generic queries) and types, that identify the type of tables. In this
	 *  application we require tables of type "TABLE".
	 *  Next, we look for the table primary key columns, using getPrimaryKey, and store in a hashtable to be used as a lookup.
	 *  Finaly, we fetch the corresponding columns and build the ColumnMetaData. The list of objects of this type are 
	 *  stored into the TableMetaData object.
	 *  
	 * @param IRI
	 * @return
	 * @throws Exception
	 */
	public ArrayList getTables(String IRI) throws Exception{
		ResultSet rsTable=null;
		ResultSet rsColumn=null;
		DatabaseMetaData dbMetaData=null;
		ArrayList<TableMetaData> listTableMetaData = new ArrayList<TableMetaData>();
		TableMetaData tmetaData=null;
		Hashtable <String,Object> pk;
		
		try {
			if (IRI.equals(Constants.IRICatalog)) {
				dbMetaData=cm.getCatalogMetadata();
			}
			else {
				TransactionMonitor tm=TransactionMonitor.getTransactionMonitor();
				dbMetaData=tm.getDatabaseMetadata(IRI);
			}
			if (dbMetaData== null){
				throw new Exception("Could not get Table MetaData");
			}
			else {
				String[] table= {"TABLE"};
				String tableName=null,columnName=null;
				int columnSize,columnIsNull,columnType;
				boolean isPrimaryKey=false;
				ColumnMetaData colMetaData=null;
				ArrayList<ColumnMetaData> listColumns=new ArrayList<ColumnMetaData>();
				ResultSet rsPrimaryKey=null;
			
				rsTable=dbMetaData.getTables(null,null,null,table);
				while (rsTable.next()){
					tmetaData=new TableMetaData();
					tableName=rsTable.getString("TABLE_NAME");
					tmetaData.setTableName(tableName);
					
					rsPrimaryKey=dbMetaData.getPrimaryKeys(null,null,tableName);
					pk=new Hashtable<String, Object>();
					while (rsPrimaryKey.next()){
						pk.put(rsPrimaryKey.getString("COLUMN_NAME"),null);
					}
					rsPrimaryKey.close();
					
					rsColumn=dbMetaData.getColumns(null,null,tableName,null);
					while (rsColumn.next()) {
						columnName=rsColumn.getString("COLUMN_NAME");
						columnType=rsColumn.getInt("COLUMN_TYPE");
						columnSize=rsColumn.getInt("COLUMN_SIZE");
						columnIsNull=rsColumn.getInt("NULLABLE");
						if (pk.containsKey(columnName))
							isPrimaryKey=true;
						else
							isPrimaryKey=false;
						
						colMetaData=new ColumnMetaData(columnName,columnType,columnSize,columnIsNull,isPrimaryKey);
						listColumns.add(colMetaData);
						}
					rsColumn.close();
					tmetaData.setColumnMetaData(listColumns);
					listTableMetaData.add(tmetaData);
					}
				rsTable.close();
				}
		}
		catch (SQLException sqle) {
			throw new Exception ("Error while accessing DataBaseMetaData info"+sqle.getMessage());
		}
		catch (CatalogException ce){
			throw new Exception ("Error during acessing catalog from DatabaseMetadata"+ce.getMessage());
		}
		catch (TransactionMonitorException tme) {
			throw new Exception ("Error accessing Transaction Monitor from DatabaseMetadata"+tme.getMessage());
		}
		return listTableMetaData;
	}
}

