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

import ch.epfl.codimsd.qeef.datastructure.Buffer;

/**
 * Thread de consumo dos dados utilizada pelo AsyncControlOperator. 
 *
 * @author Vinicius Fontes, Othman Tajmouati.
 * 
 * @date Jun 23, 2005
 */
class ConsumerThread extends Thread {

    /**
     * Operador do qual esta thread realizar� o consumo dos dados.
     */
    private Operator producer;
    
    /**
     * Operador que implementa m�todos de consumo.
     */
    private AsyncControlOperator op;
    
    /**
     * Buffer em que os dados produzidos ser�o inseridos.
     */
    private Buffer buffer;
    
    /**
     * Log4j logger.
     */
    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ConsumerThread.class.getName());

    /**
     * Construtor padr�o.
     * @param producer Operador do qual tuplas ser�o consumidas.
     * @param buffer Buffer utilizado para armazenar as tuplas lidas.
     * @param op Operador que define o processo de leitura. 
     */
    ConsumerThread(Operator producer, Buffer buffer, AsyncControlOperator op) {    	
    	
        super("CAssi" + op.id + "-" + producer.id);
                 
        this.producer = producer;
        this.buffer = buffer;
        this.op = op;
    }
    
    /**
     * Inicializa a thread de consumo.<p>
     * Invoca m�todo da AsyncControlOperator#consume(Operator, Buffer), e registra o final de processamento 
     * inserindo uma tupla null no buffer. Eventualmente registra alguma exce��o ocorrida.
     */
    public void run(){
        
        Metadata prdMetadata[] = new Metadata[1];
        try{            
            //Inicializa o produtor e avisa operador concluiu esta etapa
            producer.open();            
            prdMetadata[0] = producer.getMetadata(op.getId());
            // System.out.println("Metadadas Received by Op Asynchrone " + prdMetadata[0]);
            op.setMetadata(prdMetadata);  
            op.opened.release();
            op.consume(producer, buffer);
            
        }catch(Exception exc){   
        	exc.printStackTrace();
            op.abortThread(exc);
            op.opened.release();
        }

        //Sinaliza que esta thread terminou
        // logger.info("CAssi" + op.id + "-" + producer.id + " closed.");
        buffer.add(null);                        
        op.closeThread();
    }
}

