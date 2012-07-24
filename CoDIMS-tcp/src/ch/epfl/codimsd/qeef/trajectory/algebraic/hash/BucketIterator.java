package ch.epfl.codimsd.qeef.trajectory.algebraic.hash;

import java.io.IOException;

import ch.epfl.codimsd.qeef.Instance;

/**
 * Define uma interface para o consumo de inst�ncias de um bucket.
 *
 * @author Vinicius Fontes
 * 
 * @date Jun 20, 2005
 */
public interface BucketIterator {
    
    /**
     * 
     * Verifica se o bucket tem mais inst�ncias a serem consumidas.
     * 
     * @return true Se existir mais tuplas. False caso contr�rio.
     */
    public abstract boolean hasNext() throws IOException;

    /**
     * 
     * Pr�xima inst�ncia a ser consumida.
     * 
     * @return Pr�xima inst�ncia.
     */
    public abstract Instance next();
}