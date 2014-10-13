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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.DataUnit;
// import ch.epfl.codimsd.qeef.ExecutionProfileLogger;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.scheduler.G2N;
import ch.epfl.codimsd.qeef.scheduler.G2NNode;
// import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qep.OpNode;
import ch.epfl.codimsd.qeef.operator.control.PerformanceMonitor;

/**
 * Operador respons�vel por dividir o fluxo de execu��o entre fragmentos
 * paralelos de um mesmo plano de execu��o. Neste contexto um fragmento pode ser
 * um operador ou uma sequ�ncia de operadores, que estejam executando em uma
 * m�quina remota do grid. Os framentos de execu��o devem ser iguais, ou seja,
 * implementa paralelismo horizontal.
 * <p>
 * A divis�o do fluxo de execu��o � feita com base em informa��es de desempenho
 * de cada fragmento, que s�o capturadas pelo operador Merge. Este operador
 * funciona de forma sincrona. Ao receber uma requisi��o por dados determina o
 * tamanho do bloco e o tempo m�ximo a ser esperado para montar o bloco e
 * realiza uma requisi��o para seu consumidor.
 * <p>
 * Veja Instance2BlockConverter para informa��es sobre como estas informa��es
 * s�o definidas durante a requisi��o.
 * <p>
 *
 * Na implementa��o atual, o n�mero de unidades de dados processadas por cada
 * fragmento n�o � controlado, optou-se por deixar livre para ver como o sistema
 * se adapta. O tamanho do bloco � dado em fun��o do tamanho de bloco inicial e
 * a evolu��o do sistema (n�mero de inst�ncias que j� foram processadas).
 * <p>
 * J� o tempo m�ximo esperado � definido como sendo o tempo que se demoraria
 * para enviar n blocos unit�rios. E � dado por TempoEnvioUnit�rio X blockSize.
 *
 * @author Vinicius Fontes.
 *
 * @see ch.epfl.codimsd.qeef.operator.control.Merge
 * @see ch.epfl.codimsd.qeef.operator.control.Instance2BlockConverter
 */
public class Split extends Operator {

	/**
	 * Tempo de serializacao e deserializacao de uma tupla em ms
	 */
	private static final int MSG_PROCESSING_TIME = 2500;

	/**
	 * Taxa de transmissao assumida em bytes/seg.
	 */
	private static final long TRANSMITION_RATE = 512000;

	/**
	 * Define se este operador j� foi inicializado por um de seus consumidores
	 * (fragmentos remotos).
	 */
	private boolean opened;

	/**
	 * Define se este operador j� foi encerrado por um de seus consumidores
	 * (fragmentos remotos).
	 */
	private boolean closed;

	public boolean doContinue;
	/**
	 * Tabela indexada pelo id do operador que contem informa��es sobre
	 * desempenho e tamanho de bloco utilizado.
	 */
	private Hashtable<Integer, FragmentInfo> consumerInfo;

	/**
	 * Nr de tuplas processadas por cada frogmento.
	 */
	private int procTuples[];

	/**
	 * Taxa dos fragmentos em milisegundos por tupla.
	 */
	private int prodRate[];

	/**
	 * Log4 logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Split.class.getName());

	/**
	 * Nr de iteracoes.
	 */
	protected int nrIteration;

	/**
	 * Tamanho de uma instancia em bytes;
	 */
	protected long instanceSize;

	private Hashtable<Integer, Request> blockRequests;

	protected float expectedCost;

	protected static final float PERFORMANCE_VARIANCE = (float)0.15; //menor que um

	protected static final float SURPLUS_VARIANCE = (float)1;

	protected static final long PERFORMANCE_CHECK_INTERVAL = 30000; //Tempo em ms

	protected int initialAvaiableInstances; //nr de instancias disponiveis inicialmente

	/**
	 * Thread  que monitora a performance do sistema.
	 */
	private PerformanceMonitor monitor;

