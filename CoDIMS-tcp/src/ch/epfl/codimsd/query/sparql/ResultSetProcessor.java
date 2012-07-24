package ch.epfl.codimsd.query.sparql;

import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.Type;

/**
 * Adapted to QEF from Jena ResultSetApply.
 */
public interface ResultSetProcessor {
    /** Start result set */
    public void start(Metadata rs);
    /** Finish result set */
    public void finish(Metadata rs);
    
    /**  Start query solution (row in result set) */
    public void start(Tuple tuple, Metadata metadata);

    /**  Finish query solution (row in result set) */
    public void finish(Tuple tuple, Metadata metadata);
    
    
    /** A single (variable, value) pair in a query solution
     *  - the value may be null indicating that the variable
     *  was not present in this solution. 
     *   
     * @param varName
     * @param value
     */
    public void binding(String varName, Type value) ;
}