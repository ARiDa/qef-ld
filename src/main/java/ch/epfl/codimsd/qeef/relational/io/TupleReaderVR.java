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

import java.io.EOFException;
import java.io.IOException;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Block;
import ch.epfl.codimsd.qeef.Instance;

import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import java.io.InputStream;
import java.util.Scanner;
//import vtk.vtkDirectory;

/**
 * Deserializador de inst�ncias e blocos para o modelo relacional.<p>
 *
 * Toda opera��o de leitura de tupla ou bloco deve ser precedida por uma chamada a eof().
 * 
 * @author Vinicius Fontes
 */

/*
 * Formato de grava��o: 
 * (flag[tupla|Bloco])*flag
 * Onde um bloco possui o formato: NrTuplas (tuplas)
 */

public class TupleReaderVR implements ch.epfl.codimsd.qeef.io.InstanceReader {

   /* static
    {
	    System.loadLibrary("vtkCommonJava");
	    System.loadLibrary("vtkFilteringJava");
	    System.loadLibrary("vtkIOJava");
	    System.loadLibrary("vtkImagingJava");
	    System.loadLibrary("vtkGraphicsJava");
	    System.loadLibrary("vtkRenderingJava");
            System.loadLibrary("vtkVolumeRenderingJava");
	    System.loadLibrary("vtkParallelJava");
    }*/

    /**
     * Buffer user to initialize the DataInputStream.
     */
    public static final int BUFFER_SIZE = 200000;
    
    /**
     * Stream containing the tuples to read.
     */
    protected String input;
    
    /**
     * Tuple metadatas.
     */
    protected TupleMetadata metadata;
    
    /**
     * Log4j logger.
     */
    protected Logger logger;
    
    /**
     * Caracter de controle utilizado para identificar se existem mais tuplas a serem 
     * lidas ou se a pr�xima tupla � nula. Valores poss�veis:<p>
     * <ul>
     * <li>'0' eof 
     * <li>'1' Uma unidade de dado null
     * <li>'2' Existe mais uma unidade de dado
     * </ul> 
     */
    private char flag;

    private int nNumFilesInDir;

    private int nrOfDivisions;

  //  private vtkDirectory pDir;
     
    /**
     * Construtor padr�o.
     * 
     * @param input Fluxo do qual as inst�ncias ser�o lidas.
     * @param metadata Metadado das tuplas serializadas em input.
     * 
     */
  /*  public TupleReader(InputStream input, TupleMetadata metadata){
        
        this.input = new Scanner(input);
        this.metadata = metadata;
        
        this.logger = Logger.getLogger(TupleReader.class.getName());
    }*/

    public TupleReaderVR(String input, TupleMetadata metadata, int nrOfDivisions){

        /*pDir = new vtkDirectory();
        if (pDir.Open(input) == 0)
             pDir.FastDelete();

        this.nrOfDivisions = nrOfDivisions;
        nNumFilesInDir = nrOfDivisions + 1;
        if (nNumFilesInDir == 0)
            pDir.FastDelete();
*/
        this.input = input;
        this.metadata = metadata;
        this.logger = Logger.getLogger(TupleReaderVR.class.getName());
    }

    /**
     * Verifica se existem mais tuplas ou blocos a serem lidos.
     * 
     * @return True se existir mais tuplas/blocos a serem lidos, false caso contr�rio.
     */
    public boolean eof() throws IOException{

        if(this.nNumFilesInDir == 0)
            return true;
        else
        {
            this.nNumFilesInDir--;
            return false;
        }
    }
    
    /**
     * Deserializa uma tupla.
     * @return Tupla deserializada.
     * @throws IOException Se acontecer algum problema durante a leitura do fluxo.
     * @throws EOFException Se o final do fluxo for atingido antes que a tupla seja lida.
     */
	public Instance readInstance() throws IOException, EOFException{
	    
	    Tuple t=null;

            if (this.nNumFilesInDir != 0){
	    
                    t = new Tuple();
                    //t.readImages(pDir, metadata, input, nrOfDivisions);
	    }
	    
	    return t;
	}
	
    /**
     * Deserializa um bloco de tuplas.
     * @return Bloco deserializado.
     * @throws IOException Se acontecer algum problema durante a leitura do fluxo.
     * @throws EOFException Se o final do fluxo for atingido antes que todas as tuplas do bloco sejam deserializadas.
     */	
	public Block readBlock() throws IOException, EOFException {
	    
	    Block block = null;
            int blockSize;
            Tuple t;
        
	    if(flag=='0') {
	        throw new EOFException();
	        
	    } else if (flag == '1'){
	        return null;
	        
	    } else { //flag==2
	        
	        //Le caracteristicas do bloco 
	        block = new Block();
	        
	        blockSize = 1;
	        block.readSmooth(input, metadata);
	        
	        //Le tuplas	        	        	       
	        for(;blockSize>0; blockSize--){
	            eof();
                    System.out.println("readblock");
	            t = (Tuple)readInstance();
	            block.add(t);            
	        }
	    }
	    return block;
	}
	
    /**
     * Fecha o fluxo de leitura.
     * @throws IOException Se acontecer enquanto o fluxo � fechado.
     */
	public void close() throws IOException{
	 //   input.close();
	}		
}

