/*
* CoDIMS version 1.0 
* Copyright (C) 2006 Othman Tajmouati
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package ch.epfl.codimsd.qeef.operator.control;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Block;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.ExecutionProfileLogger;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.datastructure.Buffer;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qep.OpNode;


/**
 * Operador respons�vel por alterar a unidade de consumo em um fragmento de um plano de execu��o.
 * Os blocos ser�o consumidos de forma sincrona de seu produtor e as inst�ncias do bloco ser�o deixadas
 * em um buffer de onde ser�o consumidas. O consumo de novos blocos s� ser� realizado se o buffer de inst�ncias
 * estiver vazio.<p>
 * 
 * As inst�ncias de um bloco consumido s�o armazenadas em um buffer, e o consumo de um bloco s� ser� realizado quando
 * n�o houver inst�ncias no buffer no buffer. O processamento � realizado de forma sincrono, ou seja, o consumo de blocos 
 * s� ser� realizado quando houver requisi��o por inst�ncias. 
 *
 * Changes made by Othman :
 * - Translate javadoc to english
 * - Change constructor
 * - Add call to ExecutionProfileLogger in remote nodes
 * - Logger called in a static way
 *
 * @author Vinicius Fontes, Othman Tajmouati.
 */

public class Block2InstanceConverter extends Operator {

    /**
     * Estrutura utilizada para armazenar as inst�ncias de um bloco consumido.
     */
    private Buffer buffer;
    
    /**
	 * Log4 logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Block2InstanceConverter.class.getName());
    
    /**
     * Nr de tuplas em que foi realizado ultimo log
     */
    @SuppressWarnings("unused")
	private int lastLog;
    
    /**
     * Nr de tuplas consumidas
     */
    private int consumed;
    
    @SuppressWarnings("unused")
	private long startTime, lastTime;
    
    private static int logInterval = 1000;
    
    private int currInterval;
    
    /**
	 * Execution profile logger that logs inot a database.
	 */
	private ExecutionProfileLogger executionProfileLogger;
	
	/**
	 * Flag indicating if the operator should log the execution profile.
	 */
	private boolean logExecutionProfile = false;
	
	/**
	 * BlackBoard.
	 */
	private BlackBoard bl;
    
    /**
     * Default constructor.
     * 
     * @param id identifier of this operator.
     * @param opNode OpNode structure for this operator.
     */
    public Block2InstanceConverter(int id, OpNode opNode){
        super(id);
    }
    
    
    /**
     * Realiza a inicializa��o deste operador e de seus produtores.
     * 
     * @throws Exception
     *             Se algum erro que impossibilite a execu��o acontecer durante
     *             a inicializa��o dos demais produtores.
     */
    public void open() throws Exception {
        
    	super.open();
        buffer = new Buffer();

        startTime = System.currentTimeMillis();
        lastTime = System.currentTimeMillis();
        
        consumed = 0;
        lastLog = 0;
        currInterval = logInterval;
        
        // Initialize the ExecutionProfileLogger
		bl = BlackBoard.getBlackBoard();
	    
		if (bl.containsKey(Constants.LOG_EXECUTION_PROFILE)) {
	    	String value = (String) bl.get(Constants.LOG_EXECUTION_PROFILE);
	    	if (value.equalsIgnoreCase("TRUE")) {
		    	executionProfileLogger = new ExecutionProfileLogger();
		    	logExecutionProfile = true;
	    	}
	    }
    }
    
    /**
     * Define o formato/metadado das inst�ncias produzidas por este operador.
     * O formato ser� identico ao de seu produtor.
     * 
     * @param prdMetadata Formato das unidades de dados produzidas pelo seu produtor.
     */
    public void setMetadata(Metadata prdMetadata[]){
        this.metadata[0] = (Metadata)prdMetadata[0].clone();
    }
    
    /**
     * @param consumerId
     *            Identificador do consumidor que solicitou a opera��o.
     *  
     * @return Inst�ncia de um bloco ou Null se n�o existir mais inst�ncias a serem consumidas.
     * 
     * @throws Exception Se acontecer algum erro durante a obten��o do bloco de seu produtor.
     */
    public DataUnit getNext(int consumerId) throws Exception {
        
        Block block;
        Instance aux;
        
        if(!hasNext)
            return null;
            
        //Obtem proximo bloco ate receber null ou um bloco com pelo menos uma tupla
        while(buffer.isEmpty()){            
            block = (Block)getProducer(0).getNext(id);
            if(block != null){            	
            	consumed += block.size();
            	// logger.debug("B2I : Receiving block ; size ; " + block.size() + " ; Time : " + System.currentTimeMillis());
            }
            
            if( consumed >= currInterval ){
            	long currTime = System.currentTimeMillis();
            	// logger.info("Number of tuples produced by the system ; " + consumed + " ; Time : " + currTime + " ; Tax Acc ; " + ((System.currentTimeMillis()-startTime)/consumed)  + " ; Tax Inst ; " + (currTime-lastTime)*1.0/(consumed - lastLog));
            	lastLog = consumed;
            	lastTime = currTime;   
            	currInterval += logInterval;
            }
            disassembleBlock(block, buffer);            
        }
        
        aux = (Instance)buffer.get();
        
        if (aux == null) {
        
        	if (logExecutionProfile == true && bl.containsKey(Constants.THIS_NODE))
        		executionProfileLogger.printLastLogByTuple();
            
        	hasNext = false;
        
        } else {
        	// Log each tuple if this B2I operator is running in a remote node.
        	if (logExecutionProfile == true && bl.containsKey(Constants.THIS_NODE))
        		executionProfileLogger.logByTuple();
        }
        
        return aux;
    }
    
    /**
     * Retira as inst�ncias um de block e as insere no buffer.
     * 
     * @param block Bloco de onde ser�o retiradas as inst�ncias.
     * @param buffer Estrutura onde inst�ncias ser�o inseridas.
     */
    private void disassembleBlock(Block block, Buffer buffer){
        
        int blockSize ;
        Instance aux;        
        
        if(block == null) {
            buffer.add(null);
        } else{            
        	
        	blockSize = block.size();
        	while(blockSize>0){
	        	
        		aux = (Instance)block.get();
	            buffer.add(aux);
	            blockSize--;
	        }
        }
    }
    
    /**
     * Close the Block2InstanceConverter operator.
     * 
     * @throws Exception
     */
    public void close() throws Exception {

        super.close();
        buffer = null;
    }
}

