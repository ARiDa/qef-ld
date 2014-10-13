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

import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.Logger;
import org.globus.examples.stubs.DQEEService_instance.BlackBoardParams;
import org.globus.examples.stubs.DQEEService_instance.DQEEPortType;

import ch.epfl.codimsd.exceptions.distributed.DistributedException;
import ch.epfl.codimsd.qeef.scheduler.G2NInfoNodes;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qeef.util.RemoteBlackBoardManager;
import ch.epfl.codimsd.query.Request;
import java.net.InetAddress;

/**
 * The DistributedManager builds the Globus environment, starts the remote and local web services. It
 * prepares remote nodes for the execution of requests.
 * 
 * @author Othman Tajmouati.
 */
public class DistributedManager extends Thread {

	/**
	 * Log4 logger.
	 */
	private static Logger logger = Logger.getLogger(DistributedManager.class.getName());

	/**
	 * Remote nodes carrying the execution of the request.
	 */
	private DQEEPortType []qenodes;
	
	/**
	 * Boolean flag indicating if the GT4 containers are started or not.
	 */
	private boolean containersStarted;
	
	/**
	 * DistributedManager singleton reference.
	 */
	private static DistributedManager ref;
	
	/**
	 * Factory that creates the WebService once.
	 */
	private WebServiceFactory webServiceFactory;
	
	private String[] serviceURI;
	private LinkedList qepRemoteList;
	private boolean webServiceCrached;
	private int startedWebServicesNumber;
	
	/**
	 * Default constructor.
	 */
	private DistributedManager() {
		
		// Intializations
		containersStarted = false;
		
		// Initialize the WebServiceFactory.
		webServiceFactory = new WebServiceFactory();
	
		webServiceCrached = false;
		startedWebServicesNumber = 0;
	}
	
	/**
	 * @return singleton reference of the DistributedManager.
	 */
	public static synchronized DistributedManager getDistributedManager() {
		
		if (ref == null)
			ref = new DistributedManager();
		
		return ref;
	}
	
