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

import ch.epfl.codimsd.qeef.DataUnit;

import ch.epfl.codimsd.qeef.scheduler.G2NNode;

/**
 * Classe que mantem informa��es sobre cada fragmento.
 * 
 * @author Vinicius Fontes
 * 
 * Calculo do custo durante execucao
 * Comparar com modelo de custo inicial
 * 
 * OBS: Calculo do custo nao usa B determinado pela funcao getMinBlockSize por problemas de arredondamento.
 */
public class FragmentInfo extends G2NNode implements Cloneable {

	/**
	 *  
	 */
	public int sentBlocks;

	/**
	 *  
	 */
	public int sentInstances;

	/**
	 *  
	 */
	public int receivedBlocks;

	/**
	 *  
	 */
	public int receivedInstances;

	/**
	 * Tempo de envio do primeiro bloco.
	 */
	public long startTime;

	/**
	 * Tempo de serializacao e deserializacao de uma tupla
	 */
	public int msgProcessingTime;

	/**
	 * Taxa de transmissao assumida em bytes/seg.
	 */
	public long netTransmitionRate;

//	/**
//	 * Tamanho de uma msg soap com anexo em bytes.
//	 */
//	private static final long OVERHEAD = 1413;
	
	/**
	 * Sinaliza que fragmento j� foi finalizado
	 */
	boolean closed;
	
	/**
	 * Janela utilizada no calcula da taxa a ser utilizada pelo G2N
	 */
	RateWindow rateWindow;
	
	/**
	 * Tamanho da janela de taxas
	 */
	private static final int WINDOW_SIZE=2;
	
	/**
	 * Taxa Acumulada --> elapsedTime(ms)/nrInstanciasRecebidas
	 */
	private int accRate;
	
	
	/**
	 * Log4 logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(FragmentInfo.class.getName());
    

	/**
	 * 
	 * @param id
	 * @param rate
	 * @param msgProcessingTime
	 * @param netTransmitionRate
	 * @param nrInstances
	 * @param iterations
	 */
	public FragmentInfo(int id, int rate, int msgProcessingTime,
			long netTransmitionRate, int nrInstances) {	
		
		this(id, rate, new RateWindow(WINDOW_SIZE), msgProcessingTime, netTransmitionRate, nrInstances);
		
		rateWindow.insertRate(rate);
	}

	private FragmentInfo(int id, int rate, RateWindow rw, int msgProcessingTime,
			long netTransmitionRate, int nrInstances) {

		//Rate � inicializado com o valor default e depois vai sendo atualizado em
		//funcao da janela
		super(id, nrInstances, rate);

		this.msgProcessingTime = msgProcessingTime;
		this.netTransmitionRate = netTransmitionRate;

		this.sentBlocks = 0;
		this.sentInstances = 0;
		this.receivedBlocks = 0;
		this.receivedInstances = 0;

		this.startTime = 0;
		
		this.accRate = 0;
		this.rateWindow = rw;

	}
	
	/**
	 *  
	 */
	public void logReceived(Request req, DataUnit dtUnit, long start, long end) {

		//Registra recebimento bloco
		receivedBlocks++;
		receivedInstances += dtUnit.size();
		rateWindow.insertRate( (int)(end-start)/dtUnit.size() );
		
		//Registra Rate para utilizacao pelo G2N
		this.rate = rateWindow.getRate();

		//Calcula Taxa Acumulada
		long elapsedTime = (end - startTime) - 2 * receivedBlocks;
		accRate = (int) elapsedTime / receivedInstances;	

//		logger.debug("Fragment ID ; " + id + " ; Received Block ; Size ; "
//				+ dtUnit.size() + " ; asked for ; " + req.blockSize
//				+ " ; Aloc ; " + nrInstances
//				+ " ; Tx Acc ; " + accRate + " ; Tx Jan ; " + rateWindow.getRate() + 
//				" ; Tx Inst ; " + (end-start)/dtUnit.size());
	}

