package ch.epfl.codimsd.query.sparql;

import java.io.OutputStream;

import com.hp.hpl.jena.sparql.resultset.XMLOutputASK;

import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.query.ResultSet;

/**
 * Adapted from Jena to use QEF ResultSet and Metadata.
 */
public class XMLOutput extends ResultOutput {
    String stylesheetURL = null ;
    boolean includeXMLinst = true ;
    
    public XMLOutput() {}
    
    public XMLOutput(String stylesheetURL) { 
    	setStylesheetURL(stylesheetURL); 
    }

    public XMLOutput(boolean includeXMLinst) { 
    	setIncludeXMLinst(includeXMLinst); 
    }
    
    public XMLOutput(boolean includeXMLinst, String stylesheetURL) { 
        setStylesheetURL(stylesheetURL); 
        setIncludeXMLinst(includeXMLinst);
    }
    
    /** @return Returns the includeXMLinst. */
    public boolean getIncludeXMLinst() { 
    	return includeXMLinst; 
    }
    
    /** @param includeXMLinst The includeXMLinst to set. */
    public void setIncludeXMLinst(boolean includeXMLinst) { 
    	this.includeXMLinst = includeXMLinst ; 
    }

    /** @return Returns the stylesheetURL. */
    public String getStylesheetURL() { 
    	return stylesheetURL ; 
    }
    
    /** @param stylesheetURL The stylesheetURL to set. */
    public void setStylesheetURL(String stylesheetURL) { 
    	this.stylesheetURL = stylesheetURL ; 
    }
    
	public void format(OutputStream out, ResultSet resultSet, Metadata md) {
        XMLOutputResultSetProcessor xOut =  new XMLOutputResultSetProcessor(out) ;
        xOut.setStylesheetURL(stylesheetURL) ;
        xOut.setXmlInst(includeXMLinst) ;
        ResultSetApply a = new ResultSetApply(resultSet, md, xOut) ;
        a.apply() ;
    }
	
    public void format(OutputStream out, boolean booleanResult) {
        XMLOutputASK xOut = new XMLOutputASK(out) ;
        xOut.exec(booleanResult) ;
    }
    
}
