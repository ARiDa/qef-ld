package ch.epfl.codimsd.query.sparql;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.query.ResultSet;

/**
 * Adapted from Jena to use QEF ResultSet and Metadata.
 */
public abstract class ResultOutput {

    private static Logger logger = Logger.getLogger(ResultOutput.class.getName());
	
    /** Format a result set - output on the given stream
     * @param out
     * @param resultSet
     */
    
    public abstract void format(OutputStream out, ResultSet resultSet, Metadata md);

    /** Format a boolean result - output on the given stream
     * @param out
     * @param booleanResult
     */
    
    public abstract void format(OutputStream out, boolean booleanResult);

    public String asString(ResultSet resultSet, Metadata md) {
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        format(arr, resultSet, md) ;
        try { return new String(arr.toByteArray(), "UTF-8") ; }
        catch (UnsupportedEncodingException e) {
            logger.warn("UnsupportedEncodingException");
            return null ;
        }
    }

    public String asString(boolean booleanResult) {
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        format(arr, booleanResult) ;
        try { return new String(arr.toByteArray(), "UTF-8") ; }
        catch (UnsupportedEncodingException e) {
            logger.warn("UnsupportedEncodingException");
            return null ;
        }
    }
	
}
