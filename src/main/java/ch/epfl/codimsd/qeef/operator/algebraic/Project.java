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
package ch.epfl.codimsd.qeef.operator.algebraic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.epfl.codimsd.qeef.Data;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qep.OpNode;

public class Project extends Operator {
    
    /**
     * Attributes that will be projected.
     */
    private String[] projectedAttributes;

    private int[] projectedAttributesOrder;
    
    /**
     * Componente de log.
     */
    final static Logger logger = LoggerFactory.getLogger(Project.class);

    /**
     * @param id Identificador do operador.
     */
    public Project(int id, OpNode opNode) {
        super(id, opNode);
        logger.debug("Project id: {}", id);
        
        if (opNode.getParams() != null && opNode.getParams().length != 0 && opNode.getParams()[0].trim().length() != 0) {
            this.projectedAttributes = opNode.getParams()[0].split("\\s*,\\s*");
        }
        this.projectedAttributesOrder = new int[this.projectedAttributes.length];
        
        logger.debug("Projected attributes: {}", this.projectedAttributes);
    }


	/**
	 * Consome uma instância de seu produtor e usa dela os dados devidos.
	 * 
	 * @param idConsumer Identificador do consumidor que está solicitando uma nova instância.
	 * 
	 * @see ch.epfl.codimsd.qeef.Op#getNext(java.lang.Object)
	 */
    public DataUnit getNext(int idConsumer) throws Exception {
        Instance producerInstance = (Instance)super.getNext(idConsumer);
        if (producerInstance == null) {
            return null;
        }
        this.instance = producerInstance.getClass().newInstance();
        for (int i=0; i < this.projectedAttributes.length; i++) {
            ((Instance)this.instance).addData(producerInstance.getData(projectedAttributesOrder[i]));
        }
        
        logger.debug("Project.getNext: {}", this.instance);
        return this.instance;
    }

    public void close() throws Exception {
    	super.close();
    }
    
    public void setMetadata(Metadata[] prdMetadata) {
        try {
            this.metadata[0] = prdMetadata[0].getClass().newInstance();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
        for (int i=0; i < this.projectedAttributes.length; i++) {
        	Data data = prdMetadata[0].getData(this.projectedAttributes[i]);
        	if (data == null) {
        		throw new RuntimeException("Projected attribute not found: " + this.projectedAttributes[i]);
        	}
            this.metadata[0].addData(data);
            projectedAttributesOrder[i] = prdMetadata[0].getDataOrder(this.projectedAttributes[i]);
        }
    }
}

