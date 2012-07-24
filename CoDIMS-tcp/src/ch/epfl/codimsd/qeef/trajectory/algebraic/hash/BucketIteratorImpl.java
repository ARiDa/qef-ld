package ch.epfl.codimsd.qeef.trajectory.algebraic.hash;

import java.io.IOException;
import java.util.Iterator;

import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.datastructure.Estrutura;
import ch.epfl.codimsd.qeef.relational.Tuple;

public class BucketIteratorImpl implements BucketIterator{
	
	protected Bucket bucket;
	protected Iterator iterator;
	protected Estrutura elements;
        protected Instance instance[];
	
	/**
	 * Construtor padr�o.
	 * 
	 * @param Bucket sobre o qual deseja-se iterar.
	 * @param Estrutura de mem�ria utilizada ca carga do bucket.
	 */
	BucketIteratorImpl(Bucket bucket, Estrutura inMemory){
		this.bucket = bucket;
		this.elements = inMemory;	
		iterator  = elements.iterator();
	}
 

	/**
	 * Verifica se extiste mais inst�ncias a serem consumidas.
	 * A opera��o refresh do bucket � utilizada para realizar a carga de estrutura de mem�ria do bucket.
	 * 
	 * @result True se existir mais tuplas.
	 * 
	 * @throws IOException Se acontecer algum erro durante a leitura dos dados no mecanismo 
	 * de armazenamento secund�rio.
	 */
	public boolean hasNext() throws IOException{
		if( iterator.hasNext() == true)
			return true;
		else{
			if (!bucket.refresh())
				return false;
			iterator = elements.iterator();
			return iterator.hasNext();
		}
	}

	/**
	 * Obtem a proxima inst�ncia a ser consumida.
	 *
	 * @result Proxima inst�ncia.
	 */
	public Instance next() {
		return (Instance)iterator.next();
	}
}
