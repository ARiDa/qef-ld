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

public class Query {

	String condition = null;
	String projection = null;
	String openCondition = null;
	String language = null;
	String stringRequest = null;
	
	public Query () {}

	public void setCondition(String condition){
		this.condition = condition;
	}
	
	public void setStringRequest(String stringRequest){
		this.stringRequest = stringRequest;
	}
	
	public String getStringRequest(){
		return stringRequest;
	}
       
	public void setProjection(String projection){
		this.projection = projection;
	}
       
	public void setOpenCondition(String openCondition){
		this.openCondition = openCondition;
	}
       
	public void setLanguage(String language){
		this.language = language;
	}
       
	public String getCondition(){
		return condition;
	}
	   
	public String getProjection(){
		return projection;
	}
	   
	public String getOpenCondition(){
		return openCondition;
	}

	public String getLanguage(){
		return language;
	}
}

