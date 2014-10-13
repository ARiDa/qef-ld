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

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Vector;

/**
 * Unidade de dados que ecapsula um conjunto de inst�ncias.
 * Unit of data that ecapsula a set of instances.
 * 
 * @author Vinicius Fontes
 */
@SuppressWarnings("serial")
public class Block extends DataUnit implements Cloneable, Serializable {

    /**
     * Lista de inst�ncias deste bloco.
     */
    protected Vector<Instance> instances;

    /**
     * Construtor padr�o.
     */
    public Block() {
        instances = new Vector<Instance>();
    }

    /**
     * Adiciona uma inst�ncia a este bloco.
     * 
     * @param instance
     *            Instancia a ser adicionada ao bloco.
     */
    public void add(Instance instance) {

        instances.add(instance);
    }

    /**
     * Retira uma inst�ncia deste bloco.
     * 
     * @return Inst�ncia removida.
     */
    public Instance get() {

        return (Instance) instances.remove(0);
    }

    /**
     * Obtem o tamanho deste bloco.
     * 
     * @return N�mero de inst�ncias neste bloco.
     */
    public int size() {
        return instances.size();
    }

    /**
     * Imprime as inst�ncias deste bloco no fluxo para serem visualizadas.
     * 
     * @throws IOException
     *             Se algum problema ocorrer durante a impress�o.
     */
    public void display(Writer out) throws IOException {

        int blockSize = instances.size();
        
        for (int i = 0; i < blockSize; i++) {
            if(instances.get(i) != null){
                ((Instance) instances.get(i)).display(out);
                out.write("\n");
            }
        }
    }

    /**
     * Libera as refer�ncias realizadas por esta inst�ncia. Melhora o desempenho
     * do processo de libera��o de mem�ria realizado pelo garbage colector.
     */
    public void finalize() {
        instances = null;
    }

    /**
     * Cria um novo bloco id�ntico a este. As inst�ncias do bloco tamb�m ser�o
     * clonadas.
     * 
     * @return Bloco clonado.
     */
    public Object clone() {

        Block tb = new Block();

        for (int i = 0; i < instances.size(); i++) {
            tb.add((Instance) (get()).clone());
        }
        return tb;
    }
    
    /**
     * Retorna a representacao textual das tuplas deste bloco em uma String.
     * 
     */
    public String toString() {

        String aux = "Bloco:\n";
        int blockSize = instances.size();
        
        for (int i = 0; i < blockSize; i++) {
            aux = aux + "\t" + instances.get(i);
            aux = aux + "\n";
        }
        return aux;
    }
}

