package ch.epfl.codimsd.qeef.trajectory.algebraic.hash;


/**
 * Informa que um bucket n�o pode ser utilizado pois ainda n�o foi carregado em mem�ria. 
 *
 * @author Vinicius Fontes
 */
public class BucketNotLoadedException extends Exception {

    public BucketNotLoadedException(String message){
        super(message);
    }
}
