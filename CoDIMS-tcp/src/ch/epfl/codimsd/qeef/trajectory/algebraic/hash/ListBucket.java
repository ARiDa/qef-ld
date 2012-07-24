package ch.epfl.codimsd.qeef.trajectory.algebraic.hash;

import java.io.FileNotFoundException;
import java.io.IOException;

import ch.epfl.codimsd.qeef.datastructure.Buffer;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;

/**
 * 
 * Implementa��o de bucket para particionamento  
 *
 * @author Vinicius Fontes
 * 
 * @date Jun 13, 2005
 */
public class ListBucket extends Bucket implements Comparable {
    
    //private Predicate joinPredicate;
    
    /**
     * Implementa um bucket com um buffer. A implementa��o de match � realizada
     * por um algoritmo de loops aninhados que para cada par aplica o predicado de jun��o.
     *  
     */
    public ListBucket(int hashId, int bcktId, int relation, int capacity,
            String path, TupleMetadata metadata) throws FileNotFoundException,
            IOException {


        super(hashId, bcktId, relation, capacity, path, new Buffer(), metadata);

    //    this.joinPredicate = joinPredicate;
    }

    /**
     * Insere uma tupla na estrutura de mem�ria utilizada. Neste caso um buffer.
     * 
     * @param tuple
     *            Tupla a ser inserida.
     * 
     * @see Bucket#insert(Tuple)
     */
    protected void insert(Tuple tuple){

        ((Buffer)inMemoryTuples).add(tuple);
    }
    
//    /**
//     * Tenta realizar o match de uma tupla com alguma tupla que esteja no bucket.
//     * @return Conjunto de tuplas que realizaram o match; 
//     */
//    public Collection match(Tuple outerTuple) throws BucketNotLoadedException{
//        
//        Tuple innerTuple;
//        Vector matchedTuples[];
//        BucketIterator itBucket = this.iterator();
//        
//        while(itBucket.hasNext()){
//            
//            innerTuple = (Tuple)itBucket.next();
//            
//            if( joinPredicate.evaluate(outerTuple, innerTuple)
//                    matchedTuples.add();
//        }
//        
//    }

}