/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.codimsd.qeef.trajectory.algebraic.hash.spatial;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Hashtable;

import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.QEEF;
import ch.epfl.codimsd.qeef.trajectory.PlainFilePlanManager;
import ch.epfl.codimsd.qeef.operator.OperatorFactoryManager;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.trajectory.algebraic.hash.HashFunction;
import ch.epfl.codimsd.qeef.trajectory.algebraic.hash.DoublePipelineHashJoin;
import ch.epfl.codimsd.qeef.trajectory.algebraic.hash.ListBucket;
import ch.epfl.codimsd.qeef.QueryExecutionEngine;
import ch.epfl.codimsd.qep.OpNode;

/**
 * @author Vinicius Fontes
 *
 * @date Mar 15, 2005
 */
public class SpatialHashJoin extends DoublePipelineHashJoin {

    private String outerColumnName, innerColumnName, metacelLimits;

    public SpatialHashJoin(int id, OpNode op) {

        super(id, op);
        
        this.outerColumnName = op.getParams()[6].toUpperCase();
        this.innerColumnName = op.getParams()[5].toUpperCase();
        this.metacelLimits = op.getParams()[8];
    }


    /**
     * DataUnit os buckets de uma rela��o. Os buckets devem ser indexados de 1 a nrBuckets.
     *
     * @param buckets Tabela hash na qual os buckets devem ser inseridos.
     * @param nrBuckets N�mero de buckets a ser criado.
     * @param bucketSize Capacidade do bucket.
     * @param metadata Metadado que descreve o formato das tuplas armazenadas neste bucket.
     * @param home Local onde o arquivo de persist�ncia do bucket ser� criado.
     *
     * @throws Exception Se ocorrer algum erro durante a cria��o dos buckets.
     */
    protected void createBuckets(Hashtable buckets, int nrBuckets,
            int bucketSize, int relation, Metadata metadata, File home) throws Exception{

        ListBucket newBucket;

        for (int i = 0; i < nrBuckets; i++) {

            newBucket = new ListBucket(id, i, relation, bucketSize, home+"", (TupleMetadata)metadata);
            buckets.put(new Integer(i), newBucket);
        }
    }

   	public HashFunction createInnerFunction(){

   	    Metadata innerMetadata;
   	    int inner;

   	    innerMetadata = getProducer(INNER_RELATION).getMetadata( id );
   	    inner = innerMetadata.getDataOrder(innerColumnName);

   	    return new GridPartitioningHashFunction( nrBuckets, inner, metacelLimits );
   	}

   	public HashFunction createOuterFunction(){

   	    Metadata outerMetadata;
   	    int outer;

   	    outerMetadata = getProducer(OUTER_RELATION).getMetadata( id );
   	    outer = outerMetadata.getDataOrder(outerColumnName);

   	    return new GridPartitioningHashFunction( nrBuckets, outer, metacelLimits );
   	}

    /**
     *
     */
    public static void main(String[] args) throws Exception {

        File home;
        QEEF maquina;
        PlainFilePlanManager plMgr;
        OperatorFactoryManager opFac;

        String plan;
        StringReader plIn;
        FileWriter plOut;

        home = new File("/home/douglas/NetBeansProjects/CoDIMS/build/classes/codims-home");
        opFac = new OperatorFactoryManager();
        plMgr = new PlainFilePlanManager(opFac);

        maquina = new QueryExecutionEngine();

        plan = "3\n";
        plan += "0 SpatialHashJoin ;8;12100;150;10000;200;Tetraedro.vertices;Particula.ponto;Particula.ponto ISINSIDE Tetraedro.vertices;-0.409683 -1.044562 -0.932400 0.041711 -0.211005 1.407617#-0.398143 -1.551585 01.307436 0.047250 -0.211005 2.927826#-0.378613 -0.292320 -0.932400 0.042523 00.453248 0.255864#-0.390677 -0.300942 00.173429 0.052477 01.128682 2.100821#-0.033205 -1.044562 -0.932400 0.410329 -0.191613 1.351209#-0.038234 -1.551585 01.267152 0.401973 -0.198412 2.927826#-0.029383 -0.281856 -0.932400 0.378720 00.453248 0.251265#-0.038500 -0.285698 00.194394 0.387524 01.128682 2.100821\n";
        plan += "1 scan ;Particula;_\n";
        plan += "2 scan ;Tetraedro;_\n";
        plan += "1\n";
        plan += "FIX;0;1;2\n";

        plIn = new StringReader(plan);
        plOut = new FileWriter(home + File.separator + "results" + File.separator + "TesteTemporalJoin.txt");

//        maquina.execute(plIn, plOut);
    }
}

