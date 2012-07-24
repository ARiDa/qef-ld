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
package ch.epfl.codimsd.qeef.relational.control.exchange;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import javax.activation.DataHandler;

import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Block;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;

import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.relational.io.TupleReader;
import ch.epfl.codimsd.qeef.relational.io.TupleReaderSmooth;
import ch.epfl.codimsd.qep.OpNode;

import org.globus.examples.stubs.DQEEService_instance.DQEEPortType;
import org.globus.examples.stubs.DQEEService_instance.GetNextRemoteRequest;
import org.globus.examples.stubs.DQEEService_instance.service.DQEEServiceAddressingLocator;

public class ReceiverOp extends Operator implements Receiver {

	private DQEEPortType remoteQE;
	private String gsh;
	private int idRemote;
	
	private int blockSize;
	private long waitTime;
	
	/**
	 * Log4 logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ReceiverOp.class.getName());
	
	public ReceiverOp(int opId, OpNode opNode) {
		super(opId);
		String[] opParams = opNode.getParams();
		
		this.gsh = opParams[0];
		this.idRemote = Integer.parseInt(opParams[1]);
		this.blockSize = Integer.parseInt(opParams[2]);
		this.waitTime  = Long.parseLong(opParams[3]);				
	}
	
	public DataUnit getNext(int consumerId) throws Exception {

		try {
	    DataHandler dhBlock;
	    //TupleReaderSmooth tr;
            TupleReader tr;
	    Block block = null;
	    int blockSize;
	    long waitTime;
	    
	    if( hasNext == false )
	        return null;
	    
	    
	    blockSize = this.blockSize;
	    waitTime = this.waitTime;

	    BlackBoard bl = BlackBoard.getBlackBoard();
	    if( bl.containsKey(consumerId+"_BLOCK_SIZE") && bl.containsKey(consumerId+"_WAIT_TIME")) {
	        blockSize = ((Integer)bl.get(consumerId+"_BLOCK_SIZE")).intValue();
	    	waitTime  = ((Long)bl.get(consumerId+"_WAIT_TIME")).longValue();
	    }

	    GetNextRemoteRequest request = new GetNextRemoteRequest();
	    request.setBlockSize(blockSize);
	    request.setRemoteOp(idRemote);
	    request.setWaitTime(waitTime);
	    dhBlock = remoteQE.getNextRemote(request);

	    InputStream is = dhBlock.getDataSource().getInputStream();
	    tr = new TupleReader(new DataInputStream(is), (TupleMetadata)metadata[0]);
	    tr.eof();
	    block = tr.readBlock();
	    
	    if(block == null) {
	        hasNext = false;
	        // logger.info("---->ReceiverOp(" + id + "): It received NULL. It requested size block  " + blockSize);
	    } else {
	    	// logger.debug("---->ReceiverOp(" + id + "): It received Block from Size  " + block.size() + ". It requested size block  " + blockSize);
	    }
		
		return block;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/** ****************************************************** */
	/**/
	/** ****************************************************** */
	public void open() throws Exception{

	    super.open();
	    
		try {
		    
			DQEEServiceAddressingLocator locator = new DQEEServiceAddressingLocator();

			// Create endpoint reference to service
			EndpointReferenceType endpoint = new EndpointReferenceType();
			endpoint.setAddress(new Address(gsh));
			remoteQE = locator.getDQEEPortTypePort(endpoint);
			
			//Inicializa consumidor remoto
			remoteQE.openRemote(idRemote);
			
                        //Obtem metadados
                        DataHandler dh = remoteQE.getMetadataRemote(idRemote);
                        Object o = (new ObjectInputStream(dh.getInputStream())).readObject();

                        metadata = (Metadata[])o;

		} catch (Exception exc) {
			// logger.warn("RECEIVER(" + id + "): Erro while opening.\n" + exc);
			throw exc;
		}		
	}
	
    public void setMetadata(Metadata metadata[]){        
    }

	public void close() throws Exception{

		try {
			
			remoteQE.closeRemote(idRemote);
			// logger.info("Receiver(" + id + "): closed");
			
		} catch (RemoteException remExc) {
			logger.warn("Cannot close Reveiver operator.");
			logger.warn(remExc);
		    throw remExc;
		}

	}

}