	/**
	 *  
	 */
	public void logSent(Request req, DataUnit dtUnit) {

		if (sentBlocks == 0)
			startTime = System.currentTimeMillis();

		sentBlocks++;
		sentInstances += dtUnit.size();

//		logger.debug("Frg ID ; " + id + " ; Sent block ; Size ; "
//				+ dtUnit.size() + " ; Size of received block ; " + req.blockSize);
	}

	public int getMinimunBlockSize() {
		return (int) Math.ceil((2.0 * msgProcessingTime) / rate);
	}
	
	public int getUsedBlock() {
		
		int minBlock;
		
		minBlock = getMinimunBlockSize();

              //  System.out.println("minBlock = " + minBlock + " nrInstances = " + nrInstances);
		
		if( nrInstances > 2*minBlock )
			return Math.round(nrInstances/2);
		else if( nrInstances >= minBlock )
			return minBlock;
		else
			return nrInstances;
	}

	/**
	 * 
	 * @return
	 */
	Request defineSplitRequest(long instanceSize) {

		Request request = new Request();
		int minBlockSize = getMinimunBlockSize();

             //   System.out.println("nrInstances = " + nrInstances + " minBlockSize = " + minBlockSize);
		//If the node don't have more instances to process close it.
		if (nrInstances == 0) {
			request.blockSize = -1;

		} else {

			//First Block is always equal to the number of instances
			//The others blocks is determided as follow:
			// - odd blocks -> Minimun Block Size
			// - pair blocks -> Nr of instances - minimum block size
			// Pay Attention in the case that nr instances < minimum block size
                    
			if (sentBlocks == 0) {
				request.blockSize = nrInstances;
                                request.waitTime = 0;

                        } else if (nrInstances <= minBlockSize) { //3 caso
                                request.blockSize = nrInstances;

                        } else if( nrInstances > 2*minBlockSize){ //Para nao dar distorcoes entre valores de b e q-b
                                request.blockSize = nrInstances/2;

			} else if (sentBlocks % 2 == 0) { // par
				request.blockSize = minBlockSize;

			} else { // impar
				request.blockSize = nrInstances - minBlockSize;
			}
		}

                System.out.println("BLOCKSIZE = " + request.blockSize + " rate = " + rate + " nrInstances = " + nrInstances + " minBlockSize = " + minBlockSize);
		if(sentBlocks != 0)
                    request.waitTime = request.blockSize * rate;
		
		request.id = id;

//		logger.debug("Frg ID ; " + id
//				+ " ; Prepared Req Split ; Size ; " + request.blockSize
//				+ " ; WaitTime ; " + request.waitTime + " ; Q ; " + nrInstances
//				+ " ; B ; " + minBlockSize + " ; BlockId ; " + sentBlocks);

		return request;
	}

	/**
	 * 
	 * @return
	 */
	Request defineMergeRequest(long instanceSize) {

		Request request = new Request();
		int minBlockSize = getMinimunBlockSize();

		//Nao vai mais processar e ja buscou todas enviadas
		if (nrInstances == 0 && (sentInstances - receivedInstances) <= 0) {
			request.blockSize = -1;

			//Nao vai mais processar e ainda tem tuplas a buscar no fragmento
		} else if (nrInstances == 0 && (sentInstances - receivedInstances) > 0) {
			request.blockSize = sentInstances - receivedInstances;

		} else if (nrInstances <= minBlockSize) { //3 caso
			request.blockSize = nrInstances;

		} else if( nrInstances > 2*minBlockSize){ //Para nao dar distorcoes entre valores de b e q-b			
			request.blockSize = nrInstances/2;
			
		} else if (receivedBlocks % 2 == 1) { // impar
			request.blockSize = minBlockSize;

		} else {// par

			request.blockSize = nrInstances - minBlockSize;
		}

		request.waitTime = request.blockSize * rate;

//		logger.debug("Frg ID ; " + id
//				+ " ; Prepared Req Merge de Size ; " + request.blockSize
//				+ " ; WaitTime ; " + request.waitTime + " ; Q ; " + nrInstances
//				+ " ; B ; " + minBlockSize + " ; BlockId ; " + receivedBlocks);

		return request;
	}

