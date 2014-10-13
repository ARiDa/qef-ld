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
package ch.epfl.codimsd.qeef.io;

import java.io.EOFException;
import java.io.IOException;

import ch.epfl.codimsd.qeef.Block;
import ch.epfl.codimsd.qeef.Instance;


/**
 * Fornece a interface de um fluxo capaz de deserializar inst�ncias e blocos de um determinado 
 * modelo de dados.
 * 
 * @author Vinicius Fontes. 
 */

public interface InstanceReader{    
    
    /**
     * Realiza a leitura de uma inst�ncia.
     * 
     * @throws IOException Se algum problema ocorrer durante a leitura dos dados da inst�ncia.
     * @throws EOFException Se o fim do fluxo foi atingido antes que os dados de uma inst�ncias tenham sido lidos.
     */
	public Instance readInstance() throws IOException, EOFException;
	
	/**
     * Realiza a leitura de um bloco de inst�ncias.
     * 
     * @throws IOException Se algum problema ocorrer durante a leitura dos dados da inst�ncia.
     * @throws EOFException Se o fim do fluxo foi atingido antes que todas as inst�ncias do bloco tenham sido lidas.
     */
	public Block readBlock() throws IOException, EOFException;
	
	/**
	 * Fecha o fluxo de dados do qual as inst�ncias est�o sendo lidas.
	 * 
	 * @throws IOException Se algum problema enquanto se fecha o fluxo de dados.
	 */
	public void close() throws IOException;
	
	/**
	 * Verifica se exist�m mais int�ncias a serem lidas.
	 * 
	 * @return True se existirem mais inst�ncias a serem lidas do fluxo, false caso contr�rio.
	 * 
	 * @throws IOException Se Ocorrer algum problema ao verifirar se o fluxo possui mais inst�ncias a serem lidas.
	 */
	public boolean eof() throws IOException;

}

