package ch.epfl.codimsd.qeef.sparql;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;

import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.types.rdf.Convert;

/**
 * 
 * @author Regis Pires Magalhaes
 */
public class JoinQueryManipulation {

	private Metadata leftMetadata;
	private Metadata rightMetadata;
	private final List<Integer> leftSharedVarsPositions = new Vector<Integer>();
	private final List<Integer> rightSharedVarsPositions = new Vector<Integer>();
	
	public JoinQueryManipulation(Metadata outerMetadata, Metadata innerMetadata) {
		this.leftMetadata = outerMetadata;
		this.rightMetadata = innerMetadata;
		findSharedVarsPositions();
	}

	/**
	 * Generates an inner query with bound variables from the original inner query and
	 * the outer instance list.
	 * @param rightQuery
	 * @param leftTupleCollection Collection of Lists, where each list contains left tuples.
	 * @return
	 * @throws Exception
	 */
	public Query bindVariables(Query rightQuery, Collection leftTupleCollection) throws Exception {
        rightQuery = rightQuery.cloneQuery();
		
        // Change query from left side of join to "bind" shared variables using FILTER
        ElementGroup originalElementGroup = (ElementGroup)rightQuery.getQueryPattern();

        // ROOT GROUP
        ElementGroup rootElementGroup = null;
        
        // Creates a filter do "bind" the shared variables.
        // We use filter because many SPARQL Endpoints don't accept BINDINGS.
        if (getRightSharedVarsPositions().size() > 0) {
        	// UNION
        	// Creates one query to get results related to results got from right query
        	// using UNION if there is more than one tuple in leftTupleCollection.
        	ElementUnion elementUnion = null;
        	if (leftTupleCollection.size() > 1) {
        		rootElementGroup = new ElementGroup();
        		elementUnion = new ElementUnion();
        		rootElementGroup.addElement(elementUnion);
        	} else {
        		rootElementGroup = originalElementGroup;
        	}
        	
        	// Left tuple collection can be a collection of tuples or
        	// a collection of a collection of tuples.
        	for (Object obj : leftTupleCollection) {
        		if (obj instanceof Tuple) {
            		processTuple((Tuple)obj, leftTupleCollection.size(), originalElementGroup, rootElementGroup, elementUnion);
        		} else if (obj instanceof Collection) {
        			// Use only the first element of the collection, since the key of all elements of the collection is the same and
        			// only the key is needed and used to create the filter.
        			Collection leftTupleCol = (Collection) obj;
    				Iterator it = leftTupleCol.iterator();
    				if (it.hasNext()) {
    					Object tuple = it.next();
    					processTuple((Tuple)tuple, leftTupleCollection.size(), originalElementGroup, rootElementGroup, elementUnion);
    				}
        		} else {
        			new IllegalArgumentException("Invalid type: " + obj.getClass().getName() + ". Should be Tuple or Collection.");
        		}
        	}
        }
        rightQuery.setQueryPattern(rootElementGroup);
        return rightQuery;
	}

	
	private void processTuple(Tuple leftInstance, int numberOfLeftTuples, ElementGroup originalElementGroup, ElementGroup rootElementGroup, ElementUnion elementUnion) {
    	// Creates the first expression related to the first shared var
    	int pos = getRightSharedVarsPositions().get(0);
    	String sharedVarName = this.rightMetadata.getData(pos).getName();
        Expr expr = generateEqualsExpresssion(this.leftMetadata, leftInstance, sharedVarName);

        // Creates the other expressions
        for(int i=1; i < getRightSharedVarsPositions().size(); i++) {
        	pos = getRightSharedVarsPositions().get(i);
        	sharedVarName = this.rightMetadata.getData(pos).getName();
        	Expr rightExpr = generateEqualsExpresssion(this.leftMetadata, leftInstance, sharedVarName);
        	expr = new E_LogicalAnd(expr, rightExpr);
        }
        ElementFilter elementFilter = new ElementFilter(expr);
    	if (numberOfLeftTuples > 1) {
    		ElementGroup elementGroup = createElementGroup(originalElementGroup);
    		elementGroup.addElementFilter(elementFilter);
    		elementUnion.addElement(elementGroup);
    	} else {
    		rootElementGroup.addElementFilter(elementFilter);
    	}
	}
	
