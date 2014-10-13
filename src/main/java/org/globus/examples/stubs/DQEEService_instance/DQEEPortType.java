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
/**
 * DQEEPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package org.globus.examples.stubs.DQEEService_instance;

public interface DQEEPortType extends java.rmi.Remote {
    public org.globus.examples.stubs.DQEEService_instance.InitializationResponse initializeService(int parameters) throws java.rmi.RemoteException;
    public org.globus.examples.stubs.DQEEService_instance.InitRemoteResponse initRemote(java.lang.String parameters) throws java.rmi.RemoteException;
    public javax.activation.DataHandler getMetadataRemote(int parameters) throws java.rmi.RemoteException;
    public org.globus.examples.stubs.DQEEService_instance.OpenRemoteResponse openRemote(int parameters) throws java.rmi.RemoteException;
    public javax.activation.DataHandler getNextRemote(org.globus.examples.stubs.DQEEService_instance.GetNextRemoteRequest parameters) throws java.rmi.RemoteException;
    public org.globus.examples.stubs.DQEEService_instance.CloseRemoteResponse closeRemote(int parameters) throws java.rmi.RemoteException;
    public javax.activation.DataHandler executeRemote(org.globus.examples.stubs.DQEEService_instance.ExecuteRemoteRequest parameters) throws java.rmi.RemoteException;
    public org.globus.examples.stubs.DQEEService_instance.BlackBoardResponse copyToRemoteBlackBoard(org.globus.examples.stubs.DQEEService_instance.BlackBoardParams parameters) throws java.rmi.RemoteException;
    public org.oasis.wsrf.properties.GetResourcePropertyResponse getResourceProperty(javax.xml.namespace.QName getResourcePropertyRequest) throws java.rmi.RemoteException, org.oasis.wsrf.properties.InvalidResourcePropertyQNameFaultType, org.oasis.wsrf.properties.ResourceUnknownFaultType;
}

