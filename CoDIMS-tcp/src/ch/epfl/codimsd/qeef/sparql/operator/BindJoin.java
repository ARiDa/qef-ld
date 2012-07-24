package ch.epfl.codimsd.qeef.sparql.operator;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.sparql.JoinQueryManipulation;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qep.OpNode;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class BindJoin extends Operator {

	/**
     * Log4j logger.
     */
	private static Logger logger = LoggerFactory.getLogger(BindJoin.class);
	
	/**
     * Constant used to reference the join LEFT producer.
     */
	protected static final int LEFT = 0;

    /**
     * Constant used to reference the join RIGHT producer.
     */
	protected static final int RIGHT = 1;

	/**
	 * 
	 */
	protected Operator rightProducer;
	
	/**
	 * 
	 */
	protected boolean emptyRightTuple = true;
	
	/**
	 * 
	 */
	protected Tuple leftTuple;
	
	/**
	 * 
	 */
	protected JoinQueryManipulation joinQueryManipulation;

	/**
	 * 
	 */
	protected boolean leftJoin = false;
	
    /**
     * Constructor
     * @param id
     * @param op
     */
	public BindJoin(int id, OpNode op) {
        super(id, op);
    }

    @Override
    public void open() throws Exception {
    	super.open();
//    	// Open only the first producer.
//        Operator leftProducer = (Operator) producers.get(LEFT);
//        leftProducer.open();
//        logger.debug("OP({}) open (left producer).", id);
//
//        // Get first producer metadata (left).
//        Metadata leftMetadata = (Metadata) leftProducer.getMetadata(id).clone();
//        
//    	// Get query from the right side of join
//        Operator rightProducer = (Operator) producers.get(RIGHT);
//        rightProducer.open();
//        logger.debug("OP({}) open (right producer).", id);
//
//        // Get second producer metadata (right).
//        Metadata rightMetadata = (Metadata) rightProducer.getMetadata(id).clone();
//        
//        // Defines the metadata of the join result
//        setMetadata(new Metadata[] { leftMetadata, rightMetadata });
        
    	this.emptyRightTuple = true;
    }

    /**
     * Obtem uma instância que é resultado da junção de uma tupla do produtor
     * right com uma tupla do produtor left.
     * 
     * @param consumerId
     *            Consumidor que está fazendo a requisição.
     * @throws IOException
     *             Se ocorrer algum problema durante a leitura de dados do
     *             bucket.
     * @throws OutOfMemoryError
     *             Se não houver memória suficiente para realizar a carga do
     *             bucket.
     */
    public DataUnit getNext(int consumerId) throws Exception {
        this.instance = applyMatch();
        
        if (instance != null) {
            produced++;
        }
        return instance;
    }    
    
	private DataUnit applyMatch() throws Exception {
        if (this.emptyRightTuple) {
            this.leftTuple = (Tuple)getProducer(LEFT).getNext(id);
            if (this.leftTuple == null) {
            	return null;
            }

            // Changes the right producer to use bound variables.
            List<Tuple> leftInstances = new ArrayList<Tuple>(1);
            leftInstances.add(this.leftTuple);

            Map<String, Object> params = new Hashtable<String, Object>(2);
            params.put("joinQueryManipulation", this.joinQueryManipulation);
            params.put("leftTuples", leftInstances);
            this.rightProducer = getProducer(RIGHT).cloneOperator(params);
        }

        DataUnit rightInstance = this.rightProducer.getNext(id);
        
        this.instance = (Tuple) leftTuple.clone();
        
        if (rightInstance == null) {
        	if (this.leftJoin && this.emptyRightTuple) {
                // if there is only the left side of the join then fill the right side with null values.
            	Iterator<Data> itData = this.joinQueryManipulation.getRightMetadata().getData().iterator();
            	
            	for (int i=0; itData.hasNext(); i++) {
            		itData.next();
            		if (! this.joinQueryManipulation.getRightSharedVarsPositions().contains(i)) {
            			((Tuple)this.instance).addData(null);
            		}
            	}
            	this.emptyRightTuple = true;
        	} else {
            	this.emptyRightTuple = true;
        		return applyMatch();
        	}
        	
        	
        } else {
            // Creates the joined instance: Right values + Left values minus shared variables.
        	Iterator<Type> tupleValues = ((Tuple)rightInstance).getValues().iterator();
        	
        	for (int i=0; tupleValues.hasNext(); i++) {
        		Type value = tupleValues.next();
        		if (! this.joinQueryManipulation.getRightSharedVarsPositions().contains(i)) {
        			((Tuple)this.instance).addData(value);
        		}
        	}
        	this.emptyRightTuple = false;
        }
    	
        return instance;
	}

	/**
     * Define o metadado das tuplas resultantes da junção. Metadado é dado por:
     * metadados left união metadados right, nesta ordem.
     * 
     * @param prdMetadata
     *            Metadados de seus produtores. Left e right respectivamente.
     */
    @Override
    public void setMetadata(Metadata[] prdMetadata) {
    	this.joinQueryManipulation = new JoinQueryManipulation(prdMetadata[LEFT], prdMetadata[RIGHT]);
    	
    	this.metadata = new Metadata[1];
    	this.metadata[0] = (Metadata) prdMetadata[LEFT];
    	Metadata rightMetadata = (Metadata) prdMetadata[RIGHT];
    	for (int i=0; i < rightMetadata.size(); i++) {
    		Data data = rightMetadata.getData(i);

    		// Adds right metadata information that is NOT SHARED with left metadata.
    		if (! joinQueryManipulation.getRightSharedVarsPositions().contains(i)) {
    			this.metadata[0].addData(data);
    		}
    	}
    }
    
    /**
     * 
     * @return
     */
    public JoinQueryManipulation getJoinQueryManipulation() {
    	if (this.joinQueryManipulation == null) {
    	}
		return this.joinQueryManipulation;
	}

    @Override
    public void close() throws Exception {
    	super.close();
    	this.rightProducer = null;
    	this.leftTuple = null;
    	this.joinQueryManipulation = null;
    }
    
}
