package ch.epfl.codimsd.query.sparql;

/**
 * Adapted to QEF from Jena JSONResultsKW.
 */
public interface JSONResults {
    public static String kHead          = "head" ;
    public static String kVars          = "vars" ;
    public static String kLink          = "link" ;
    public static String kResults       = "results" ;
    public static String kBindings      = "bindings" ;
    public static String kType          = "type" ;
    public static String kUri           = "uri"  ;
    public static String kValue         = "value" ;
    public static String kLiteral       = "literal" ;
    public static String kTypedLiteral  = "typed-literal" ;
    public static String kXmlLang       = "xml:lang" ;
    public static String kDatatype      = "datatype" ;
    public static String kBnode         = "bnode" ;
    public static String kBoolean       = "boolean" ;	
}
