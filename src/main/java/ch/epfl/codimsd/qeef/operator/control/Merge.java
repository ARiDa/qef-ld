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
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.AsyncControlOperator;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.datastructure.Buffer;
import ch.epfl.codimsd.qep.OpNode;


/**
 * Operador respons�vel por unir o fluxo de execu��o plano de execu��o e manter taxas de produ��o para cada consumidor.
 * O objetivo � ter alguma informa��o sobre o desempenho de cada fragmento que possa ser utilizado pelo operador
 * Split na distribui��o dos dados. <p>
 * As informa��es sobre cada fragmento ser� disponibilizada no quador de comunica��o em uma tabela
 * indexada pelo id do consumidor. Seu nome de acesso � PROCESSING_RATES.<p>
 *
 * @author Vinicius Fontes.
 *
 * @see ch.epfl.codimsd.qeef.operator.control.Split
 */
public class Merge extends AsyncControlOperator {

	/**
	 * Log4 logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Merge.class.getName());

	private Vector<Vector<LogRate>> logLocalRate;

    /**
     * Construtor padr�o.
     *
     * @param id Identificador deste operador.
     * @param blackBoard Quadro de comunica��o utilizado pelos operadores deste plano.
     * @param capacity Capacidade do buffer utilizado no armazenamento dos dados produzidos.
     */
	public Merge(int id, OpNode opNode) {

        super(id, Integer.parseInt(opNode.getParams()[0]));
    }

    /**
     * Realiza o consumo de de dados de producer. A unidade de processamento
     * ser� uma inst�ncia Implementa��es devem consumir dados de seus produtor e
     * inseri-las no buffer para que possam ser consumidas futuramente.
     * Com a inutiliza��o do m�todo Thread#stop() do objeto thread, recomenda-se
     * que de tempos em tempos o procedimento verifique se o processo deve continuar existindo.
     * Nesta classe � definida a vari�vel continueProcessing que serve para este fim.
     * Ela possui valor true at� que a opera��o close seja executada.
     *
     * @param producer
     *            Produtor do qual os dados ser�o consumidos
     * @param buffer
     *            Buffer no qual as inst�ncias produzidas dever�o ser inseridas.
     *
     * @throws Exception
     *             Se algum erro acontecer na produ��o dos dados.
     */
    public void consume(Operator producer, Buffer buffer)
            throws Exception {

        /**
         * Vetor com informa��es dos fragmentos de processamento disponibilizada pelo Split.
         * Sua ordem deve ser a mesma que os produtores deste operador que por sua vez
         * � igual a ordem dos consumidores do split.
         */
    	BlackBoard bl = BlackBoard.getBlackBoard();
        Vector fragsInfo = (Vector) bl.get("SPLIT_FRAGMENTS_INFO");

        DataUnit next;
        Request request;
        FragmentInfo info;
        int aux, totRecebido;
        long instanceSize, start, end;
        LogRate currLog;
        Vector<LogRate> log;

        aux = producers.indexOf(producer);
        info = (FragmentInfo)fragsInfo.get(aux);
        instanceSize = metadata[0].instanceSize();

        //inicia componentes do log da taxa
        log = new Vector<LogRate>();
        totRecebido = 0;

        //Marca inicio para calcular taxa
        currLog = new LogRate();
        currLog.id = info.id;
        currLog.acc = totRecebido;
        currLog.time = System.currentTimeMillis();
        log.add(currLog);

        while(continueProcessing){

            //configura tamanho de bloco e tempo de espera
            request = info.defineMergeRequest(instanceSize);

            bl.put(id+"_BLOCK_SIZE", new Integer(request.blockSize));
            bl.put(id+"_WAIT_TIME", new Long(request.waitTime));

            start = System.currentTimeMillis();
            next = producer.getNext(id);

            end = System.currentTimeMillis();

            if(next == null){
            	logger.info("Merge(" + id + ") Thread Fragment " + info.id + " it received null and it was locked up .");
            	break;
            }

            totRecebido += next.size();
            currLog = new LogRate();
            currLog.id = info.id;
            currLog.acc = totRecebido;
            currLog.time = end;
            log.add(currLog);

            info.logReceived(request, next, start, end);

            buffer.add(next);
        }

//        if( info.sentInstances - info.receivedInstances > 0)
//        	logger.warn("MERGE(" + id + "): ERROR: Split send more tuples than Merge received. Difference " +  (info.sentInstances - info.receivedInstances) + " fragment id " + info.id);
//
        logLocalRate.add(log);
    }

    public void open() throws Exception {

    	super.open();

    	logLocalRate = new Vector<Vector<LogRate>>();
    }

    public void close() throws Exception{

    	super.close();

    	String log;
//    	logger.info("-----------------------------------------------------------------");
//    	logger.info("Datas for calculating cost in Merge operator");
//    	logger.info("-----------------------------------------------------------------");

    	Enumeration[] frgsEnun = new Enumeration[logLocalRate.size()];
    	boolean alguemTemMais = true;
    	for(int i=0; i < logLocalRate.size(); i++){
    		frgsEnun[i] = ((Vector)logLocalRate.get(i)).elements();
    	}

    	while( alguemTemMais ){
    		log = "TxLocal: ";
    		alguemTemMais = false;

	    	for(int i=0; i < logLocalRate.size(); i++){

	    		if( frgsEnun[i].hasMoreElements() ){
	    			alguemTemMais = true;
	    			 log += frgsEnun[i].nextElement();
	    		} else{
	    			 log += " ; id ; " + " ; received ; " + " ; Time ; " + " ; Cost ; "  ;
	    		}
	    	}
	    	 log = log + "\n";
	    	 // logger.info(log);
    	}
//    	 logger.info("-----------------------------------------------------------------");
//    	logger.info("Merge(" + id + "): closed.");
    }
}

class LogRate{

	int id;
	int acc;
	long time;

	public String toString() {

		return " ; id ; " + id + " ; received ; " + acc + " ; Time ; " + time + " ; Cost ; " ;
	}
}

