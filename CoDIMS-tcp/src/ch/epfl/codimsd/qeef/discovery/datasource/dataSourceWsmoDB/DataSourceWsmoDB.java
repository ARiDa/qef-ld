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

import java.io.IOException;

import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataUnit;

import ch.epfl.codimsd.qeef.relational.TupleMetadata;

public class DataSourceWsmoDB extends DataSource {

	public DataSourceWsmoDB(String queryConditions, TupleMetadata metadata) {
		
		super(queryConditions, metadata);
	}

	public void open() throws Exception {
		
		ConnectionObject connection = new ConnectionObject();
		
		connection.setUrl("jdbc:oracle:thin:@lbdsun7.epfl.ch:1521:lbd10");
		connection.setUser("amine");
		connection.setPwd("amine");

		Query query = new Query();
		query.setCondition("/webService/Entity[type=\"webService\"]/EntityName=\"WebService_1\"");
		query.setProjection("/webService/Entity[type=\"webService\"]");
	
		//query.setCondition("/webService/Entity[type=\"webService\"]/MetaInfo/Property[Name=\"quality\"]/Value=10");
		//query.setProjection("/webService/Entity[type=\"webService\"]/MetaInfo/Property[Name=\"quality\"]");

		//query.setCondition("/Ontology/Entity[type=\"Concept\"]/EntityName=\"country_5\"");
		//query.setProjection("/Ontology/Entity[type=\"Concept\"]/Component[type=\"Attribute\"]");

		//--- setOpenCondition() is optional
		//query.setOpenCondition("where name='example_3'");
		query.setLanguage("wsmo_map_minimized");	

		Repository rep = new Repository(query,connection);
		String result = rep.excecuteQuery();
		
		parse(result);
	}
	
	public DataUnit read() throws IOException {
		return null;
	}
	
	public void close() {}
	
	private void parse(String wsmoFile) {}
	
	
}

