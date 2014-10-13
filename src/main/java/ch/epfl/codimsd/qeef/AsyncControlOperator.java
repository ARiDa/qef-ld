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
package ch.epfl.codimsd.qeef;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.datastructure.*;

/**
 * Classe abstrata que define o comportamento e as estruturas necess�rias a um
 * operador de controle assincrono. Este operador consome os dados de forma assincrona de
 * seus produtores. Uma thread de consumo � inicializada para cada operador qdo
 * de sua inicializa��o. Os dados produzidos s�o deixados em um buffer, de onde
 * ser�o consumidos futuramente por AsyncControlOperator#getNext(int).<p>
 * A etapa de open tamb�m � realizada de forma assincrona e depende de que um produtor
 * tenha a conclu�do para que o metadado deste operador possa ser obtido e se encerre a inicializa��o.<p>
 * 
 * Se alguma excess�o ocorrer durante o consumo dos dados pelas threads, essa excess�o ser� registrada e lan�ada
 * na pr�xima requisi��o por dados ao operador.
 * 
 * Changes made by Othman :
 * - Logger static parameter.
 * - Constructor changes, we're not passing the blackBoard anymore.
 * - deleted : int p = getProducers().size();
 * 
 * @author Vinicius Fontes, Othman Tajmouati. 
 */

public abstract class AsyncControlOperator extends Operator {

    /**
     * Tempo m�ximo que se espera at� que uma thread seja encerrada. Ap�s isso
     * verifica se alguma excess�o ocorreu e espera novamente se necess�rio.
     */
    protected static final int MAX_WAIT_TIME = 1000;

    /**
     * Determina se thread de consumo deve continuar consumindo tuplas de seu
     * produtor. Utilizada no lugar de Thread#stop() que foi deprecado.
     */
    protected boolean continueProcessing;

    /**
     * Estrutura utilizada para armazenar inst�ncias at� que elas sejam
     * consumidas.
     */
    protected Buffer buffer;

    /**
     * Thread que realiza o consumo de dados do produtor.
     */
    private ConsumerThread thrConsumers[];

    /**
     * N�mero m�ximo de tuplas que pode ser consumido de forma assincrona(define
     * tamanho do buffer).
     */
    protected int capacity;

    /**
     * Sem�foro utilizado para garantir que todos as threads foram encerradas.
     * S� depois poder� ser feito o encerramento dos demais produtores.
     * O uso de sem�foro � interessante em casos onde o processamento ainda n�o terminou 
     * e o encerramento � solicitado. Desta forma, esta thread n�o ocupa processamento e permite
     * que as threads de consumo terminem seu trabalho.
     */
    protected Semaphore closed;
    
    /**
     * Sem�foro utilizado para garantir que todos as threads foram encerradas.
     * S� depois poder� ser feito o encerramento dos demais produtores;
     */
    protected Semaphore opened;

    /**
     * Define se alguma thread foi terminada de maneira incorreta.
     */
    protected Exception abortReason;
    
    /**
     * Determina quantas threads j� terminaram.
     * Cada thread insere um null no buffer quando termina processamento.
     */
    private int receivedNulls;
    
    /**
     * Log4j logger
     */
    @SuppressWarnings("unused")
    private Logger  logger = Logger.getLogger(AsyncControlOperator.class.getName());

    /**
     * Construtor padr�o.
     * 
     * @param id
     *            Identificador deste operador.
     * @param blackBoard
     *            quadro de comunica��o utilizado pelos operadores de um plano.
     * @capacity N�mero m�ximo de tuplas que pode ser consumido de forma
     *           assincrona(define tamanho do buffer).
     */
    public AsyncControlOperator(int id, int capacity) {

        super(id);
        this.capacity = capacity;
    }

    /**
     * Inicializa produtores e come�a a consumir dados.
     * 
     * @throws Exception
     *             Se algum erro que impossibiliza a execu��o acontecer durante
     *             a inicializa��o.
     */
    public void open() throws Exception {
        
        hasNext = true;
        metadata = new Metadata[getProducers().size()];
        receivedNulls = 0;
        buffer = new Buffer(capacity);
        opened = new Semaphore(0);
        closed = new Semaphore(0);
        continueProcessing = true;

        thrConsumers = new ConsumerThread[getProducers().size()];

        for (int i = 0; i < thrConsumers.length; i++) {
            thrConsumers[i] = new ConsumerThread(getProducer(i), buffer, this);
            thrConsumers[i].setDaemon(true);
            thrConsumers[i].start();
        }
        
        //Garante que um operador terminou open e setou metadados
        opened.acquire();
        
        if(abortReason!= null)
            throw abortReason;
    }

    /**
     * Atribui os metadados deste operador.
     */
    public void setMetadata(Metadata prdMetadata[]){
        this.metadata[0] = (Metadata)prdMetadata[0].clone();
    }

    /**
     * Consome uma tupla deste produtor de forma assincrona. Se houver tuplas dispon�veis o resultado ser� imediato.
     * 
     * @param consumerId
     *            Identificador do consumidor deste operador que solicitou a
     *            opera��o.
     * 
     * @return Tupla consumida. Null se n�o existir mais tuplas.
     * 
     * @throws Exception
     *             Se acontecer algum erro durante a obten��o da tupla de seu
     *             produtor.
     */
    public DataUnit getNext(int consumerId) throws Exception {        
        
        DataUnit aux;

        if (abortReason != null)
            throw abortReason;
        
        aux = null;

        while (receivedNulls < thrConsumers.length){
            aux= (DataUnit)buffer.get();
            if(aux == null)
                receivedNulls++;
            else
                break;
        }

        return aux;
    }

    /**
     * Para de consumo de dados e encerra os produtores. Os recursos ocupados
     * ser�o liberados.
     * 
     * @throws Exception
     *             se acontecer algum durante a libera��o dos recursos.
     */
    public void close() throws Exception {

        continueProcessing = false;

        //Adquire nr Produtores locks ou seja todas as threads finalizaram.
        //Espera um tempo, se n�o conseguir verifica se acontecer alguma
        // excess�o.
        //Fica preso ate que todas as threads sejam finalizadas.
        for (int i = thrConsumers.length; i > 0;) {

            if (closed.acquire(MAX_WAIT_TIME)) {
                i--;
            } else {
                if (abortReason == null)
                    throw abortReason;
            }
        }

        super.close(); //operador
        buffer = null;
        thrConsumers = null;
    }

    /**
     * Realiza o consumo de de dados de producer. A unidade de processamento
     * ser� uma inst�ncia Implementa��es devem consumir dados de seus produtor e
     * inseri-las no buffer para que possam ser consumidas futuramente.
     * Os valores null, que indicam que o operador n�o possui mais tuplas, 
     * n�o devem ser inseridos no buffer. A implementa��o do operador
     * define outros meios para sinaliar o final de processamento.
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
    public abstract void consume(Operator producer, Buffer buffer)
            throws Exception;

    /**
     * Registra o motivo pelo qual uma thread foi abortada.
     */
    void abortThread(Exception exc) {
        abortReason = exc;
    }

    /**
     * Registra que uma thread de consumo terminou o processamento.
     */
    void closeThread() {
        closed.release();
    }    
}

