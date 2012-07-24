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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import ch.epfl.codimsd.qeef.discovery.datasource.dataSourceWsmoDB.Query;

/**
 * 
 * @author Administrator
 *
 */
public class Repository {

	private static String conditionValue, conditionElement, conditionSigne;

	static Connection conn;
	static Query query;
	
	private ResultSet rset;
	
	public Repository(Query _query, Connection connection) {

		query = _query;
		conn = connection;
	}		

	public String excecuteQuery() {

		String queryF = PathAnalyser();

		System.out.println("Final Query: " + queryF);

		//---Une fois qu'on a le query Final, on l'excecute sur la table ontologies_and_webservice 
		//ou se trouve les documents XML des Ontologies et WebServices
		//---getOpenCondition represente la condition wehere a ce niveau du query. C'est une condition 
		//ouverte qui peut etre appliqu� sur une des colonnes de la table

		String queryFinal;

		if(query.getOpenCondition() != null){
			queryFinal = "select w.xml_doc.extract('"+queryF+"').getstringval() as xml from ontologies_and_webservices w "+query.getOpenCondition();
		}
		else {
			queryFinal = "select w.xml_doc.extract('"+queryF+"').getstringval() as xml from ontologies_and_webservices w";
		}
		
		String answer = read(queryFinal,"xml");
		
		return answer;		
	}
	
	public ResultSet getResultSet() {
		return rset;
	}
	
	//---Cette methode permet de combiner et transformer la condition et la projection du MetaModel et retourne le query Final
	private String PathAnalyser() {

		String cond1, proj1;

		cond1= query.getCondition();
		proj1= query.getProjection();
		System.out.println("MetaModel Condition: " +cond1 );
		System.out.println("MetaModel Projection: " +proj1 );
		System.out.println();

		cond1= "/metaModel"+query.getCondition();
		proj1= "/metaModel"+query.getProjection();
	
		String cond2 = cond2(cond1);
		String proj2 = proj2(proj1);
		String cond3, cond4;
		cond3 = cond3(cond2);	
		cond4 = cond4(cond3);

		System.out.println("Mapping Cond : " +cond3 );
		System.out.println("Mapping Proj : " +proj2 );
		System.out.println();

		String queryF = analyser(cond4,proj2);

		return queryF;
	}	
	
	
	static String cond2(String cond1){

		
		String cond2=cond1;
		
		int cond1_lastSuperieurIndex = 0;
		int cond1_lastInferieurIndex = 0;
		int cond1_lastEqualIndex = 0;
		
		cond1_lastEqualIndex = cond1.lastIndexOf("=");
		cond1_lastSuperieurIndex = cond1.lastIndexOf(">");
		cond1_lastInferieurIndex = cond1.lastIndexOf("<");
		
		if((cond1_lastEqualIndex > cond1_lastSuperieurIndex)&(cond1_lastEqualIndex > cond1_lastInferieurIndex)){
			conditionSigne = cond1.substring(cond1_lastEqualIndex,cond1_lastEqualIndex+1);
			cond2 = cond1.substring(0,cond1_lastEqualIndex);
			conditionValue = cond1.substring(cond1_lastEqualIndex+1);
		}
		else
			if(( cond1_lastSuperieurIndex > cond1_lastEqualIndex)&(cond1_lastSuperieurIndex > cond1_lastInferieurIndex)){
				conditionSigne = cond1.substring(cond1_lastSuperieurIndex,cond1_lastSuperieurIndex+1);
				cond2 = cond1.substring(0,cond1_lastSuperieurIndex);
				conditionValue = cond1.substring(cond1_lastSuperieurIndex+1);
			}
			else
				if(( cond1_lastInferieurIndex > cond1_lastEqualIndex)&(cond1_lastInferieurIndex > cond1_lastSuperieurIndex)){
					conditionSigne = cond1.substring(cond1_lastInferieurIndex,cond1_lastInferieurIndex+1);
					cond2 = cond1.substring(0,cond1_lastInferieurIndex);
					conditionValue = cond1.substring(cond1_lastInferieurIndex+1);
				}
	
		return cond2;	
	}
	