	/**
	 * Creates a new ElementGroup using existing elements from another ElementGroup.
	 * @param elementGroup
	 * @return
	 */
	private ElementGroup createElementGroup(ElementGroup elementGroup) {
		ElementGroup result = new ElementGroup();
		List<Element> elements = elementGroup.getElements();
		for (Element element : elements) {
			result.addElement(element);
		}
		return result;
	}
	
	/**
	 * Generates Equals expression that will be used to create a Filter to "bind" variables.
	 * The expression has the following form: '?variable = value', where the variable is
	 * given by varName parameter and the value is got from the outerInstance result. 
	 * @param leftMetadata Metadata from outer instance.
	 * @param leftInstance Tuple from outer instance.
	 * @param varName Variable name.
	 * @return Equals Expression.
	 */
	private Expr generateEqualsExpresssion(Metadata leftMetadata, Tuple leftInstance, String varName) {
    	// Variable value extracted from left side result
		int varPosition = leftMetadata.getDataOrder(varName);
    	Type value = leftInstance.getData(varPosition);
    	Expr expr = Convert.QefType2JenaExpr(value);
        expr = new E_Equals(new ExprVar(varName), expr);
        return expr;
	}
	
	/**
	 * Creates the joined instance: Inner values + outer values minus shared variables.
	 * @param leftInstance
	 * @param rightInstance
	 * @return
	 */
	public Tuple join(Tuple leftInstance, Tuple rightInstance) {
    	Tuple instance = (Tuple) leftInstance.clone();
    	Iterator<Type> rightTupleValues = ((Tuple)rightInstance).getValues().iterator();
    	
    	for (int i=0; rightTupleValues.hasNext(); i++) {
    		Type value = rightTupleValues.next();
    		if (! getRightSharedVarsPositions().contains(i)) {
    			((Tuple)instance).addData(value);
    		}
    	}
    	return instance;
	}	
	
    /**
	 *  Finds inner metadata shared variable positions compared with the outer metadata variables.
     */
    private void findSharedVarsPositions() {
    	Iterator<Data> rightData = this.rightMetadata.getData().iterator();
		for (int i=0; rightData.hasNext(); i++) {
			String innerVarName = rightData.next().getName();
			Iterator<Data> leftData = this.leftMetadata.getData().iterator();
	    	for (int j=0; leftData.hasNext(); j++) {
	    		String leftVarName = leftData.next().getName();
    			if (leftVarName.equals(innerVarName)) {
    				this.rightSharedVarsPositions.add(i);
    				this.leftSharedVarsPositions.add(j);
    			}
	    	}
		}
    }
    
    /**
     * Gets a string with tuple values defined by keyPositions separed by a delimiter (;).
     * @param tuple
     * @param keyPositions
     * @return
     */
    public String getKey(Tuple tuple, List<Integer> keyPositions) {
    	StringBuilder sb = new StringBuilder();
    	for (int i=0; i < keyPositions.size(); i++) {
    		sb.append(tuple.getData(keyPositions.get(i)));
    		if (i < keyPositions.size() - 1) {
    			sb.append(';');
    		}
    	}
    	return sb.toString();
    }
    
//	/**
//	 * Changes inner query to include bound variables got from the outer instances.
//	 * @throws Exception
//	 */
//	public void reformulateInnerQuery(List<Tuple> outerInstanceList, SparqlEndpointDataSource ds, Query originalInnerQuery) throws Exception {
//        // Gets a changed query with bound variables.
//		Query innerQuery = bindVariables(originalInnerQuery, outerInstanceList);
//        ds.setQuery(innerQuery);
//        ds.open(); // Opens the changed query (with the generated filter)
//        ds.setQuery(originalInnerQuery); // Changes to the original query.
//	}    

	public List<Integer> getLeftSharedVarsPositions() {
		return this.leftSharedVarsPositions;
	}
	
	public List<Integer> getRightSharedVarsPositions() {
		return this.rightSharedVarsPositions;
	}

	public Metadata getLeftMetadata() {
		return this.leftMetadata;
	}

	public Metadata getRightMetadata() {
		return this.rightMetadata;
	}

}
