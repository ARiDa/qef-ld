package ch.epfl.codimsd.qeef.trajectory.algebraic.hash.temporal;

import java.io.File;
import java.io.FileWriter;

import java.io.StringReader;
import java.util.Hashtable;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.trajectory.PlainFilePlanManager;
import ch.epfl.codimsd.qeef.QEEF;
import ch.epfl.codimsd.qeef.operator.OperatorFactoryManager;
import ch.epfl.codimsd.qeef.trajectory.RelationalOpFactory;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.trajectory.algebraic.hash.*;
import ch.epfl.codimsd.qeef.QueryExecutionEngine;
import ch.epfl.codimsd.qep.OpNode;

/**
 * @author Vinicius Fontes
 *
 * @date Mar 15, 2005
 */
public class TemporalHashJoin extends DoublePipelineHashJoin {
    
    
    private String outerColumnName, innerColumnName;
    
    private int start, end;
    
    public TemporalHashJoin(int id, OpNode op) {
        
        super(id, op);

        this.innerColumnName = op.getParams()[5].toUpperCase();
        this.outerColumnName = op.getParams()[6].toUpperCase();

        this.start = Integer.parseInt(op.getParams()[8].substring(0, op.getParams()[8].indexOf("-")));;
        this.end = Integer.parseInt(op.getParams()[8].substring(op.getParams()[8].indexOf("-") + 1, op.getParams()[8].length()));;
    }

    /**
     * DataUnit os buckets de uma rela��o. Os buckets devem ser indexados de 0 a nrBuckets.
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

    
    
    /**
     * Define a fun��o de hash a ser utilizada.
     * 
     * @return Fun��o de hash utilizada.
     */
    public HashFunction createInnerFunction(){
   	    
   	    Metadata innerMetadata;
   	    int inner;
   	 
   	    innerMetadata = getProducer(INNER_RELATION).getMetadata( id );
   	    inner = innerMetadata.getDataOrder(innerColumnName);

   	    return new DivisaoIntervaloHashFunction( nrBuckets, start, end, inner);
    }
   	
    public HashFunction createOuterFunction(){
   	    
   	    Metadata outerMetadata;
   	    int outer;
   	    
   	    outerMetadata = getProducer(OUTER_RELATION).getMetadata( id );
   	    outer = outerMetadata.getDataOrder(outerColumnName);
   	       	    
   	    return new DivisaoIntervaloHashFunction( nrBuckets, start, end, outer);
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
        plan += "0 TemporalHashjoin ;25;28920;120;28920;200;0-24;Velocidade.idTempo;Particula.iteracao;Particula.iteracao = Velocidade.idTempo\n";
        plan += "1 scan ;Particula;_\n";
        plan += "2 scan ;Velocidade;Velocidade.idTempo < 25\n";                
        plan += "1\n";
        plan += "FIX;0;1;2\n";

//        plan = "1\n";
//        plan += "0 scan ;Particula;_\n";                
//        plan += "0\n";

        plIn = new StringReader(plan);
        plOut = new FileWriter(home + File.separator + "results" + File.separator + "TesteTemporalJoin.txt");

//        maquina.execute(plIn, plOut);
    }
}
