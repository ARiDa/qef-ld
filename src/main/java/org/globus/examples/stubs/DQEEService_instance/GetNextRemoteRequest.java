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
 * GetNextRemoteRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Mar 03, 2006 (12:17:06 EST) WSDL2Java emitter.
 */

package org.globus.examples.stubs.DQEEService_instance;

public class GetNextRemoteRequest  implements java.io.Serializable {
    private int remoteOp;
    private int blockSize;
    private long waitTime;

    public GetNextRemoteRequest() {
    }

    public GetNextRemoteRequest(
           int blockSize,
           int remoteOp,
           long waitTime) {
           this.remoteOp = remoteOp;
           this.blockSize = blockSize;
           this.waitTime = waitTime;
    }


    /**
     * Gets the remoteOp value for this GetNextRemoteRequest.
     * 
     * @return remoteOp
     */
    public int getRemoteOp() {
        return remoteOp;
    }


    /**
     * Sets the remoteOp value for this GetNextRemoteRequest.
     * 
     * @param remoteOp
     */
    public void setRemoteOp(int remoteOp) {
        this.remoteOp = remoteOp;
    }


    /**
     * Gets the blockSize value for this GetNextRemoteRequest.
     * 
     * @return blockSize
     */
    public int getBlockSize() {
        return blockSize;
    }


    /**
     * Sets the blockSize value for this GetNextRemoteRequest.
     * 
     * @param blockSize
     */
    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }


    /**
     * Gets the waitTime value for this GetNextRemoteRequest.
     * 
     * @return waitTime
     */
    public long getWaitTime() {
        return waitTime;
    }


    /**
     * Sets the waitTime value for this GetNextRemoteRequest.
     * 
     * @param waitTime
     */
    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetNextRemoteRequest)) return false;
        GetNextRemoteRequest other = (GetNextRemoteRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.remoteOp == other.getRemoteOp() &&
            this.blockSize == other.getBlockSize() &&
            this.waitTime == other.getWaitTime();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getRemoteOp();
        _hashCode += getBlockSize();
        _hashCode += new Long(getWaitTime()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetNextRemoteRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.globus.org/namespaces/examples/core/DQEEService_instance", ">getNextRemoteRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("remoteOp");
        elemField.setXmlName(new javax.xml.namespace.QName("", "remoteOp"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("blockSize");
        elemField.setXmlName(new javax.xml.namespace.QName("", "blockSize"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("waitTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "waitTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}

