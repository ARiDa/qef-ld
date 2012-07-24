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
package ch.epfl.codimsd.qeef.datastructure;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.types.Type;


/**
 * Classe abstrata que generaliza as fun��es de uma estrutura de recupera��o complexa. 
 * Por exmplo uma hashtable ou uma �rvore.
 * 
 * @author Vinicius Fontes
 * 
 * @date Jun 19, 2005
 */
public abstract class RetrievalStructure extends Estrutura {


    /**
     * Insere uma inst�ncia na estrutura. Utiliza key na sua organiza��o.
     * 
     * @param newInstance Inst�ncia a ser inserida na estrutura.
     * @param key Chave a ser utilizada para inserir este elemento na estrutura.
     */
    public abstract void add(DataUnit newInstance, Type key);

    /**
     * Remove um elemento da estrutura que tenha a chave igual a key.
     * 
     * @return key Chave de busca utilizada.
     */
    public abstract DataUnit get(Type key);

}


