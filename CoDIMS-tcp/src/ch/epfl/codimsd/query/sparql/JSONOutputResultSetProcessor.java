package ch.epfl.codimsd.query.sparql;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.openjena.atlas.io.IndentedWriter;
import org.openjena.atlas.json.io.JSWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.types.rdf.BlankNodeType;
import ch.epfl.codimsd.qeef.types.rdf.LiteralType;
import ch.epfl.codimsd.qeef.types.rdf.UriType;

/**
 * Adapted to QEF from Jena JSONOutputResultSet.
 */
public class JSONOutputResultSetProcessor implements ResultSetProcessor, JSONResults {
	
    final static Logger logger = LoggerFactory.getLogger(JSONOutputResultSetProcessor.class);
	
	static boolean multiLineValues = false ;
    static boolean multiLineVarNames = false ;
    
    private boolean outputGraphBNodeLabels = false ;
    private IndentedWriter out ;
    private int bNodeCounter = 0 ;
    private Map<String, String> bNodeMap = new HashMap<String, String>();
    
    JSONOutputResultSetProcessor(OutputStream outStream) { 
    	this(new IndentedWriter(outStream)); 
    }
    
    JSONOutputResultSetProcessor(IndentedWriter indentedOut) {   
    	out = indentedOut ;
        outputGraphBNodeLabels = false;
    }
    
    @Override
    public void start(Metadata mt) {
        out.println("{") ;
        out.incIndent() ;
        doHead(mt) ;
        out.println(quoteName(kResults)+": {") ;
        out.incIndent() ;
        out.println(quoteName(kBindings)+": [") ;
        out.incIndent() ;
        firstSolution = true ;
    }

    @Override
    public void finish(Metadata mt) {
        // Close last binding.
        out.println() ;
        
        out.decIndent() ;       // bindings
        out.println("]") ;
        out.decIndent() ;
        out.println("}") ;      // results
        out.decIndent() ;
        out.println("}") ;      // top level {}
        out.flush() ;
    }

    private void doHead(Metadata mt)
    {
        out.println(quoteName(kHead)+": {") ;
        out.incIndent() ;
        doLink(mt) ;
        doVars(mt) ;
        out.decIndent() ;
        out.println("} ,") ;
    }
    
    private void doLink(Metadata mt)
    {
        // ---- link
        //out.println("\"link\": []") ;
    }
    
    private void doVars(Metadata mt) {
        // On one line.
        out.print(quoteName(kVars)+": [ ") ;
        if ( multiLineVarNames ) out.println() ;
        out.incIndent() ;
        
        Iterator<Data> it = mt.getData().iterator();
        while (it.hasNext()) {
            String varname = it.next().getName() ;
            out.print("\""+varname+"\"") ;
            if ( multiLineVarNames ) out.println() ;
            if ( it.hasNext() )
                out.print(" , ") ;
        }
        out.println(" ]") ;
        out.decIndent() ;
    }

    boolean firstSolution = true ;
    boolean firstBindingInSolution = true ;
    
    // NB assumes are on end of previous line.
    @Override
    public void start(Tuple tuple, Metadata metadata)
    {
        if ( ! firstSolution )
            out.println(" ,") ;
        firstSolution = false ;
        out.println("{") ;
        out.incIndent() ;
        firstBindingInSolution = true ;
    }

    @Override
    public void finish(Tuple tuple, Metadata metadata)
    {
        out.println() ;     // Finish last binding
        out.decIndent() ;
        out.print("}") ;    // NB No newline
    }

    @Override
    public void binding(String varName, Type value)
    {
        if ( value == null )
            return ;
        
        if ( !firstBindingInSolution )
            out.println(" ,") ;
        firstBindingInSolution = false ;

        // Do not use quoteName - varName may not be JSON-safe as a bare name.
        out.print(quote(varName)+": { ") ;
        if ( multiLineValues ) out.println() ;
        
        out.incIndent() ;
        // Old, explicit unbound
//        if ( value == null )
//            printUnbound() ;
//        else
        
        if ( value instanceof UriType || value instanceof BlankNodeType )
        	printResource(value) ;
        else if ( value instanceof LiteralType )
        	printLiteral((LiteralType)value) ;
        else 
        	logger.warn("Unknown Type in result set: {}", value.getClass()) ;
        out.decIndent() ;
        
        if ( !multiLineValues ) out.print(" ") ; 
        out.print("}") ;        // NB No newline
    }
    
//    private void printUnbound()
//    {
//        out.print(quoteName(kType)+ ": "+quote(kUnbound)+" , ") ;
//        if ( multiLineValues ) out.println() ;
//        out.print(quoteName(kValue)+": null") ;
//        if ( multiLineValues ) out.println() ;
//    }

    private void printLiteral(LiteralType literal) {
        String datatype = literal.getDataType();
        String lang = literal.getLanguage();
        
        if ( datatype != null ) {
            out.print(quoteName(kDatatype)+": "+quote(datatype)+" , ") ;
            if ( multiLineValues ) out.println() ;
            
            out.print(quoteName(kType)+": "+quote(kTypedLiteral)+" , ") ;
            if ( multiLineValues ) out.println() ;
        } else {
            out.print(quoteName(kType)+": "+quote(kLiteral)+" , ") ;
            if ( multiLineValues ) out.println() ;
            
            if ( lang != null && !lang.equals("") )
            {
                out.print(quoteName(kXmlLang)+": "+quote(lang)+" , ") ;
                if ( multiLineValues ) out.println() ;
            }
        }            
        //out.print(quoteName(kValue)+": "+quote(literal.getLexicalForm())) ;
        out.print(quoteName(kValue)+": "+quote(literal.toString())) ;
        if ( multiLineValues ) out.println() ;
    }

    private void printResource(Type resource)
    {
    	if ( resource instanceof BlankNodeType ) 
        {
            String label ; 
            if ( outputGraphBNodeLabels )
                //label = resource.getId().getLabelString() ;
            	label = resource.toString();
            else
            {
                if ( ! bNodeMap.containsKey(resource.toString()))
                    bNodeMap.put(resource.toString(), "b"+(bNodeCounter++)) ;
                label = bNodeMap.get(resource.toString()) ;
            }
            
            out.print(quoteName(kType)+": "+quote(kBnode)+" , ") ;
            if ( multiLineValues ) out.println() ;
            
            out.print(quoteName(kValue)+": "+quote(label)) ;
            
            if ( multiLineValues ) out.println() ;
        }
        else
        {
            out.print(quoteName(kType)+": "+quote(kUri)+" , ") ;
            if ( multiLineValues ) out.println() ;
            out.print(quoteName(kValue)+": "+quote(resource.toString())) ;
            if ( multiLineValues ) out.println() ;
            return ;
        }
    }
    
    private static String quote(String string)
    {
        return JSWriter.outputQuotedString(string) ;
    }
    
    // Quote a name (known to be JSON-safe)
    // Never the RHS of a member entry (for example "false")
    // Some (the Java JSON code for one) JSON parsers accept an unquoted
    // string as a name of a name/value pair.
    
    private static String quoteName(String string)
    {
        // Safest to quote anyway.
        return quote(string) ;
        
        // Assumes only called with safe names
        //return string ;
        
        // Better would be:
        // starts a-z, constains a-z,0-9, not a keyword(true, false, null)
//        if ( string.contains(something not in a-z0-9)
//        and         
//        //return "\""+string+"\"" ;
//        return JSONObject.quote(string) ;
    }

}