	/**
	 * Construtor padr�o.
	 *
	 * @param id
	 * @param blackBoard
	 *            Quadro de comunica��o utilizado pelos operadores deste plano.
	 * @param procTuples
	 *            Lista com nr de tuplas processadas por cada fragmento
	 * @param nrIteration
	 *            Nr de iteracoes do eddy.
	 */
	public Split(int id, OpNode opNode) {

		super(id);

		BlackBoard bl = BlackBoard.getBlackBoard();

		String stringNrTuplesBL = (String) bl.get("NR_TUPLES");
		String stringProdRateBL = (String) bl.get("PROD_RATES");

		String[] stringNrTuples = stringNrTuplesBL.split(",");
		String[] stringProdRate = stringProdRateBL.split(",");

		int[] nrTuples = new int[stringNrTuples.length];
		int[] prodRate = new int[stringProdRate.length];

		for (int i = 0; i < stringNrTuples.length ; i++) {

			nrTuples[i] = Integer.parseInt(stringNrTuples[i]);
			prodRate[i] = Integer.parseInt(stringProdRate[i]);
		}

		this.opened = false;
		this.closed = false;
		this.procTuples = nrTuples;
		this.prodRate = prodRate;

		initialAvaiableInstances = 0;

		for(int i=0; i < prodRate.length; i++){
			initialAvaiableInstances += nrTuples[i];
		}
	}

	/**
	 * Inicializa este operador e seuu produtor.
	 *
	 * @throws Exception
	 *             Se alguma excess�o ocorrer durante a inicializa��o de seu
	 *             produtor.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void open() throws Exception {

		if (opened) {
			return;
		}

		if (procTuples.length != consumers.size())
			throw new Exception(
					"Nunber of consumers different from the number of Blocks Size");

		this.opened = true;
		this.hasNext = true;

		super.open();

		// Define o tamanho das instancias processadas
		instanceSize = metadata[0].instanceSize();

		// Controi Hash indexada pelo id do operador que guardara informacoes
		// dinamicas sobre cada consumidor
		FragmentInfo aux;
		Vector<FragmentInfo> fragsInfo;

		fragsInfo = new Vector<FragmentInfo>();
		consumerInfo = new Hashtable<Integer, FragmentInfo>();

		for (int i = 0; i < procTuples.length; i++) {

			Operator op = getConsumer(i);
			aux = new FragmentInfo(op.getId(), prodRate[i], MSG_PROCESSING_TIME,
					TRANSMITION_RATE, procTuples[i]);
			consumerInfo.put(new Integer(aux.id), aux);
			fragsInfo.add(aux);
		}

		BlackBoard bl = BlackBoard.getBlackBoard();
		bl.put("SPLIT_FRAGMENTS_INFO", fragsInfo);
		blockRequests = (Hashtable)bl.get(getProducer(0).getId() + "_REQUEST_LIST");
		bl.put(id + "_REQUEST_LIST", blockRequests);

		expectedCost = findMaxCost();

		monitor = new PerformanceMonitor(this, Split.PERFORMANCE_CHECK_INTERVAL);
		monitor.start();

		// logger.debug("Op(" + id + "): End opening split");

		// Initialize the ExecutionProfileLogger and log into the InitialExecutionProfile.s
//		log = false;
//	    if (bl.containsKey(Constants.LOG_EXECUTION_PROFILE)) {
//	    	String value = (String) bl.get(Constants.LOG_EXECUTION_PROFILE);
//	    	if (value.equalsIgnoreCase("TRUE")) {
//	    		log = true;
//		    	executionProfileLogger = new ExecutionProfileLogger();
//		    	executionProfileLogger.initialLog();
//	    	}
//	    }

	    doContinue = true;
	}

	public void setMetadata(Metadata prdMetadata[]) {
		this.metadata[0] = (Metadata) prdMetadata[0].clone();
	}

	/**
	 * Retorna o formato das tuplas produzidas por este operador para seus
	 * consumidores(igual para todos).
	 *
	 * @param idConsumer
	 *            Identificador do consumidor que est� solicitando o metadado.
	 *
	 * @return Metadados das tuplas produzidas por este operador.
	 *
	 */
	public Metadata getMetadata(int idConsumer) {
		return metadata[0];
	}

