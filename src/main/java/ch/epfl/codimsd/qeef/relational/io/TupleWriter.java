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
package ch.epfl.codimsd.qeef.relational.io;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Block;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;

import ch.epfl.codimsd.qeef.io.InstanceWriter;
import ch.epfl.codimsd.qeef.relational.Tuple;

/**
 * digite aqui descricao do tipo
 *
 * @author Vinicius Fontes
 *
 * @date Jun 17, 2005
 */

/*
 * Formato de grava��o:
 *
 * (flag DataUnit)*flag
 *
 * Onde flag pode ser Flags:
 * '0' eof
 * '1' Uma unidade de dado null
 * '2' Existe mais uma unidade de dado
 */

public class TupleWriter implements InstanceWriter {

    /**
     * Fluxo para o qual as tuplas ser�o serializadas.
     */
    protected DataOutputStream out;


    /**
     * Metadados das tuplas a serem serializadas.
     */
    protected Metadata metadata;

    /**
     * Tamanho do buffer de grava��o utilizado.
     */
    public static final int BUFFER_SIZE = 200000;
    //public static final int BUFFER_SIZE = 16384;

    // Othman
    //private ByteArrayOutputStream byteArrayOut;

    /**
     * Log4j logger.
     */
    @SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(TupleWriter.class.getName());

    /**
     * Construtor pad�o.
     *
     *@param out FLuxo de destino das tuplas serializadas.
     *@param instanceMetadata Metadado das inst�ncias a serem serializadas.
     */
    // XXX Author : Othman - new TupleWriter constructor
    public TupleWriter(OutputStream out, Metadata instanceMetadata) {

        this.out = new DataOutputStream(new BufferedOutputStream(out, BUFFER_SIZE));
        this.metadata = instanceMetadata;
    }

//    public TupleWriter(Metadata instanceMetadata) {
//
//    	byteArrayOut = new ByteArrayOutputStream();
//        this.out = new DataOutputStream(new BufferedOutputStream(byteArrayOut, BUFFER_SIZE));
//        this.metadata = instanceMetadata;
//    }

    /**
     * Serializa uma tupla.
     * @param t Inst�ncia a ser serializada.
     * @throws IOException Se acontecer algum problema durante a serializa��o.
     */
    public void writeInstance(Instance t) throws IOException {

        if(t!=null) {
            out.writeChar('2');
            t.write(out, metadata);
        } else {
            out.writeChar('1');
        }
    }

    /**
     * Serializa um bloco de tuplas.
     * @param block Bloco de tuplas a ser serializado.
     * @throws IOException Se acontecer algum problema durante a serializa��o
     */
    public void writeBlock(Block block) throws IOException {
        int blockSize;
        Tuple t;

        if(block!=null) {

            out.writeChar('2');
            blockSize =block.size();
            out.writeInt(blockSize);

            block.write(out, metadata);//grava caracteristicas do bloco
            //Grava tuplas do bloco
            for(;blockSize>0; blockSize--){
                t = (Tuple)block.get();
                this.writeInstance(t);
            }

        } else {
            out.writeChar('1');
        }
    }

    /**
     *
     */

    public void flush() throws IOException {
        out.flush();
    }

    /**
     * Fecha o fluxo.
     * @throws IOException Se acontecer enquanto o fluxo � fechado.
     */
    public void close() throws IOException {
        out.writeChar('0');
        out.close();
    }

    public static void main(String[] args) {

        int i=260;

        byte b =(byte)i;

        System.out.println(b);
    }


}

