package br.ufc.lia.qef.sparql;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;

import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.Column;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.sparql.JoinQueryManipulation;
import ch.epfl.codimsd.qeef.types.StringType;
import ch.epfl.codimsd.qeef.types.rdf.UriType;


/**
 * 
 * @author Regis Pires Magalhaes
 */
public class JoinQueryManipulationTest {

	private JoinQueryManipulation joinQueryManipulation;
	private Query q1;
	private List<Tuple> instanceList1;
	private List<Tuple> instanceList2;
	
    @Before
    public void setUp() {
    	Metadata outerMetadata = new TupleMetadata();
    	outerMetadata.addData(new Column("researcher",      null, 1, 0, false));
    	outerMetadata.addData(new Column("researcher_name", null, 2, 0, false));
    	outerMetadata.addData(new Column("dblp_researcher", null, 3, 0, false));
    	
    	Metadata innerMetadata = new TupleMetadata();
    	innerMetadata.addData(new Column("publication",     null, 1, 0, false));
    	innerMetadata.addData(new Column("dblp_researcher", null, 2, 0, false));
    	innerMetadata.addData(new Column("pub_title",       null, 3, 0, false));
    	
    	this.joinQueryManipulation = new JoinQueryManipulation(outerMetadata, innerMetadata);
    	
    	String queryString = 
    		"PREFIX  dc:   <http://purl.org/dc/elements/1.1/> \n" + 
	    	"SELECT  * \n" + 
	    	"WHERE { \n" + 
	    	"  ?publication dc:creator ?dblp_researcher . \n" + 
	    	"  ?publication dc:title ?pub_title \n" + 
	    	"}";
    	this.q1 = QueryFactory.create(queryString);
    	
    	this.instanceList1 = new ArrayList<Tuple>();
    	
    	Tuple tuple = new Tuple();
    	tuple.addData(new UriType("http://localhost/researchers/1"));
    	tuple.addData(new StringType("Marco A. Casanova"));
    	tuple.addData(new UriType("http://dblp.l3s.de/d2r/resource/authors/Marco_A._Casanova"));
    	this.instanceList1.add(tuple);

    	this.instanceList2 = new ArrayList<Tuple>(this.instanceList1);
    	tuple = new Tuple();
    	tuple.addData(new UriType("http://localhost/researchers/2"));
    	tuple.addData(new StringType("Vânia Maria Ponte Vidal"));
    	tuple.addData(new UriType("http://dblp.l3s.de/d2r/resource/authors/V%C3%A2nia_Maria_Ponte_Vidal"));
    	this.instanceList2.add(tuple);
    	
    	tuple = new Tuple();
    	tuple.addData(new UriType("http://localhost/researchers/3"));
    	tuple.addData(new StringType("José Antônio Fernandes de Macedo"));
    	tuple.addData(new UriType("http://dblp.l3s.de/d2r/resource/authors/Jos%C3%A9_Ant%C3%B4nio_Fernandes_de_Mac%C3%AAdo"));
    	this.instanceList2.add(tuple);
    	
    	tuple = new Tuple();
    	tuple.addData(new UriType("http://localhost/researchers/4"));
    	tuple.addData(new StringType("Karin Koogan Breitman"));
    	tuple.addData(new UriType("http://dblp.l3s.de/d2r/resource/authors/Karin_Koogan_Breitman"));
    	this.instanceList2.add(tuple);
    	
    	tuple = new Tuple();
    	tuple.addData(new UriType("http://localhost/researchers/5"));
    	tuple.addData(new StringType("Fábio André Machado Porto"));
    	tuple.addData(new UriType("http://dblp.l3s.de/d2r/resource/authors/Fabio_Porto"));
    	this.instanceList2.add(tuple);
    	
    	tuple = new Tuple();
    	tuple.addData(new UriType("http://localhost/researchers/6"));
    	tuple.addData(new StringType("Ana Maria de Carvalho Moura"));
    	tuple.addData(new UriType("http://dblp.l3s.de/d2r/resource/authors/Ana_Maria_de_Carvalho_Moura"));
    	this.instanceList2.add(tuple);
    	
    }

