package ch.epfl.codimsd.qeef.sparql.datasource;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.relational.Column;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.sparql.JoinQueryManipulation;
import ch.epfl.codimsd.qeef.sparql.QueryManipulation;
import ch.epfl.codimsd.qeef.types.rdf.Convert;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class SparqlEndpointDataSource extends DataSource {
    
    private String serviceURI;
    private String queryString;
    private QueryExecution queryExecution;
    private ResultSet results;
    private int numberOfVariables;
    
    final static Logger logger = LoggerFactory.getLogger(SparqlEndpointDataSource.class);

    public SparqlEndpointDataSource(String alias, TupleMetadata metadata, String serviceURI, String queryString) {
    	// metadata param must be TupleMetadata  
        super(alias, metadata);
        this.serviceURI = serviceURI;
        this.queryString = queryString;
		logger.debug("SparqlEndpointDatasource - Service URI: {} - Query: {}", this.serviceURI, queryString);
    }
    
    /**
     * The query will be executed only when the first tuple will be read.
     */
    @Override
    public void open() throws Exception {
		logger.debug("Executing SPARQL Query... Service URI: {} - Query: {}", this.serviceURI, this.queryString);

		Query query = QueryFactory.create(this.queryString);
		
        // Executing query
		this.queryExecution = QueryExecutionFactory.sparqlService(this.serviceURI, query);
        
        this.metadata = new TupleMetadata();

        List<Var> projectVars = query.getProjectVars();
        this.numberOfVariables = projectVars.size(); 
        
        for (int i = 0; i < this.numberOfVariables; ++i) {
            Column column = new Column(projectVars.get(i).getVarName(), null, 1, i, false);
            this.metadata.addData(column);
        }
        
        setMetadata(metadata);
        
        // Put Metadata of the dataSource in the BlackBoard.
        BlackBoard bl = BlackBoard.getBlackBoard();
        bl.put("Metadata", metadata);
        
		logger.debug("SparqlEndpointDatasource opened successfully.\nNumber of variables: {}", this.numberOfVariables);
        
    }

    /**
     * Fills the buffer (resultSet) if it is empty and gets the next tuple.
     */
    @Override
    public DataUnit read() throws Exception {
    	// Executes the query and fills the buffer (ResultSet) if it is empty.
    	if (this.results == null) {
    		if (this.queryExecution == null) {
    			open();
    		}
    		long startTime = System.currentTimeMillis(); 
			this.results = this.queryExecution.execSelect();
			logger.debug("Query execution time (ms): {}", (System.currentTimeMillis() - startTime));
    	}
    	
    	// Reads the buffer (ResultSet) to get the next solution and returns it as a tuple.
    	Tuple tuple = null;
        if (this.results.hasNext()) {
            tuple = new Tuple();
            QuerySolution querySolution = this.results.nextSolution();
    		logger.debug("Native datasource querySolution: {}", querySolution);
    		
            for (int i = 0; i < this.numberOfVariables; ++i) {
                String variableName = metadata.getData(i).getName();
                RDFNode rdfNode = querySolution.get(variableName);
                tuple.addData(Convert.rdfNode2QefType(rdfNode));
            }
        }
        return tuple;
    }

    @Override
    public void close() throws Exception {
        if (this.queryExecution != null) {
        	this.queryExecution.close();
        	this.queryExecution = null;
        	this.results = null;
    		logger.debug("SparqlEndpointDatasource - closed successfully");
        } else {
        	throw new Exception("The SparqlEndpointDatasource related to " + this.serviceURI + " should have been opened.");
        }
    }

	@Override
	public DataSource cloneDatasource(Map<String, Object> params) throws Exception {
		logger.debug("Starting cloneDatasource...");
		String queryStr = this.queryString;

		if (params.get("paramNames") != null && params.get("paramValues") != null) {
			// Replace named parameters by their respective values
			String[] paramNames = (String[]) params.get("paramNames");
			String[] paramValues = (String[]) params.get("paramValues");
 			queryStr = StringUtils.replaceEach(this.queryString, paramNames, paramValues);
			
		} else if (params.get("joinQueryManipulation") != null) {
    		// Query reformulation -> "bind" variables
    		JoinQueryManipulation joinQueryManipulation = (JoinQueryManipulation)params.get("joinQueryManipulation");
			Collection leftInstances = (Collection)params.get("leftTuples");
            
			Query query = QueryFactory.create(this.queryString);
    		query = joinQueryManipulation.bindVariables(query, leftInstances);
    		queryStr = query.toString();
    	}
		
		logger.debug("Query after cloneDatasource: {}", queryStr);
		DataSource ds = new SparqlEndpointDataSource(this.alias, (TupleMetadata)this.metadata, this.serviceURI, queryStr);
		return ds;
	}

	
    @Override
	public void processMessage(Map<String, Object> params) {
		
		// Adds these datasource named parameters to the global named parameters. 
    	if (params.get("namedParams") != null) {
			Set<String> globalParamNames = (Set<String>)params.get("namedParams");
			
			Set<String> paramNames = QueryManipulation.queryParamNames(this.queryString);
			if (paramNames != null) {
				Iterator<String> it = paramNames.iterator();
				while (it.hasNext()) {
					globalParamNames.add(it.next());
				}
			}
		}
		
	}
	
}
