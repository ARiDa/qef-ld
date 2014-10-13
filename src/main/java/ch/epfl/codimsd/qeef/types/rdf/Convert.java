package ch.epfl.codimsd.qeef.types.rdf;

import ch.epfl.codimsd.qeef.types.FloatType;
import ch.epfl.codimsd.qeef.types.IntegerType;
import ch.epfl.codimsd.qeef.types.StringType;
import ch.epfl.codimsd.qeef.types.Type;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueFloat;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueInteger;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueNode;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class Convert {

	/**
	 * Converts RDFNodes to types used in QEF.
	 * @param rdfNode
	 * @return
	 * @throws RuntimeException
	 */
	public static Type rdfNode2QefType(RDFNode rdfNode) throws RuntimeException {
		if (rdfNode == null) {
			return null;
		}
		Type type = null;
		if (rdfNode.isURIResource()) {
		    Resource resource = rdfNode.asResource();
		    type = new UriType(resource.getURI());
		    
		} else if (rdfNode.isAnon()) {
		    Node node = rdfNode.asNode();
		    type = new BlankNodeType(node.getBlankNodeId().getLabelString());
		    
		} else if (rdfNode.isLiteral()) {
		    Literal literal = rdfNode.asLiteral();
		    type = new LiteralType(literal.getLexicalForm(), literal.getDatatypeURI(), literal.getLanguage());
		    
//		    RDFDatatype dataType =  literal.getDatatype();
		    
//		    if (dataType == null || dataType.getJavaClass() == String.class) {
//		    	type = new StringType(literal.getString());
//		    } else if (dataType.getJavaClass() == Integer.class) {
//		    	type = new IntegerType(literal.getInt());
//		    } else if (dataType.getJavaClass() == Float.class) {
//		    	type = new FloatType(literal.getFloat());
//		    } else {
//		    	type = new StringType(literal.toString());
//		    }
		    
		} else {
		    throw new RuntimeException("Invalid node type for: " + rdfNode.toString());
		}
		return type;
	}
	

	/**
	 * Creates a Jena SPARQL expression given a type and an object value.
	 * @param type
	 * @param value
	 * @return Jena SPARQL expression
	 */
	public static Expr QefType2JenaExpr(Type value) {
		Expr expr = null;
    	if (value instanceof UriType) {
    		Node uri = Node.createURI(value.toString());
    		expr = new NodeValueNode(uri);
    	} else if (value instanceof StringType) {
    		expr = new NodeValueString(value.toString());
    	} else if (value instanceof IntegerType) {
    		expr = new NodeValueInteger(((IntegerType)value).intValue());
    	} else if (value instanceof FloatType) {
    		expr = new NodeValueFloat(((FloatType)value).floatValue());
    	} else {
    		expr = new NodeValueString(value.toString()); 
    	}
		return expr;
	}
	
}