	/**
	 * Cost in milliseconds
	 */
	public float evaluationCost(int nrNode) {

		float cost = 0;
		float minBlock = (float)(2.0*msgProcessingTime)/rate;

		if (nrInstances == 0) {

			return 0;

		} else if (nrInstances >= 2 * minBlock) {

			cost = msgProcessingTime + nrInstances * rate;

		} else if (nrInstances < (2.0 * minBlock) && nrInstances > minBlock) {

			//float delta =  1 - (nrInstances-minBlock)/minBlock ;
			//A seguinte simplificacao ser� utilizada: B * taxa = (2*MSG / taxa) * taxa = 2*MSG
			//cost = msgProcessingTime + (2*msgProcessingTime + Math.round(2*msgProcessingTime*delta) );
			cost = 4*msgProcessingTime + (nrInstances-minBlock)/minBlock;

		} else { //nrInstances < getMinimunBlockSize()

			cost = 2 * msgProcessingTime + (nrInstances * rate);
		}
		
//		if (nrNode != -1) {
//		
//			double environmentInitializationTime =  nrNode * 0.63;
//			double constructingBlockTime = nrInstances * 0.00015;
//			int numberOfRequests = (int) Math.ceil((nrInstances / (nrNode * minBlock)));
//			double waitingTime = 0;
//			
//			for (; nrNode > 1; nrNode--)
//				waitingTime += constructingBlockTime * (nrNode-1);
//		
//			double overlappingWaitingTime = numberOfRequests * waitingTime;
//			int initializationTime = (int) (environmentInitializationTime + overlappingWaitingTime);
//		
//			//logger.debug("INIT time : " + initializationTime);
//			cost += initializationTime;
//		}
		
		return cost;
	}

	/**
	 * Qtas tuplas este no pode processar em time - nr Instancias que ele ja tem
	 * 
	 * @param time
	 *            tempo em ms
	 * @return tuplas que este no pode processar neste tempo Se
	 *         supportedInstances < 0, significa que com o nr de instancias que
	 *         ele ja possui se custo � maior que time. Se supportedInstances ==
	 *         0, pode cair em dois casos: 1-Ele ja tem algumas tuplas e se
	 *         receber mais uma seu custo ultrapassa time 2-Ele n�o tem nenhuma
	 *         tupla e n�o consegue processar nada com custo time
	 */
	public int ableToProcess(float time) {

		int Q;
		float minBlockSize =  (float)(2.0*msgProcessingTime)/rate;

		Q = (int)Math.floor( (time - msgProcessingTime) / rate );

		if (Q < 2.0 * minBlockSize) {//Caiu no 1 caso

//			A seguinte simplificacao foi utilizada: B * taxa = (2*MSG / taxa) * taxa = 2*MSG
//			Q = (time - 2 * msgProcessingTime - ((iterations - 1) * (minBlockSize
//					* rate + 2 * msgProcessingTime)))
//					/ rate;
			
//			Q = (time - msgProcessingTime - (2*msgProcessingTime + 2 * msgProcessingTime))
//					/ rate;
			if(time > 4*msgProcessingTime){
				Q = (int)Math.floor(2 * minBlockSize - 1);
			} else{
			//if (Q <= minBlockSize) { //Caiu no 2 caso

				//Caiu no 3 caso
				Q = (int)Math.floor( (time - 2 * msgProcessingTime) / rate );
			}
		}

		return Q - nrInstances;
	}

	public Object clone() {

		FragmentInfo aux = new FragmentInfo(id, rate, (RateWindow)rateWindow.clone(), msgProcessingTime,
				netTransmitionRate, nrInstances);

		aux.sentBlocks = sentBlocks;
		aux.receivedBlocks = receivedBlocks;

		aux.sentInstances = sentInstances;
		aux.receivedInstances = receivedInstances;

		aux.startTime = startTime;		
		aux.closed = closed;

		return aux;
	}

	public String toString() {
		return "Frg: ID " + id + " Taxa Acc " + accRate + " Taxa Inst " + rateWindow.getRate() + " Tuples Env "
				+ sentInstances + "Tuples Rec " + receivedInstances;
	}
	
}

