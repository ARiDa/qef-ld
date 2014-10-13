package ch.epfl.codimsd.qeef.sparql.operator;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qep.OpNode;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class SetBindJoin extends BindJoin {

	final static Logger logger = LoggerFactory.getLogger(SetBindJoin.class);
	
	/**
	 * Used when threads are enabled to specify that there is no more results.
	 */
	private static Tuple END_TOKEN = new Tuple();

	/**
	 * Defines the left tuples set size.
	 * It is also tells how many left tuples will be used in the right query.
	 */
	private int leftTuplesSetSize = 10; // Default value
	
	
	/**
	 * Defines the time used to get an left tuples set.
	 * If time is greater than zero, setSize will not be used.
	 */
	private long leftTuplesSetTime;
	
	/**
	 * Buffer containing the resulting join tuples.
	 */
	private BlockingQueue<Tuple> resultBuffer;
	
	/**
	 * Counts the number of left producer sets that are still being processed. 
	 * As soon as the set is completely processed, this counter is decremented.
	 */
	private int activeLeftProducerSetCounter;

	private int maxActiveLeftProducerSetCounter;
	
	/**
	 * 
	 */
	private boolean processStarted;
	

	private Tuple lastLeftTuple;
	
	/** Maximum of active (simultaneous) threads 
	 * < 0 - unlimited.
	 * 0 - do not use threads at all.
	 * > 0 - do not use more simultaneous threads than this.
	 * */
	private int maxActiveThreads;


	
	/**
	 * 
	 * @param id
	 * @param op
	 */
    public SetBindJoin(int id, OpNode op) {
        super(id, op);
        
        String[] params = op.getParams();
        
        // Optional parameters: maxActiveThreads and setSize
        if (params != null) {
        	if (params.length > 0) {
        		this.maxActiveThreads = Integer.parseInt(params[0]);
        		System.out.println("maxActiveThreads:" + this.maxActiveThreads);
        	}
        	if (params.length > 1) {
        		this.leftTuplesSetSize = Integer.parseInt(params[1]);
        	}
        	
        }
    	
        logger.info("SetBindJoin - maxActiveThreads {} - SetSize: {}", 
        		this.maxActiveThreads, this.leftTuplesSetSize);
    } 
    

    @Override
    public void open() throws Exception {
    	super.open();
    	setActiveLeftProducerSetCounter(0);
		this.processStarted = false;
		this.resultBuffer = new LinkedBlockingQueue<Tuple>();
    }
    
    /**
     * 
     */
    public synchronized void incrementActiveSetCounter() {
    	setActiveLeftProducerSetCounter(this.activeLeftProducerSetCounter + 1);
    }
    
    /**
     * @throws InterruptedException 
     * 
     */
    public synchronized void decrementActiveSetCounter() throws InterruptedException {
    	setActiveLeftProducerSetCounter(this.activeLeftProducerSetCounter - 1);
		if (this.getLastLeftTuple() == null && getActiveLeftProducerSetCounter() == 0) {
			this.resultBuffer.add(END_TOKEN);
		}
    }

    /**
     * Obtem uma instância que é resultado da junção de uma tupla da relação
     * right com uma tupla da relação left.
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
    	// Fills leftTupleSet (Buffer) if it is empty 
    	if (! this.processStarted) {
    		this.processStarted = true;
    		if (this.maxActiveThreads != 0) {
    			Thread thread = new Thread( new Runnable() {
					public void run() {
						try {
							processTuples();
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
				    		System.out.println("maxActiveLeftProducerSetCounter:" + maxActiveLeftProducerSetCounter);
						}
					}
				});
    			thread.start();
    		} else {
    			processTuples();
    		}
    	}

    	this.instance = this.resultBuffer.take();
		if (this.instance == END_TOKEN) {
    		this.instance = null;
    		System.out.println("maxActiveLeftProducerSetCounter:" + maxActiveLeftProducerSetCounter);
		} else {
            produced++;
		}
        return instance;
    }

    /**
     * Fills a buffer (tupleList) of left tuples.
     * @throws Exception
     */
    private void processTuples() throws Exception {
    	long startTime = System.currentTimeMillis();
    	Tuple leftTuple = (Tuple)getProducer(LEFT).getNext(id);
    	setLastLeftTuple(leftTuple);
    	
    	while (leftTuple != null) {
    		// Map will be used to join the results
			Map<String, List<Tuple>> leftTuplesMap = new Hashtable<String, List<Tuple>>();

			// if setSize < 1 all left tuples will be used in the right query
			int numberOfLeftTuples=0;
			
			while (true) {
	    		// Defines the set size based on time or number of left results.
	    		if (this.leftTuplesSetTime > 0) {
	    			if (System.currentTimeMillis() - startTime > this.leftTuplesSetTime) {
	    				break;
	    			}
	    		} else if ( (this.leftTuplesSetSize > 0 && numberOfLeftTuples >= this.leftTuplesSetSize) || leftTuple == null ) {
	    			break;
	    		}
	    		String key = this.joinQueryManipulation.getKey(leftTuple, this.joinQueryManipulation.getLeftSharedVarsPositions());
	    		
	    		// There is one list for each key
	    		List<Tuple> leftList = leftTuplesMap.get(key);
	    		if (leftList == null) {
	    			leftList = new Vector<Tuple>();
	    			leftTuplesMap.put(key, leftList);
	    		}
	    		leftList.add(leftTuple);
	    		
	    		leftTuple = (Tuple)getProducer(LEFT).getNext(id);
	        	setLastLeftTuple(leftTuple);
	    		numberOfLeftTuples++;
	    	}
//	    	logger.info("SetBindJoin set time: {}", (System.currentTimeMillis() - startTime));
	    	
	    	// Use threads here. One thread for each leftTupleList.
	    	SetBindJoinResultProcessor setBindJoinResultBuffer = new SetBindJoinResultProcessor(this, leftTuplesMap, startTime);
	    	if (this.maxActiveThreads != 0) {
	    		while (this.maxActiveThreads > 0 && getActiveLeftProducerSetCounter() >= this.maxActiveThreads) {
	    			Thread.sleep(20);
	    		}
	    		Thread thread = new Thread(setBindJoinResultBuffer);
	    		thread.start();
	    	} else {
	    		setBindJoinResultBuffer.fillResultBuffer(leftTuplesMap);
	    	}
	    	startTime = System.currentTimeMillis();
		}
    }

	public BlockingQueue<Tuple> getResultBuffer() {
		return resultBuffer;
	}
	
	
	
	@Override
	public void close() throws Exception {
		super.close();
		this.resultBuffer = null;
	}


	public synchronized Tuple getLastLeftTuple() {
		return lastLeftTuple;
	}


	public synchronized void setLastLeftTuple(Tuple lastLeftTuple) {
		this.lastLeftTuple = lastLeftTuple;
	}


	public int getActiveLeftProducerSetCounter() {
		return activeLeftProducerSetCounter;
	}


	public void setActiveLeftProducerSetCounter(int activeLeftProducerSetCounter) {
		this.activeLeftProducerSetCounter = activeLeftProducerSetCounter;
		if (this.activeLeftProducerSetCounter > this.maxActiveLeftProducerSetCounter) {
			this.maxActiveLeftProducerSetCounter = activeLeftProducerSetCounter;
		}
	}


	public int getMaxActiveLeftProducerSetCounter() {
		return maxActiveLeftProducerSetCounter;
	}

}
