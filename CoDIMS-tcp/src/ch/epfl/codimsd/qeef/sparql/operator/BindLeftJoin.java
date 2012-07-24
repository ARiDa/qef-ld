package ch.epfl.codimsd.qeef.sparql.operator;

import ch.epfl.codimsd.qep.OpNode;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class BindLeftJoin extends BindJoin {

    /**
     * Constructor
     * @param id
     * @param op
     */
	public BindLeftJoin(int id, OpNode op) {
        super(id, op);
        this.leftJoin = true;
    }
	
}