	/**
	 * Encerra este operador e seus produtor. Libera os recursos ocupados.
	 *
	 * @throws Exception
	 *             Se acontecer algum problema durante o encerramento dos seu
	 *             produtor.
	 */
	public synchronized void close() throws Exception {

		if (closed)
			return;

		closed = true;
		super.close();

		synchronized (monitor) {
			doContinue = false;
			monitor.notify();
		}

		consumerInfo = null;
		logger = null;
		blockRequests = null;
	}

	/**
	 * Realiza a requisi��o por um bloco de dados. Este operador realizar� a
	 * requisi��o por um bloco de dados a seu produtor com tamanho definido por
	 * (tamanho inicial definido X evolu��o do sistema) e tempo de espera como
	 * TempoEnvioBlocoUnit�rio X TamanhoBloco.
	 *
	 * @param consumerId
	 *            identificador do consumidor.
	 *
	 * @throws Se
	 *             alguma exce��o acontecer durante o consumo de dados de seu
	 *             produtor.
	 */
	public DataUnit getNext(int consumerId) throws Exception {

		Request request; // Tamanho do bloco a ser utilizado nesta requisicao
		FragmentInfo consInfo; // informacoes deste produtor

		// Obtem informacoes sobre este produtor
		consInfo = (FragmentInfo) consumerInfo.get(new Integer(consumerId));

		//Verifica se deve executar G2N
		//Baseado na sobra
		if(consInfo.nrInstances > 0 && !isSurplusSufficient()){
                       // System.out.println("Passei por Surplus");
			callG2N();
		}

		request = consInfo.defineSplitRequest(instanceSize);

		if( request.blockSize > 0){
			// Seta parametros
			Integer aux = new Integer(consumerId);
			blockRequests.remove(aux);
			blockRequests.put(aux, request);
			//faz requisicao
			//Nao utiliza super pq deve utilizar id do seu consumidor para
			//fazer a requisicao
                        if(!hasNext)
                            instance = null;
                        instance = getProducer(0).getNext(consumerId);

                        // Log the execution profile
        //	        if (log == true) {
        //
        //	        	if (instance != null) {
        //	        		executionProfileLogger.log(request);
        //	        	} else if (produced != 0 && hasNext == true){
        //	        		executionProfileLogger.printLastLog();
        //	        	}
        //	        }

                        if (instance!= null)
                                produced ++;
                        else
                                hasNext = false;
                        
		} else{
			instance = null;
		}

		if(instance!= null){
			consumed += instance.size();
			consInfo.logSent(request, instance);
		} else {
			// logger.info("Split(" + id + ") Fragmento " + consInfo.id + " will be finished.");
		}

		return instance;
	}


	private float findMaxCost() {

		float max = -1;
		BlackBoard bl = BlackBoard.getBlackBoard();
		Vector frgsInfo = (Vector)bl.get("SPLIT_FRAGMENTS_INFO");

		float curr;

		for(int i=1; i < frgsInfo.size(); i++ ){

			curr = ((FragmentInfo)frgsInfo.get(i)).evaluationCost(-1);

			if( curr > max )
				max = curr;
		}

		return max;
	}

