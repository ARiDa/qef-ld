package ch.epfl.codimsd.query.sparql;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.openjena.atlas.io.IndentedWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.types.rdf.BlankNodeType;
import ch.epfl.codimsd.qeef.types.rdf.LiteralType;
import ch.epfl.codimsd.qeef.types.rdf.UriType;

import com.hp.hpl.jena.sparql.resultset.XMLResults;

/**
 * Adapted to QEF from Jena XMLOutputResultSet.
 */
public class XMLOutputResultSetProcessor implements ResultSetProcessor, XMLResults {

	final static Logger logger = LoggerFactory.getLogger(XMLOutputResultSetProcessor.class);
	
    static boolean outputExplicitUnbound = false ;
    
    boolean outputGraphBNodeLabels = false;

    int index = 0 ;                     // First index is 1 
    String stylesheetURL = null ;
    boolean xmlInst = true ;

    IndentedWriter out ;
    int bNodeCounter = 0 ;
    Map<String, String> bNodeMap = new HashMap<String, String>() ;
    
    XMLOutputResultSetProcessor(OutputStream outStream) {
        this(new IndentedWriter(outStream)) ;
    }
    
    XMLOutputResultSetProcessor(IndentedWriter indentedOut) {
        out = indentedOut ;
    }
    
    @Override
    public void start(Metadata md)
    {
        if ( xmlInst )
            out.println("<?xml version=\"1.0\"?>") ;
        
        if ( stylesheetURL != null )
            out.println("<?xml-stylesheet type=\"text/xsl\" href=\""+stylesheetURL+"\"?>") ;
        
        // ---- Root
        out.print("<"+dfRootTag) ;
        out.print(" ") ;
        out.println("xmlns=\""+dfNamespace+"\">") ;

        // ---- Header
        out.incIndent(INDENT) ;
        out.println("<"+dfHead+">") ;
        
        for (int i=0; i < md.getData().size(); i++)
        {
            Data data = md.getData(i);
            String name = data.getName();
            out.incIndent(INDENT) ;
            out.print("<") ;
            out.print(dfVariable) ;
            out.print(" "+dfAttrVarName+"=\""+name+"\"") ;
            out.println("/>") ;
            out.decIndent(INDENT) ;
        }
        out.println("</"+dfHead+">") ;
        out.decIndent(INDENT) ;
        
        // Start results proper
        out.incIndent(INDENT) ;
        out.println("<"+dfResults+">") ;
        out.incIndent(INDENT) ;
    }

    @Override
    public void finish(Metadata md)
    {
        out.decIndent(INDENT) ;
        out.println("</"+dfResults+">") ;
        out.decIndent(INDENT) ;
        out.println("</"+dfRootTag+">") ;
        out.flush() ;
    }

    @Override
    public void start(Tuple tuple, Metadata metadata)
    {
        out.println("<"+dfSolution+">") ;
        index ++ ;
        out.incIndent(INDENT) ;
    }

    @Override
    public void finish(Tuple tuple, Metadata metadata)
    {
        out.decIndent(INDENT) ;
        out.println("</"+dfSolution+">") ;
    }

    @Override
    public void binding(String varName, Type value)
    {
        if ( value == null && ! outputExplicitUnbound )
            return ;
        
        out.print("<") ; 
        out.print(dfBinding) ;
        out.println(" name=\""+varName+"\">") ;
        out.incIndent(INDENT) ;
        printBindingValue(value) ;
        out.decIndent(INDENT) ;
        out.println("</"+dfBinding+">") ;
    }
        
    void printBindingValue(Type value)
    {
        if ( value == null )
        {
            // Unbound
            out.println("<"+dfUnbound+"/>") ;
            return ;
        }
        
        if ( value instanceof UriType || value instanceof BlankNodeType ) {
            printResource(value) ;
            return ;
            
        } else if ( value instanceof LiteralType ) {
            printLiteral((LiteralType)value) ;
            return ;
        	
        }
        
        logger.warn("Unknown Type in result set: {}", value.getClass()) ;
    }
    
    void printLiteral(LiteralType literal)
    {
        String datatype = literal.getDataType();
        String lang = literal.getLanguage();
        
        out.print("<"+dfLiteral) ;
        
        if ( lang != null && !(lang.length()==0) )
            out.print(" xml:lang=\""+lang+"\"") ;
            
        if ( datatype != null && ! datatype.equals(""))
        {
            out.print(" "+dfAttrDatatype+"=\""+datatype+"\"") ;
        }
            
        out.print(">") ;
        //out.print(xml_escape(literal.getLexicalForm())) ;
        out.print(xml_escape(literal.toString())) ;
        out.println("</"+dfLiteral+">") ;
    }
    
    void printResource(Type r)
    {
        if ( r instanceof BlankNodeType ) 
        {
            String label ;
            
            if ( outputGraphBNodeLabels )
                //label = r.asNode().getBlankNodeId().getLabelString() ;
                label = r.toString() ;
            else
            {
                if ( ! bNodeMap.containsKey(r.toString()))
                    bNodeMap.put(r.toString(), "b"+(bNodeCounter++)) ;
                label = bNodeMap.get(r.toString()) ;
            }
            out.println("<"+dfBNode+">"+label+"</"+dfBNode+">") ;
        }
        else
        {
            //out.println("<"+dfURI+">"+xml_escape(r.getURI())+"</"+dfURI+">") ;
            out.println("<"+dfURI+">"+xml_escape(r.toString())+"</"+dfURI+">") ;
        }
    }
    
    private static String xml_escape(String string)
    {
        final StringBuilder sb = new StringBuilder(string);
        
        int offset = 0;
        String replacement;
        char found;
        for (int i = 0; i < string.length(); i++) {
            found = string.charAt(i);
            
            switch (found) {
                case '&' : replacement = "&amp;"; break;
                case '<' : replacement = "&lt;"; break;
                case '>' : replacement = "&gt;"; break;
                case '\r': replacement = "&#x0D;"; break;
                case '\n': replacement = "&#x0A;"; break;
                default  : replacement = null;
            }
            
            if (replacement != null) {
                sb.replace(offset + i, offset + i + 1, replacement);
                offset += replacement.length() - 1; // account for added chars
            }
        }
        
        return sb.toString();
    }

    /** @return Returns the stylesheetURL. */
    public String getStylesheetURL()
    { return stylesheetURL ; }

    /** @param stylesheetURL The stylesheetURL to set. */
    public void setStylesheetURL(String stylesheetURL)
    { this.stylesheetURL = stylesheetURL ; }

    /** @return Returns the xmlInst. */
    public boolean getXmlInst()
    { return xmlInst ; }

    /** @param xmlInst The xmlInst to set. */
    public void setXmlInst(boolean xmlInst)
    { this.xmlInst = xmlInst ; }
}
