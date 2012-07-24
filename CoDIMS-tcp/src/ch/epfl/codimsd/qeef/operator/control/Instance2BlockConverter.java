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

import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.AsyncControlOperator;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Block;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.datastructure.Buffer;
import ch.epfl.codimsd.qep.OpNode;


/**
 * Monta um bloco de inst�ncias a partir dos dados consumidos de seus
 * produtores.
 * <p>
 * Os dados s�o consumidos de seus produtores de forma assincrona e armazenados
 * em um buffer. Quando houver a requisi��es por blocos, um conjunto de tuplas
 * ser�o retiradas do buffer e colocadas em um novo bloco.
 * <p>
 * Dois parametros podem ser configurados na requisi��o de um bloco: o tamanho
 * desejado e um tempo limite para se aguardar caso as tuplas n�o estejam
 * dispon�veis. Esses parametros s�o obtido do quadro de comunica��o pelas
 * vari�veis idOP_Block_SIZE e idOP_WAIT_TIME, onde isOP deve ser substituido
 * pelo identificador do operador que est� fazendo a solicita��o.
 * 
 * @author Vinicius Fontes.
 */
public class Instance2BlockConverter extends AsyncControlOperator {

	/**
	 * Log4 logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Instance2BlockConverter.class.getName());
    
	/**
	 * Lista de pedidos
	 */
	private Hashtable requests;

	@SuppressWarnings("unused")
	private int timeouts;

	@SuppressWarnings("unused")
	private int possibleTimeouts; //timeout que poderia ter sidos gerados ao
								  // esperar pela primeira tupla

	/**
	 * Construtor padr�o.
	 * 
	 * @param id
	 *            Identificador do operador.
	 * @param blackBoard
	 *            Quadro de comunica��o utlizado pelo operadores do plano.
	 * @param capacity
	 *            Capacidade do buffer utilizado para armazenar os dados
	 *            produzidos.
	 */
	public Instance2BlockConverter(int id, OpNode opNode) {
		
		super(id, Integer.parseInt(opNode.getParams()[0]));
	}

	public void open() throws Exception {
		super.open();
		
		requests = new Hashtable();
		BlackBoard bl = BlackBoard.getBlackBoard();
		bl.put(id+"_REQUEST_LIST", requests);
		
		timeouts = 0;
		possibleTimeouts = 0;

	}

	/**
	 * Realiza o consumo de inst�ncias de um dado produtor e as insere no
	 * buffer.
	 * 
	 * @param producer
	 *            Produtor do qual os dados ser�o consumidos.
	 * @param buffer
	 *            Buffer no qual as inst�ncias produzidas dever�o ser inseridas.
	 * 
	 * @throws Exception
	 *             Se algum erro acontecer na obten��o dos dados de seu
	 *             produtor.
	 */
	public void consume(Operator producer, Buffer buffer) throws Exception {

		DataUnit next;

		this.consumed = 0;
		
		next = producer.getNext(id);
		
		while (next != null && continueProcessing) {
			this.consumed++;
			buffer.add(next);
			next = producer.getNext(id);
		}
	}

	/**
	 * Cria um bloco a partir das inst�ncias consumidas de seu produtor.
	 * Variaveis que devem estar no Quadro de Comunica��o: -idOP_BLOCK_SIZE
	 * -idOP_WAIT_TIME. <br>
	 * 
	 * Se o tamanho definido por idOP_BLOCK_SIZE for -1, ser� retornado null. Se
	 * 0, um bloco de tamanho 0. Se > 0, ser� retornado um bloco com pelo menos
	 * uma tupla e no m�ximo o tamanho definido. Se n�o houver tuplas
	 * suficientes para completar o bloco, o operador aguradar� por um per�odo
	 * de at� idOP_WAIT_TIME para conseguir as tuplas que faltarem. Entretanto,
	 * o operador poder� aguardar por um tempo indeterminado se n�o existir ao
	 * menos uma tupla para colocar no bloco. <br>
	 * 
	 * Se n�o houver mais tuplas a serem enviadas o bloco saira imcompleto ou
	 * vazio. O proximo bloco produzido ser� null.
	 * 
	 * @param consumerId
	 *            Identificador do consumidor deste operador que solicitou a
	 *            opera��o.
	 * 
	 * @return Um bloco de inst�ncias. Null se n�o existir mais inst�ncias para
	 *         montar o bloco ou o tamanho do bloco for -1.
	 * 
	 * @throws Exception
	 *             Se aconteceu algum erro durante a obten��o das inst�ncias de
	 *             algum de seus produtores.
	 */
	public DataUnit getNext(int consumerId) throws Exception {

		// Remove by Othman int blockSize
		long aux, totalWaitTime, totalTime;
		Request req;
		Block block;
		Instance instance;
		
		req = (Request)requests.get(new Integer(consumerId));
		totalTime = System.currentTimeMillis();
		
//		logger.debug("I2B received request from block."
//				+ " ; id ; " + consumerId
//				+ " ; Size " + req.blockSize
//				+ " ; Time " + req.waitTime );

		if (abortReason != null)
			throw abortReason;

		block = new Block();

		if (req.blockSize == 0)
			return block;
		if (req.blockSize < 0 || !hasNext)
			return null;

		//Aguarda ate que uma tupla pelo menos esteja disponivel
		//E registra tempo gasto
		totalWaitTime = 0;

		synchronized (buffer) {
			while (buffer.isEmpty() && abortReason == null) {
				aux = System.currentTimeMillis();
				buffer.wait();
				totalWaitTime += System.currentTimeMillis() - aux;
			}
			
//			if(totalWaitTime > req.waitTime){
//				logger.info("I2B: Tuples created timeout first; waited time more ; " + (totalWaitTime-req.waitTime));
//			}

			//Preenche o bloco com o nr de tuplas solicitadas
			//ou at� que tempo expire e bloco tenha pelo menos uma tupla
			while (block.size() < req.blockSize) {

				if (totalWaitTime >= req.waitTime && buffer.isEmpty()) {
//					logger.info("I2B: Timeout when creating block  ; nrTimeouts ; "
//							+ (++timeouts) + " ; received instances ; "
//							+ consumed + " ; Sol. size ; " + req.blockSize  + " ; size ; " + block.size() );
					break;
				} 

				if (abortReason != null) {
//					logger
//							.warn(
//									"I2B: Exception detected when trying to assemble a block.",
//									abortReason);
					throw abortReason;
				}

				//          Obtem 1 tupla para colocar no buffer
				instance = (Instance) buffer.get();

				//Verifica se �ltima tupla consumida do produtor foi null
				if (instance == null) {
					hasNext = false;
					buffer.add(null); //Pode ter outras threads presas (esperando tupla)
					break;
				}
				block.add(instance);

				while (buffer.isEmpty() && totalWaitTime < req.waitTime
						&& abortReason == null) {

					aux = System.currentTimeMillis();
					buffer.wait(req.waitTime - totalWaitTime);
                                        aux = System.currentTimeMillis() - aux;
					totalWaitTime += aux;
				}

			}
		}

		totalTime = System.currentTimeMillis() - totalTime;
//		logger.debug("I2B sent block  ;" 
//				+ "; id ; " + consumerId
//				+ "; Size ; " + block.size()
//				+ "; requested size  ; " + req.blockSize
//				+ "; MaxWaitTime ; " + req.waitTime
//				+ "; Time spent  ; " + totalTime);

		if (block.size() == 0) {
			return null;
		}
		else {
			return block;
		}
	}

	public void close() throws Exception {
		
		// logger.debug("closing operator i2b.");
		super.close();
		// logger.debug("I2B closed.");
	}
}