	private boolean isSurplusSufficient(){

		int B, avaiableInstances;
		Enumeration it;
		FragmentInfo aux;
		B = 0;

              //  System.out.println("initialAvaiableInstances = " + initialAvaiableInstances);
		avaiableInstances = (int)Math.round(initialAvaiableInstances * 1);
		it = consumerInfo.elements();

		while ( it.hasMoreElements() ){
			aux = (FragmentInfo)it.nextElement();
                       // System.out.println("B1 = " + B);
			B += aux.getUsedBlock();
		}

		B = (int)Math.floor(B + B * SURPLUS_VARIANCE);

               // System.out.println("AvaiableInstances = " + avaiableInstances + "B = " + B);
		if( avaiableInstances > B){
			return true;
		} else {
			// logger.info("INEFFICIENT SURPLUS, Hour to execute G2N� B*Delta  " + B + " Q " + avaiableInstances );
			return false;
		}
	}

	public synchronized boolean isPerformanceAcceptable(){

		float newCost, dif;

		newCost = findMaxCost();
		dif = Math.abs(newCost - expectedCost);

		// logger.info("It analyzes Performance: Current cost  ; " + newCost + "; Expected Cost ; " + expectedCost + " ; Dif ; " + dif*1.0/expectedCost + " ; Delta ; " + PERFORMANCE_VARIANCE);

		if( dif*1.0/expectedCost > PERFORMANCE_VARIANCE )   {
			// logger.info("Performance Is of the Padrao.");
			return false;
		} else {
			return true;
		}
	}

	public void callG2N() {


		G2N g2n;
		@SuppressWarnings("unused")
		int avaiableInstances, toDoIterations, toProcess;
		G2NNode selNodes[], aux;
		// FragmentInfo currInfo;
		Vector<G2NNode> newInfo;
		Vector frgs;

		//frgs = (Vector)blackBoard.get("SPLIT_FRAGMENTS_INFO");
		BlackBoard bl = BlackBoard.getBlackBoard();
		frgs = (Vector)bl.get("SPLIT_FRAGMENTS_INFO");

		//discover the number of instances avaiable in the system
		//avaiableInstances = (int)Math.ceil(initialAvaiableInstances * ((Float) blackBoard.get("EVOLUTION")).floatValue());
		avaiableInstances = (int)Math.ceil(initialAvaiableInstances * 1);
		//avaiableInstances = (int)Math.ceil(initialAvaiableInstances * ((Float) bl.get("EVOLUTION")).floatValue());

		//Discover the number of iterations to do
		toProcess = (initialAvaiableInstances*nrIteration) - consumed;
		toDoIterations = (int)Math.ceil( toProcess / (avaiableInstances*1.0) );

		//prepares a copy of the informations about the fragment
		newInfo = new Vector<G2NNode>();
		for(int i=0; i < frgs.size(); i++ ){
			if( !((FragmentInfo)frgs.get(i)).closed ){
				aux = (FragmentInfo)((FragmentInfo)frgs.get(i)).clone();
				newInfo.add(aux);
			}
		}

		//call g2n
		g2n = new G2N();
		selNodes = g2n.schedule(newInfo, avaiableInstances);

		//Update the fragments information
		int tot=0;
		for(int i=0; i < selNodes.length; i++){

			aux = (FragmentInfo)consumerInfo.get( new Integer(selNodes[i].id) );
			aux.nrInstances = selNodes[i].nrInstances;
			if(aux.nrInstances == 0)
				((FragmentInfo)aux).closed = true;
			tot = tot + aux.nrInstances;
		}

		if(tot != avaiableInstances){
			// logger.warn("G2N ERROR --> Nr Instances sent to g2n " + avaiableInstances + " distributed " + tot);
		}

		//update expected cost
		expectedCost = findMaxCost();
	}

}

class PerformanceMonitor extends Thread {

	private Split split;
	private long time;

	PerformanceMonitor(Split split, long time){

		this.split = split;
		this.time = time;
	}

	public void run(){
		monitors();
	}

	private synchronized void monitors(){

		while(true) {

			try{
				wait(time);
			} catch(InterruptedException iExc){
				//is never the case
			}

			if (split.doContinue == false)
				return;

			if(!split.isPerformanceAcceptable()){
				// split.logger.info("Monitor executing G2N.");
				split.callG2N();
			}
		}
	}
}

