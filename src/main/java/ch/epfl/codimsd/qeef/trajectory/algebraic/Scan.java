package ch.epfl.codimsd.qeef.trajectory.algebraic;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Access;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataSourceManager;
import ch.epfl.codimsd.qep.OpNode;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Predicate;
import ch.epfl.codimsd.qeef.relational.*;

/**
 * Realiza a leitura completa de uma fonte de dados retornando 
 * inst�ncias que atendam ao predicado de sele��o.
 * <p>
 * As mensagens de log deste operador podem ser configuradas pela classe de log "qeef.operator.scan".
 * 
 * @author Fausto ayres, Vinicius Fontes.
 */
public class Scan extends Access {
    
	/**
	 * Predicado de sele��o a ser aplicado sobre as tuplas recuperadas.
	 */
	protected Predicate predicate;
	
	/**
	 * Componente de log utilizado.
	 */
	protected Logger logger;

	/**
	 * Construtor padr�o.
	 * @param id Identificador deste operador.
	 * @param blackBoard Quadro de comunica��o utilizado pelos operadores.
	 * @param dataSource Fonte de dados que ser� lida.
	 * @param predicate Predicado de sele��o a ser aplicado pelas inst�ncias recuperadas.
	 */
	public Scan(int id, OpNode op, Predicate predicate) {
	    
	    super(id);
		
	    this.logger = Logger.getLogger(Scan.class.getName());
	    this.predicate = predicate;

            // Get the DataSource of this Scan operator.
	    DataSourceManager dsManager = DataSourceManager.getDataSourceManager();
	    dataSource = (DataSource) dsManager.getDataSource(op.getOpTimeStamp());
	}
	
	/**
	 * Realiza a inicializa��o da fonte de dados utilizada e define o formato das inst�ncias 
	 * retornadas por este operador. O formato das inst�ncias ser� o mesmo que os da fonte de dados.
	 * 
	 * @throws java.io.IOException Se algum erro acontecer durante a inicializa��o da fonte de dados. 
	 */
	public void open() throws Exception{
	    logger.debug("Scan open");
	    super.open();
	} 

	/**
	 * Retorna a pr�xima inst�ncia da fonte de dados que atenda ao predicado de sele��o definido.
	 * @param consumerId Identificador do consumidor.
	 * 
	 * @return Pr�xima inst�ncia que atenda ao predicado ou null se n�o existir mais inst�ncias.
	 * 
	 * @throws QEEF.predicate.evaluator.PredicateEvaluatorException Se acontecer algum erro durante a avalia��o do predicado de sele��o.
	 * @throws Exception Se acontecer alguma problema durante a leitura da fonte de dados.
	 */
	public DataUnit getNext(int consumerId) throws Exception{

           // System.out.println("SCANNext = " + id + "consumerId"+ consumerId);
	    instance = (dataSource).read();
	    //logger.debug(instance);

	    //avaliar o predicado
	    while( instance != null ) {
	        
			if ( predicate != null ) {			    
				if (predicate.evaluate( (Tuple)instance, null))
					break;;
			} else{
			    break;
			}
			//System.out.println("dsName = " + dataSource.getAlias() +  "instance = " + instance);
			instance = (dataSource).read();
	    }

            //System.out.println("SCAN --- dsName = " + dataSource.getAlias() +  "instance = " + instance);
            return instance;
	}
}
