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

import java.util.ArrayList;

public class TableMetaData {
	String tableName=null;
	ArrayList <ColumnMetaData> columnList=null;
	
	public TableMetaData(){};
	
	protected void setTableName(String table){
		this.tableName=table;
	 }
	protected void setColumnMetaData(ArrayList <ColumnMetaData> columnList){
		this.columnList=columnList;
	 }
	public String getTableName() {
		return tableName;
	}
	public ArrayList getColumns() {
		return columnList;
	}
}

