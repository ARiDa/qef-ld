package ch.epfl.codimsd.qeef.sparql.operator;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.sparql.JoinQueryManipulation;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class SetBindJoinResultProcessor implements Runnable {
	/**
     * Log4j logger.
     */
	private static Logger logger = Logger.getLogger(SetBindJoinResultProcessor.class.getName());

	private SetBindJoin setBindJoin; 
	private Map<String, List<Tuple>> leftTuplesMap;
	private int producerId;
	private Operator rightProducer;
//	private long startTime;
//	private int numberOfResults; // used to calculate throughtput.
	
	/**
	 * 
	 * @param setBindJoin
	 * @param leftInstancesList
	 * @param leftTuplesMap
	 * @param startTime
	 * @throws Exception 
	 */
	public SetBindJoinResultProcessor(SetBindJoin setBindJoin, Map<String, List<Tuple>> leftTuplesMap, long startTime) throws Exception {
		this.setBindJoin = setBindJoin;
		this.leftTuplesMap = leftTuplesMap;
		this.producerId = this.setBindJoin.getId();
		
		// Changes the right producer to use bound variables.
		Map<String, Object> params = new Hashtable<String, Object>();
		params.put("joinQueryManipulation", this.setBindJoin.getJoinQueryManipulation());
        params.put("leftTuples", leftTuplesMap.values()); // Collection of lists
		this.rightProducer = this.setBindJoin.getProducer(SetBindJoin.RIGHT).cloneOperator(params);
//		this.startTime = startTime;
	}
	
	
	public void run() {
        try {
        	fillResultBuffer(this.leftTuplesMap);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
    		System.out.println("maxActiveLeftProducerSetCounter:" + this.setBindJoin.getMaxActiveLeftProducerSetCounter());
		}
	}
	
    /**
     * Fills the result buffer.
     * @param leftInstanceList
     * @param rightInstances
     * @throws Exception 
     */
    public void fillResultBuffer(Map<String, List<Tuple>> leftInstancesMap) throws Exception {
		this.setBindJoin.incrementActiveSetCounter();

		// changes right query and opens right producer
		// Note: easy to implement the right join from here, but not the left join.
    	Tuple rightTuple = (Tuple)this.rightProducer.getNext(this.producerId);
		while (rightTuple != null) {
			JoinQueryManipulation jqm = this.setBindJoin.getJoinQueryManipulation();
			String key = jqm.getKey(rightTuple, jqm.getRightSharedVarsPositions());
			List<Tuple> leftInstancesList = leftInstancesMap.get(key);
	    	Iterator<Tuple> leftIterator = leftInstancesList.iterator();
	    	while (leftIterator.hasNext()) {
	    		Tuple leftTuple = leftIterator.next();
        		Tuple tuple = this.setBindJoin.getJoinQueryManipulation().join(leftTuple, rightTuple);
        		this.setBindJoin.getResultBuffer().add(tuple);
//				this.numberOfResults++;
	    	}			
			rightTuple = (Tuple)this.rightProducer.getNext(this.producerId);
		}		
		
		this.setBindJoin.decrementActiveSetCounter();
//		double throughput = this.numberOfResults * 1000.0 / (System.currentTimeMillis() - this.startTime);
//		logger.info("Throughtput (results/s): " + throughput);
    }

}
