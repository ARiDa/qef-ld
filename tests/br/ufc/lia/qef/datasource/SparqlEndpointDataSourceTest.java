package br.ufc.lia.qef.datasource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.epfl.codimsd.qeef.sparql.datasource.SparqlEndpointDataSource;


import static junit.framework.Assert.assertEquals;

public class SparqlEndpointDataSourceTest {

    private SparqlEndpointDataSource dataSource;
    
    @Before
    public void setUp() {
        String serviceURI = "http://pt.dbpedia.org/sparql";
        String queryString = "select * where { ?x ?y ?z } limit 10";
        this.dataSource = new SparqlEndpointDataSource("DBPedia", null, serviceURI, queryString);
        try {
            this.dataSource.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testSparqlEndpointDataSource(){
        try {
            int i = 0;
            while (dataSource.read() != null) {
                i++;
            }
            assertEquals(10, i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        try {
            dataSource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