	private String cond3(String cond2){
		
		
		String cond3=cond2+"/@map";
		
		//---A ce niveau du programme cond3 est executer comme querry sur le document XML du mapping (ici mapping_query) pour recuperer le path
		//---getLanguage represente le language du mapping (ex: wsmo, rdf, owl)
		
		cond3 = "select w.xml_doc.extract('"+cond3+"').getstringval() as xml from mapping_query w where name='"+query.getLanguage()+"'";
		
		cond3 = read(cond3,"xml");
		
		return cond3;	
	}
	
	
	static String cond4(String cond3){
		
		int cond3_lastslashIndex = cond3.lastIndexOf("/");
		
		conditionElement = cond3.substring(cond3_lastslashIndex+1);
		
		String cond4 = cond3.substring(0,cond3_lastslashIndex);
		
		//cond4 = cond4+"[";
		cond4 = cond4+"/";
		cond4=cond4+conditionElement;
		cond4 = cond4+conditionSigne;
		cond4=cond4+conditionValue;
		//cond4 = cond4+"]";
		
		return cond4;	
	}
	
	private String proj2(String proj1){
		
		String proj2=proj1;
		
		int proj2_lastCrochetIndex = proj2.lastIndexOf("[");
		int proj2_lastEqualIndex = proj2.lastIndexOf("=");
		
		String projectionElement = proj2.substring(proj2_lastCrochetIndex+1,proj2_lastEqualIndex);
		
		proj2 = proj2+"/";
		proj2 = proj2+projectionElement;
		proj2 = proj2+"/@map";
		
		//----A ce niveau du programme proj2 est executer comme query sur le document XML du mapping (ici mapping_query) pour recuperer le path
		
		proj2 = "select w.xml_doc.extract('"+proj2+"').getstringval() as xml from mapping_query w where name='"+query.getLanguage()+"'";
		
		proj2 = read(proj2,"xml");

		return proj2;	
	}
	
	static String proj3(String proj2){
		
		String proj3=proj2;
	
		return proj3;	
	}

	
	//---Cette methode permet de combiner la condition et la projection du mapping recu en parametre
	static String analyser(String cond3, String proj3){
		
		
		String cond =cond3 ;
		String proj= proj3;
		String query;

		int proj_lenght = proj.length()-1;
        int proj_lastslashIndex =proj.lastIndexOf("/");		
        
        if (proj_lenght !=proj_lastslashIndex ){
        		proj=proj+"/";
        }
        
        query=proj;

        int query_lenght = query.length()-1;
        int query_lastslashIndex =query.lastIndexOf("/");

        if (query_lenght ==query_lastslashIndex ){
        		query=query.substring(0,query_lastslashIndex);
        }

        cond=cond.substring(2);
        proj=proj.substring(2);

        String cond_temp,proj_temp;

        String commun="";

        boolean test = true;

        while (test){
           boolean cas2 =false;
           int i = cond.indexOf("/");
           int k = cond.indexOf("[");
           
           if(i<0 && k>0){
                i=k;
                cas2 = true;
           }

           int j=proj.indexOf("/");

           if(i<0)
            	cond_temp=cond;
           else
            	cond_temp = cond.substring(0, i);

           if(j<0)
            	proj_temp=proj;
           else
            	proj_temp = proj.substring(0, j);
            
           if (cond_temp.equals(proj_temp)) {
           	commun = commun + cond_temp + "/";
           	
           	if(i>0){
           		cond = cond.substring(i);
           		if(!cas2)
           		cond = cond.substring(1);
           	}

           	if(j>0){
           		proj = proj.substring(j);
           		proj = proj.substring(1);
           	}

           }	
           else
           	test=false;
        	}

        	int retour = slashCounter(proj);
        
        String query_cond = "[";
        
        while (retour>0){
            query_cond=query_cond+"../";
            retour--;
        }

        query_cond=query_cond+cond;

        query_cond = query_cond+"]";

        query=query+query_cond;

		return query;
		
	}

    static int slashCounter(String st){

        int nb=0;
        int temp=0;
        boolean test =true;
        while (test){
            temp = st.indexOf("/");
            if (temp>0){
                st=st.substring(temp);
                st = st.substring(1);
                nb++;
            }
            else test=false;

            if(temp<0 && !st.equals("")){
                nb++;
                return nb;
            }
        }

        if(nb==0)
            return nb;
        else
            return nb;
    }
    
    private String read( String query, String row ){
    	  
		try {
			
			String result = null;
			String result_ok = null;
			Statement stmt = conn.createStatement ();
			ResultSet rset = stmt.executeQuery (query);
			// Add by Othman : this.rset = rset;
			this.rset = rset;
			ResultSetMetaData rsmd = rset.getMetaData();
			int numberOfColumns = rsmd.getColumnCount();
			
			@SuppressWarnings("unused")
			String columnName = "";
		
			for (int i=1;i<=numberOfColumns;i++) {
				columnName=rsmd.getColumnName(i);
			} 
		
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
}

