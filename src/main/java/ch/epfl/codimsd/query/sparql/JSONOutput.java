package ch.epfl.codimsd.query.sparql;

import java.io.OutputStream;

import com.hp.hpl.jena.sparql.resultset.JSONOutputASK;

import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.query.ResultSet;

/**
 * Adapted from Jena to use QEF ResultSet and Metadata.
 */
public class JSONOutput extends ResultOutput {
    public JSONOutput() {}
    
    @Override
    public void format(OutputStream out, ResultSet resultSet, Metadata md) {
        // Use direct string output - more control
    
        JSONOutputResultSetProcessor jsonOut =  new JSONOutputResultSetProcessor(out) ;
        ResultSetApply a = new ResultSetApply(resultSet, md, jsonOut) ;
        a.apply() ;
    }

    @Override
    public void format(OutputStream out, boolean booleanResult) {
        JSONOutputASK jsonOut = new JSONOutputASK(out) ;
        jsonOut.exec(booleanResult) ;
    }
}
