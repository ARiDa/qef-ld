package ch.epfl.codimsd.qeef.sparql.operator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qep.OpNode;

/**
 * Union Operator can use multiple producers.
 * Each producer can be processed in a different thread or sequencially.
 * @author Regis Pires Magalhaes
 *
 */
public class Union extends Operator {

	final static Logger logger = LoggerFactory.getLogger(Union.class);
	
	/**
	 * Used when threads are enabled to specify that there is no more results.
	 */
	private static Tuple END_TOKEN = new Tuple();
	
	/**
	 * 
	 */
	private boolean useThreads = true; // Default value
	
	/**
	 * Buffer containing the resulting union tuples.
	 */
	private BlockingQueue<Tuple> resultBuffer;
	
	/**
	 * Counts the number of producers in use. As soon as a producer does not have results, 
	 * this counter is decremented.
	 */
	private int producersCounter;
	
	/**
	 * 
	 */
	private boolean processStarted;

	/**
	 * 
	 * @param id
	 * @param op
	 */
    public Union(int id, OpNode op) {
        super(id, op);
        
        String[] params = op.getParams();
        
        // Optional parameter: useThreads
        if (params != null && params.length > 0) {
    		if (params[0].equalsIgnoreCase("false")) {
    			this.useThreads = false;
    		} else {
    			this.useThreads = true;
    		}
        }        
    	logger.info("Union - Threads {}", (useThreads ? "enabled" : "disabled"));
    }
    
    @Override
    public void open() throws Exception {
    	super.open();
    	resultBuffer = new LinkedBlockingQueue<Tuple>();
    	this.producersCounter = 0;
    	this.processStarted = false;
    }
    
    
    /**
     * 
     */
    public synchronized void incrementProducersCounter() {
    	this.producersCounter++;
    }
    
    /**
     * @throws InterruptedException 
     * 
     */
    public synchronized void decrementProducersCounter() throws InterruptedException {
    	this.producersCounter--;
		if (this.producersCounter == 0) {
			this.resultBuffer.put(END_TOKEN);
		}
    }
    
    /**
     * 
     * @return
     */
    public synchronized int getCounter() {
    	return this.producersCounter;
    }
    
    /**
     * Obtem uma instância que é resultado da junção de uma tupla da relação
     * inner com uma tupla da relação outer.
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
    	// Fills outerInstanceSet (Buffer) if it is empty 
    	if (! this.processStarted) {
    		this.processStarted = true;
    		for (int i=0; i < this.producers.size(); i++) {
    			if (this.useThreads) {
    				final Operator producer = this.producers.get(i);
    				Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
		    				try {
		    					incrementProducersCounter();
		    					process(producer);
		    					decrementProducersCounter();
		    				} catch (Exception e) {
		    					logger.error(e.getMessage(), e);
		    				}
						}
					});
    				t.start();
    			} else {
    				process(this.producers.get(i));
    			}
    		}
    		if (! this.useThreads) {
    			this.resultBuffer.put(END_TOKEN);
    		}
    	}

		this.instance = this.resultBuffer.take();
		if (this.instance == END_TOKEN) {
    		this.instance = null;
		} else {
            produced++;
		}
    	
        return instance;
    }

	public void process(Operator producer) throws Exception {
    	Tuple tuple = (Tuple) producer.getNext(this.id);
    	while (tuple != null) {
    		this.resultBuffer.put(tuple);
    		tuple = (Tuple) producer.getNext(this.id);
    	}
	}
    
	public BlockingQueue<Tuple> getResultBuffer() {
		return resultBuffer;
	}

    @Override
    public void setMetadata(Metadata[] prdMetadata) {
    	for (int i=1; i < prdMetadata.length; i++) {
        	if (! prdMetadata[0].equals(prdMetadata[i])) {
        		throw new IllegalArgumentException("Producers of union metadata must have the same metadata."); 
        	}
    	}
    	
    	this.metadata = new Metadata[1];
		this.metadata[0] = (Metadata) prdMetadata[0].clone();
    }

    
    @Override
    public void close() throws Exception {
    	super.close();
    	this.resultBuffer = null;
    }
    
}
