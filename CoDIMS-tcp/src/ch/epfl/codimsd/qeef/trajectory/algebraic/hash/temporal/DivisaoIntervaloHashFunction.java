package ch.epfl.codimsd.qeef.trajectory.algebraic.hash.temporal;


import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.trajectory.algebraic.hash.HashFunction;
import ch.epfl.codimsd.qeef.types.IntegerType;

//Recebe como parametro um intervalo de valores para o atributo
//   de jun��o. Os valores de cada Bucket ser�o faixas sequencias
//    do intervalo.

/**
 * 
 * digite aqui descricao do tipo 
 *
 * @author Vinicius Fontes
 */

public class DivisaoIntervaloHashFunction extends HashFunction {
	
	private int elementsPerBucket, start, end;
	private int columnNumber;
	
	public DivisaoIntervaloHashFunction(int nrBuckets, int start, int end, int columnNumber){
		this.start = start;
		this.end   = end;
		this.columnNumber = columnNumber;
		
		//Determina faixa de valores que ficar� em cada bucket
		//Isso � feito pelo nr de elementos de cada fatia do intervalo
		elementsPerBucket = ((end-start)+1)/nrBuckets;
	}

	public int[] assign(DataUnit instance) throws Exception{
		int bucketNumber;
		int value;
		int result[] = new int[1];
		Tuple tuple = (Tuple)instance;
		IntegerType tValue;
		
		if(instance == null)
		    logger.debug("Imposs�vel atribuir tupla null a algum bucket.");

                tValue = (IntegerType)tuple.getData(columnNumber);

		if(tValue ==null)
		    logger.warn("Coluna inexistente. Impossivel atribuir bucket.");
		
		value = tValue.intValue();
		
		result[0] = (value-start)/elementsPerBucket;
		return result;
	}


}
