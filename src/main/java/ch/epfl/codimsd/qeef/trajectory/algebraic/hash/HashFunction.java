package ch.epfl.codimsd.qeef.trajectory.algebraic.hash;

import org.apache.log4j.Logger;
import ch.epfl.codimsd.qeef.DataUnit;

/**
 * Interface de uma fun��o de hash a ser utilizada pelo algortmo de hash join durante o particionamento dos dados.
 * 
 * @author Vinicius Fontes
 */

public abstract class HashFunction {
	
    /**
     * Componente de Log utilizado.
     */
    protected Logger logger;
    
    /**
     * Contrutor padr�o.
     */
	public HashFunction(){
	    
		super();
		
		logger = Logger.getLogger(HashFunction.class.getName());
	}
	
	/**
	 * Determina em quais os buckets esta inst�ncia deve ficar. 
	 * 
	 * @param instance Instancia a ser particionada.
	 */
	public abstract int[] assign(DataUnit instance) throws Exception;
	 
}
