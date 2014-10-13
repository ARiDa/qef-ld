package ch.epfl.codimsd.qeef.sparql;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ch.epfl.codimsd.helper.MemMon;
import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.StringType;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.types.rdf.UriType;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.query.RequestResult;
import ch.epfl.codimsd.query.ResultSet;

/**
 * 
 * @author Regis Pires Magalhaes
 */
public class QueryManipulation {

	/**
	 * Gets query parameter Names from a query string.
	 * Parameter Names begins with '?:'.
	 * @param query
	 * @return
	 */
	public static Set<String> queryParamNames(String query) {
		int i = 0;
		Set<String> paramNames = null;
		while ( (i = query.indexOf("?:", i)) != -1 ) {
			if (paramNames == null) {
				paramNames = new HashSet<String>(); 
			}
			char c;
			StringBuilder paramName = new StringBuilder();
			for ( ; i < query.length() && isParamChar( (c = query.charAt(i)) ) ; i++) {
				paramName = paramName.append(c);
			}
			paramNames.add(paramName.toString());
		}
		return paramNames;
	}
	
	/**
	 * Returns if the char can be part of a parameter or not.
	 * @param c
	 * @return
	 */
	public static boolean isParamChar(char c) {
		return c == '?' || c == ':' || Character.isJavaIdentifierPart(c);
	}
	
	/**
	 * Prints query results.
	 * @param requestResult
	 * @throws IOException 
	 */
	public static int printQueryResults(RequestResult requestResult, Writer out) throws IOException {
		int i = 0;
		ResultSet rs = requestResult.getResultSet();
		Metadata  mt = requestResult.getResultMetadata();
		rs.open();
		if (! rs.hasNext()) {
			out.write("No results found.");
		} else {
			System.out.println("Outputting results...");
			for(i=1; rs.hasNext(); i++) {
				MemMon.setMsg("Result #" + i + " - ");
				Tuple tp = (Tuple)rs.next();
				out.write(i + ": ");
				out.write(formatSparqlQueryResult(tp, mt));
				out.write(Constants.LINE_SEPARATOR);
			}
			i--;
		}
		rs.close();
		return i;
	}
	
	/**
	 * Formats a SPARQL query result.
	 * @param tp
	 * @param mt
	 * @return
	 */
	public static String formatSparqlQueryResult(Tuple tp, Metadata mt) {
		StringBuilder sb = new StringBuilder();
		Iterator<Type> values = tp.getValues().iterator();
		Iterator<Data> data = mt.getData().iterator();
		while (values.hasNext()) {
			Type value = values.next();
			
			// Inserts variable
			sb.append("( ?").append(data.next().getName()).append(" = ");
			
			// Inserts formatted value
			sb.append(format(value, false));
			
			sb.append(" ) ");
		}
		return sb.toString();
	}
	
	public static String format(Type value, boolean web) {
		StringBuilder sb = new StringBuilder();
		// Inserts delimiter before value
		if (value instanceof StringType) {
			sb.append("\"");
		} else if (value instanceof UriType) {
			sb.append(web ? "&lt;" : "<");
		}

		// Inserts value
		if (web) {
			sb.append("<a href=\"" + value + "\">");
		}
		sb.append(value);
		if (web) {
			sb.append("</a>");
		}

		// Inserts delimiter after value
		if (value instanceof StringType) {
			sb.append("\"");
		} else if (value instanceof UriType) {
			sb.append(web ? "&gt;" : ">");
		}
		return sb.toString();
	}
	
}
