
package ch.epfl.codimsd.exceptions.dataSource;

/**
 * Exce��o a ser utilizada quando se tenta instanciar a m�quina com um plano e o Gerente de Plano n�o foi definido. 
 *
 * @author Vinicius Fontes
 * 
 * @date Jun 18, 2005
 */
public class UndefinedPlanManagerException extends Exception {
    
    /**
     * Construtor padr�o.
     * @param msg de Erro.
     */
    public UndefinedPlanManagerException(String msg) {

        super(msg);
    }

}