	/**
	 * This method builds instances of WebService for the distributed execution on remote
	 * nodes and should be only invoked if distribution is specified in the QEP.
	 * 
	 * @param request the request to execute
	 * @throws DistributedException
	 */
	public DQEEPortType buildEnvironement(Request request) throws DistributedException {
	
		// node id
		int i = 0;
		
		// Get node informations and remote QEP to send to remote nodes.
		BlackBoard bl = BlackBoard.getBlackBoard();
		G2NInfoNodes infoNodes = (G2NInfoNodes) bl.get(Constants.infoNodes);
		qepRemoteList = (LinkedList) bl.get(Constants.qepRemoteList);
		serviceURI = infoNodes.getNodes();
		int numberOfRemoteNodes = infoNodes.getNumberOfNodes();
		
		// Change "localhost" to the real IP address and put it in the BlackBoard.
		RemoteBlackBoardManager.prepareBlackBoard(request);

		try {

			if (!containersStarted) {
				
				//  Start gt4 containers.
				String start = SystemConfiguration.getSystemConfigInfo(Constants.START_WEBSERVICES_FROM_CODE);
				if (start != null) {
					if (start.equalsIgnoreCase("TRUE"))
						startContainers();
				}	
				
				// Initialize the local Web service.
				qenodes = new DQEEPortType[numberOfRemoteNodes+1];
			}
			
			// Prepare the remote nodes.
			ArrayList<Starter> starterThreads = new ArrayList<Starter>();
			for (i = 0; i <= numberOfRemoteNodes; i++) {
				
				Starter starter = new Starter(i);
				starter.start();
				starterThreads.add(starter);
			}
			
			long veryfingStartingThreads = System.currentTimeMillis();
			while (startedWebServicesNumber != (numberOfRemoteNodes+1)) {
				
				try {
					
					Thread.sleep(1000);
					if (webServiceCrached == true)
						break;
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			// Close all Starter Threads
			for (Starter starter : starterThreads) {
				
				synchronized (starter) {
					starter.interrupt();
				}
			}
			
			if (webServiceCrached == true) {
				throw new DistributedException("One or more web services are not initialized.");
			} else {
				startedWebServicesNumber = 0;
				containersStarted = true;
			}

			logger.debug("beginStartingThreads : " + (System.currentTimeMillis() - veryfingStartingThreads));
			// bl.remove(Constants.THIS_NODE);
			
		}  catch (Exception ex) {
			
			containersStarted = false;
			ex.printStackTrace();
			throw new DistributedException("Exception in DistributedManager: " +  ex.getMessage());
		}

		// Return the first Web service.
		return qenodes[0];
	}

	/**
	 * Start local containers depending on the execution environment id.
	 * 
	 * @return the number of started consumers.
	 * @throws DistributedException
	 */
	private void startContainers() throws DistributedException {
		
		int containersSize = 1;
		
		try {

			// Load the Runtime environment.
			Runtime load = Runtime.getRuntime();

			String addressLocalWebService = SystemConfiguration.getSystemConfigInfo(Constants.LOCAL_WEB_SERVICE);

			List<String> addresses = new ArrayList<String>();
                        List<String> machines = new ArrayList<String>();
			addresses.add(addressLocalWebService);
                        
			for (String address : serviceURI) {
				addresses.add(address);
				containersSize++;
			}

			for (String address : addresses) {

				int index = address.lastIndexOf(":");
                                String port = null;
                                
                                try
                                {
                                    port = address.substring(index + 1, index + 5);
                                    if((address.contains("localhost")) || (address.contains(InetAddress.getLocalHost().getHostAddress())))
                                    {
                                        load.exec(new String[]{"gnome-terminal", "-x", "globus-start-container", "-nosec", "-p", port});
                                    }
                                    else
                                    {
                                        String[] ipPort = address.split("/");
                                        String[] ip = ipPort[2].split(":");

                                        if(machines.contains(ip[0]))
                                        {
                                            sleep(500);
                                            load.exec(new String[]{"gnome-terminal", "-x", "bash", "/srv/QEF/CoDIMS-tcp/src/codims-home/Scripts/startContainer.sh", ip[0], port});
                                        }
                                        else
                                        {
                                            load.exec(new String[]{"gnome-terminal", "-x", "bash", "/srv/QEF/CoDIMS-tcp/src/codims-home/Scripts/startMachine.sh", ip[0], port});
                                            machines.add(ip[0]);
                                        }
                                    }
                                }
                                catch (Exception ex){
                                    throw new DistributedException("Exception in StartContainers Function Wrong Address: " +  ex.getMessage());
                                }
			}

			// Wait a moment. The Web services should be started.
			// We assume each container takes CONTAINER_INIT_WAIT_TIME to start.
			sleep(containersSize * Constants.CONTAINER_INIT_WAIT_TIME);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	class Starter extends Thread {
		
		private int numNode;
		
		public Starter(int numNode) {
			this.numNode = numNode;
		}
		
		public void run() {
			prepareNode(numNode);
		}
		
		private void prepareNode(int numNode) {
			
			try {
				
				long startingThreads = System.currentTimeMillis();
				
				BlackBoard bl = BlackBoard.getBlackBoard();
				String addressLocalWebService = SystemConfiguration.getSystemConfigInfo(Constants.LOCAL_WEB_SERVICE);
			
				// Initialize remote Web services.
				if (!containersStarted)
					qenodes[numNode] = (numNode==0) ? webServiceFactory.createService(addressLocalWebService) : 
						webServiceFactory.createService(serviceURI[numNode-1]);
				
				// Initialize the service.
				if (!containersStarted)
					qenodes[numNode].initializeService(numNode);
				
				//Get the local blackBoard hashmap.
				// BlackBoard bl = BlackBoard.getBlackBoard();
				//long BBTime = System.currentTimeMillis();
				HashMap<String, Object> blackHash = bl.getHashtable();
				HashMap<String, Object> hash = new HashMap<String, Object>();
				hash.putAll(blackHash);

				// Get the set of keys.
				Set blackBoardStrings = hash.keySet();
				Iterator itt = blackBoardStrings.iterator();
				
				while (itt.hasNext()) {
					
					// Get the object corresponding to this key.
					String key1 = (String) itt.next();
					Object valueObj = (Object) bl.get(key1);
					String value1 = "";
					
					// if the object is a string one, it could be copied together with its key
					// to the remote blackBoard.
					if (valueObj instanceof String) {
						
						value1 = (String) valueObj;
						BlackBoardParams params = new BlackBoardParams(key1, value1);
						qenodes[numNode].copyToRemoteBlackBoard(params);
						
					} else if (valueObj instanceof String[]) {
						
						String[] newValueObj = (String[]) valueObj;
						
						for (int j = 0; j < newValueObj.length; j++) {
							value1 += newValueObj[j] + ",";
						}
						
						value1 = value1.substring(0, value1.length()-1);
						BlackBoardParams params = new BlackBoardParams(key1, value1);
						qenodes[numNode].copyToRemoteBlackBoard(params);
					}
				}
				
				if (numNode != 0) {
					BlackBoardParams params = new BlackBoardParams(Constants.THIS_NODE, numNode+"");
					qenodes[numNode].copyToRemoteBlackBoard(params);
				}

				// Send the QEP.
				qenodes[numNode].initRemote((String)qepRemoteList.get(numNode));
				
				if (numNode == 0)
                                {
					logger.debug("Local web service initialized.");
                                }
				else{
					logger.debug("Remote node n� " + numNode + " initialized.");
                                }

				if (numNode != 0){
					logger.debug("StartingThread time " + serviceURI[numNode-1] + " : " + (System.currentTimeMillis() - startingThreads));
                                }
				startedWebServicesNumber++;
				
			} catch (RemoteException ex) {
				
				startedWebServicesNumber++;
				webServiceCrached = true;
				ex.printStackTrace();	
			}
		}
	}
}