    @Test
    public void testGetInnerSharedVarsPositions() {
    	assertEquals(1, this.joinQueryManipulation.getRightSharedVarsPositions().size());
    	assertEquals(1, this.joinQueryManipulation.getRightSharedVarsPositions().get(0).intValue());
    }
    
    @Test
    public void testGetKey() {
    	Tuple t1 = this.instanceList1.get(0);
    	String key = this.joinQueryManipulation.getKey(t1, this.joinQueryManipulation.getLeftSharedVarsPositions());
    	assertEquals("http://dblp.l3s.de/d2r/resource/authors/Marco_A._Casanova", key);
    }
    
    @Test
    public void testBindVariables1() {
    	try {
			Query query = this.joinQueryManipulation.bindVariables(this.q1, this.instanceList1);
			String queryString = 
					"PREFIX  dc:   <http://purl.org/dc/elements/1.1/>\n" + 
					"SELECT  *\n" + 
					"WHERE\n" + 
					"  { ?publication dc:creator ?dblp_researcher .\n" + 
					"    ?publication dc:title ?pub_title\n" + 
					"    FILTER ( ?dblp_researcher = <http://dblp.l3s.de/d2r/resource/authors/Marco_A._Casanova> )\n" + 
					"  }"; 
			assertEquals(QueryFactory.create(queryString), query);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }
    
    @Test
    public void testBindVariables2() {
    	try {
			Query query = this.joinQueryManipulation.bindVariables(this.q1, this.instanceList2);
			String queryString = 
				"PREFIX  dc:   <http://purl.org/dc/elements/1.1/>\n" + 
				"\n" + 
				"SELECT  *\n" + 
				"WHERE\n" + 
				"  {   { ?publication dc:creator ?dblp_researcher .\n" + 
				"        ?publication dc:title ?pub_title\n" + 
				"        FILTER ( ?dblp_researcher = <http://dblp.l3s.de/d2r/resource/authors/Marco_A._Casanova> )\n" + 
				"      }\n" + 
				"    UNION\n" + 
				"      { ?publication dc:creator ?dblp_researcher .\n" + 
				"        ?publication dc:title ?pub_title\n" + 
				"        FILTER ( ?dblp_researcher = <http://dblp.l3s.de/d2r/resource/authors/V%C3%A2nia_Maria_Ponte_Vidal> )\n" + 
				"      }\n" + 
				"    UNION\n" + 
				"      { ?publication dc:creator ?dblp_researcher .\n" + 
				"        ?publication dc:title ?pub_title\n" + 
				"        FILTER ( ?dblp_researcher = <http://dblp.l3s.de/d2r/resource/authors/Jos%C3%A9_Ant%C3%B4nio_Fernandes_de_Mac%C3%AAdo> )\n" + 
				"      }\n" + 
				"    UNION\n" + 
				"      { ?publication dc:creator ?dblp_researcher .\n" + 
				"        ?publication dc:title ?pub_title\n" + 
				"        FILTER ( ?dblp_researcher = <http://dblp.l3s.de/d2r/resource/authors/Karin_Koogan_Breitman> )\n" + 
				"      }\n" + 
				"    UNION\n" + 
				"      { ?publication dc:creator ?dblp_researcher .\n" + 
				"        ?publication dc:title ?pub_title\n" + 
				"        FILTER ( ?dblp_researcher = <http://dblp.l3s.de/d2r/resource/authors/Fabio_Porto> )\n" + 
				"      }\n" + 
				"    UNION\n" + 
				"      { ?publication dc:creator ?dblp_researcher .\n" + 
				"        ?publication dc:title ?pub_title\n" + 
				"        FILTER ( ?dblp_researcher = <http://dblp.l3s.de/d2r/resource/authors/Ana_Maria_de_Carvalho_Moura> )\n" + 
				"      }\n" + 
				"  }"; 

			assertEquals(QueryFactory.create(queryString), query);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    }

}
