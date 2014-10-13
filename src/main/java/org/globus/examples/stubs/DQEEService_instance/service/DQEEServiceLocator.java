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
 * DQEEServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package org.globus.examples.stubs.DQEEService_instance.service;

public class DQEEServiceLocator extends org.apache.axis.client.Service implements org.globus.examples.stubs.DQEEService_instance.service.DQEEService {

    public DQEEServiceLocator() {
    }


    public DQEEServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DQEEServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for DQEEPortTypePort
    private java.lang.String DQEEPortTypePort_address = "http://localhost:8080/wsrf/services/";

    public java.lang.String getDQEEPortTypePortAddress() {
        return DQEEPortTypePort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String DQEEPortTypePortWSDDServiceName = "DQEEPortTypePort";

    public java.lang.String getDQEEPortTypePortWSDDServiceName() {
        return DQEEPortTypePortWSDDServiceName;
    }

    public void setDQEEPortTypePortWSDDServiceName(java.lang.String name) {
        DQEEPortTypePortWSDDServiceName = name;
    }

    public org.globus.examples.stubs.DQEEService_instance.DQEEPortType getDQEEPortTypePort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(DQEEPortTypePort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDQEEPortTypePort(endpoint);
    }

    public org.globus.examples.stubs.DQEEService_instance.DQEEPortType getDQEEPortTypePort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.globus.examples.stubs.DQEEService_instance.bindings.DQEEPortTypeSOAPBindingStub _stub = new org.globus.examples.stubs.DQEEService_instance.bindings.DQEEPortTypeSOAPBindingStub(portAddress, this);
            _stub.setPortName(getDQEEPortTypePortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDQEEPortTypePortEndpointAddress(java.lang.String address) {
        DQEEPortTypePort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.globus.examples.stubs.DQEEService_instance.DQEEPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.globus.examples.stubs.DQEEService_instance.bindings.DQEEPortTypeSOAPBindingStub _stub = new org.globus.examples.stubs.DQEEService_instance.bindings.DQEEPortTypeSOAPBindingStub(new java.net.URL(DQEEPortTypePort_address), this);
                _stub.setPortName(getDQEEPortTypePortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("DQEEPortTypePort".equals(inputPortName)) {
            return getDQEEPortTypePort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.globus.org/namespaces/examples/core/DQEEService_instance/service", "DQEEService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.globus.org/namespaces/examples/core/DQEEService_instance/service", "DQEEPortTypePort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        if ("DQEEPortTypePort".equals(portName)) {
            setDQEEPortTypePortEndpointAddress(address);
        }
        else { // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}

