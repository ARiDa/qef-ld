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
package ch.epfl.codimsd.qeef.discovery.datasource.dataSourceWsmoDB;

import java.sql.*;
import oracle.jdbc.OracleDriver;

public class Connection {

	private java.sql.Connection conn;
	private boolean INSERT_ACTIVE = false;

	String url;
	String user;
	String pwd;
	ConnectionObject connection;

	public Connection(ConnectionObject connection){
  	
		try {
  		
			DriverManager.registerDriver(new OracleDriver());
  	    
			//System.out.println("driver");
			this.connection=connection;
  	    
			this.conn = DriverManager.getConnection(connection.getUrl(),
					connection.getUser(), connection.getPwd());
			//System.out.println("Connection to oracle opened");	
		}
		catch (SQLException ex) {
			System.out.println("Connection exception"+ex);
		}
	}

	public String read( String query, String row ){
  
		try {
			
			String result = null;
			String result_ok = null;
			Statement stmt = conn.createStatement ();
			ResultSet rset = stmt.executeQuery (query);
			
			while (rset.next ()) {

				result = rset.getString(row);    
			
				if(result!=null){
				
					if(result_ok==null)
						result_ok=result;
					else
						result_ok= result_ok+result;
				}
			}

			rset.close();
			stmt.close();

			return result_ok;
		}
		catch (SQLException ex) {
			System.out.println("SQL exception in read Method "+ex);
			return null;
		} 
	}
  
	public void insert( String query ) {

		try {
			
			Statement stmt = conn.createStatement ();
			System.out.println("INSERT: "+query);

			if(INSERT_ACTIVE){
				stmt.executeUpdate (query);
				System.out.println("j'ai insere");
			}

			stmt.close();
		}
		catch (SQLException ex) {
			System.out.println("SQL exception in insert Method "+ex);
		}   
	}
  
	public void close() {

		try {
			conn.close();
			//System.out.println("Connection closed");
		}
		catch (SQLException ex) {
			System.out.println("Close exception "+ex);
		}
	}  
}

