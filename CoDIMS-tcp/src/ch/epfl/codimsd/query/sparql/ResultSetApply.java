package ch.epfl.codimsd.query.sparql;

import java.util.Iterator;

import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.query.ResultSet;

/**
 * Adapted to QEF from Jena ResultSetApply.
 */
public class ResultSetApply {
    ResultSetProcessor proc = null ;
    ResultSet rs = null ;
    Metadata md = null;
    
    public ResultSetApply(ResultSet rs, Metadata md, ResultSetProcessor proc) {
        this.proc = proc ;
        this.rs = rs ;
        this.md = md;
    }
    
    public void apply() {
        proc.start(md) ;
        rs.open();
        for ( ; rs.hasNext() ; ) {
            Tuple tuple = (Tuple)rs.next() ;
            Iterator<Type> values = tuple.getValues().iterator();
            proc.start(tuple, md);
            for ( int i=0; i < md.getData().size() && values.hasNext(); i++ ) {
                String varName = md.getData(i).getName();
            	Type value = values.next();
                // node may be null
                proc.binding(varName, value) ;
            }
            proc.finish(tuple, md) ;
        }
        proc.finish(md) ;
        rs.close();
    }
    
    public static void apply(ResultSet rs, Metadata md, ResultSetProcessor proc) {
        ResultSetApply rsa = new ResultSetApply(rs, md, proc) ;
        rsa.apply() ;
    }
}
