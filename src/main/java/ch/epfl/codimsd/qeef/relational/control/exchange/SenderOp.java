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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

import javax.activation.DataHandler;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Block;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.operator.control.Request;
import ch.epfl.codimsd.qeef.relational.io.TupleWriter;
import ch.epfl.codimsd.qep.OpNode;

public class SenderOp extends Operator implements Sender {

	/**
	 * Log4j logger.
	 */
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SenderOp.class.getName());
	
	private Hashtable<Integer, Request> requests;
	
	public SenderOp(int opId, OpNode opNode) {

		super(opId);
	}

	@SuppressWarnings("unchecked")
	public void open() throws Exception {
		super.open();
		
		//requests = (Hashtable)blackBoard.get(getProducer(0).getId() + "_REQUEST_LIST");
		BlackBoard bl = BlackBoard.getBlackBoard();
		requests = (Hashtable) bl.get(getProducer(0).getId() + "_REQUEST_LIST");
	}

	public void setMetadata(Metadata metadata[]) {
		this.metadata[0] = (Metadata) metadata[0].clone();
	}

	@SuppressWarnings("deprecation")
	public DataHandler getNextRemote(int blockSize, long waitTime)
			throws Exception {
                
		Request request;
		Block tb = null;
		DataHandler dh = null;
		byte[] returnBytes;
		ByteArrayOutputStream out;
		DataOutputStream dtOut;
		TupleWriter tw;

		// seta parametros da requisicao no blackboard
		request = new Request();
		request.id = this.id;
		
		request.blockSize = blockSize;
		request.waitTime = waitTime;
		requests.put(new Integer(id), request);
		
//		logger.debug("Sender(" + id
//				+ ") it received request from block with size  " + blockSize + " Wait Time " + waitTime);

		tb = (Block) super.getNext(id);		
		
//		if (tb != null)
//			logger.debug("<----Sender(" + id + ") it sends block  : "
//					+ tb.size() + " . Requested block of size  " + blockSize);
//		else {
//			logger.info("<----Sender(" + id
//					+ ") it sends block  : null . Requested block of size  "
//					+ blockSize);
//		}
		
		
		out = new ByteArrayOutputStream();
		dtOut = new DataOutputStream(out);
		tw = new TupleWriter(dtOut, metadata[0]);
		tw.writeBlock(tb);
		tw.flush();
		
		returnBytes = out.toByteArray();
		
		dh = new DataHandler(new org.apache.turbine.util.mail.ByteArrayDataSource(returnBytes,"application/octet-stream"));

		return dh;
	}

	@SuppressWarnings("deprecation")
	public DataHandler getMetadataRemote() throws Exception {

		DataHandler dh = null;
		byte[] returnBytes;
		ByteArrayOutputStream out;
		ObjectOutputStream objOut;

		out = new ByteArrayOutputStream();
		objOut = new ObjectOutputStream(out);

		objOut.writeObject(metadata);
		objOut.flush();

		returnBytes = out.toByteArray();
		dh = new DataHandler(new org.apache.turbine.util.mail.ByteArrayDataSource(returnBytes,"application/octet-stream"));

		return dh;
	}

	public void openRemote() throws Exception {
		open();
	}

	public void closeRemote() throws Exception {
		
		close();
//		logger.info("Sender(" + id + "): closed.");
	}
}

