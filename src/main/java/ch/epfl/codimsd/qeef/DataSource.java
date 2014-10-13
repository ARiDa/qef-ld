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

import java.util.Map;

/**
 * Classe Abstrata que generaliza todas as fontes de dados.
 * Abstract classroom that generalizes all the sources of data. 
 * 
 * @author Fausto Ayres, Vinicius Fontes
 * 
 * @date Jun 18, 2005
 */
public abstract class DataSource {

    /**
     * Nome utilizado para referenciar esta fonte de dados.
     * Used name to reference this source of data. 
     */
    protected String alias;

    /**
     * Metadados desta fonte.
     * Metadada of this source. 
     */
    protected Metadata metadata;
    
    /**
     * Construtor padrão.
     * @param name Nome da fonte ed dados.
     * @param metadata Metadado que define o formato das instâncias nesta fonte.
     */
    public DataSource(String name, Metadata metadata) {
        name = name.toUpperCase();
        this.alias = name;
        this.metadata = metadata;
    }

    /**
     * Obtem o nome usado para referenciar esta fonte de dados.
     * 
     * @return Nome da fonte.
     */
    public String getAlias() {
        return (alias);
    }

    /**
     * Obtem o metadado que define o formato desta fonte de dados.
     * 
     * @return Metadado desta fonte de dados.
     */
    public Metadata getMetadata() {
        return (metadata);
    }
    
    /**
     * Sets the metadata for this DataSource. This method is used when we specify a null metadata in
     * the constructor of DataSource
     * 
     * @param metadata the metadata for this dataSource
     */
    public void setMetadata(Metadata metadata) {
    	this.metadata = metadata;
    }

    /**
     * Inicializa a fonte de dados para que se possa iniciar uma leitura.
     * Inicializes the source of data so that it can initiate a reading. 
     * 
     * @throws Exception
     *             Se acontecer algum problem na inicializa��o da fonte de
     *             dados.
     */
    public abstract void open() throws Exception;

    /**
     * Realiza a leitura de uma inst�ncia nesta fonte de dados. Null se n�o existir mais tuplas.
     * 
     * It carries through the reading of an instance in this source of data.  
     * Null if no tuple exist anymore.
     * 
     * @return Inst�ncia lida desta fonte de dados.
     * @throws Exception
     *             Se acontecer algum erro durante a leitura.
     */
    public abstract DataUnit read() throws Exception;

    /**
     * Encerra o proecsso de leitura da fonte de dados. Os recuros ocupados s�o
     * liberados.
     * It locks up procses of reading of the source of data.  The busy recuros are set free. 
     * 
     * @throws Exception
     *             Se acontecer algum problema durante a libera��o dos recursos.
     */
    public abstract void close() throws Exception;
    
    
    /**
     * 
     * @return
     * @throws Exception
     */
    public DataSource cloneDatasource(Map<String, Object> params) throws Exception {
    	DataSource ds = (DataSource)this.getClass().newInstance();
    	ds.alias = this.alias;
    	ds.metadata = this.metadata;
    	return ds;
    }

    
	/**
	 * Processes a message normally updating the params.
	 * @param params
	 */
    public void processMessage(Map<String, Object> params) {}

    
}


