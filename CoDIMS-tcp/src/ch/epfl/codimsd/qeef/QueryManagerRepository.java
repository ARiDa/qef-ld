package ch.epfl.codimsd.qeef;

import java.util.Hashtable;
import java.util.Map;

import ch.epfl.codimsd.exceptions.CodimsException;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.query.Request;
import ch.epfl.codimsd.query.RequestParameter;

 /**
  * Singleton QueryManager Repository.
  * It allows the reuse of QEPs. 
  * @author Regis Pires Magalhaes
  */
public class QueryManagerRepository {
	private Map<Integer, QueryManager> repository;
	private static QueryManagerRepository qepRepository;
	
	private QueryManagerRepository() {
		this.repository = new Hashtable<Integer, QueryManager>();
	}

	public static QueryManagerRepository getInstance() {
		if (qepRepository == null) {
			qepRepository = new QueryManagerRepository();
		}
		return qepRepository;
	}
	
	/**
	 * Gets the queryManager associated with an specific RequestType.
	 * Creates the QueryManager if it is not in the repository yet.
	 * @param requestType
	 * @return
	 * @throws CodimsException
	 */
	public QueryManager get(int requestType) throws CodimsException {
		return get(requestType, false);
	}
	
	/**
	 * Gets the queryManager associated with an specific RequestType.
	 * Creates the QueryManager if it is not in the repository yet.
	 * @param requestType RequestType. The requestType associated to the request. You should verify that the requestType
	 * is defined in codims constants, as well as in the catalog.
	 * @param reload Creates que request even if it is already in the repository.
	 * @return
	 * @throws CodimsException 
	 */
	public QueryManager get(int requestType, boolean reload) throws CodimsException {
		if (this.repository.get(requestType) == null || reload) {
			// Creates the request
			RequestParameter requestParameter = new RequestParameter();
			requestParameter.setParameter(Constants.LOG_EXECUTION_PROFILE, "TRUE");
			requestParameter.setParameter(Constants.NO_DISTRIBUTION, "FALSE");
			Request request = new Request(null, requestType, requestParameter, 1);
			
			// Creates the QEP and stores it in the repository
			this.repository.put(requestType, new QueryManager(request));
		}
		return this.repository.get(requestType);
	}

	
	/**
	 * Returns true if the plan is already stored in the repository (cache).
	 * @param requestType
	 * @return
	 */
	public boolean isLoaded(int requestType) {
		return (this.repository.get(requestType) != null);
	}
	
}
