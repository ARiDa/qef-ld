package ch.epfl.codimsd.qeef.trajectory;

import ch.epfl.codimsd.exceptions.operator.OperatorInitializationException;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;
import java.util.Hashtable;

import java.util.logging.Level;
import org.apache.log4j.Logger;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.exceptions.initialization.InitializationException;
import ch.epfl.codimsd.exceptions.initialization.QEPInitializationException;
import ch.epfl.codimsd.exceptions.operator.OperatorException;
import ch.epfl.codimsd.exceptions.optimizer.OptimizerException;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.PlanManager;
import ch.epfl.codimsd.qeef.SystemConfiguration;
import ch.epfl.codimsd.qeef.operator.OperatorFactoryManager;
import ch.epfl.codimsd.qeef.operator.modules.ModFix;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qep.OpNode;
import ch.epfl.codimsd.qep.QEP;
import ch.epfl.codimsd.qep.QEPFactory;
import ch.epfl.codimsd.query.Request;
import ch.epfl.codimsd.query.RequestParameter;


/**
 * Define uma implementa��o para o gerente de plano baseado em arquivo texto.
 * Os planos devem ter o seguinte formato:
 * <ol>
 * <li> N�mero de operadores no plano
 * <li> Defini��o de um operador. ID NOME;[parametro[;parametro]*]
 * <li> Numero de m�dulos(define tipo de link entre operadores).
 * <li> Nome Modulo;Operador;Produtor[;Produtor]
 * <ol> 
 *
 * <b>OBS:</b> Os operadores definidos no plano devem ser reconhecidos pela inst�ncia da fabrica de operadores utilizada.
 *
 * @author Vinicius Fontes
 */
public class PlainFilePlanManager implements PlanManager {

    /**
	 * Hashtable containing the operators.
	 */
	private Hashtable<Integer, Operator> operatorHashtable;

	/**
	 * The OperatorFactoryManager object used to create the operators.
	 */
	private OperatorFactoryManager operatorFactoryManager;

	/**
	 * Default constructor.
	 *
	 * @param operatorFactoryManager
	 */
	public PlainFilePlanManager(OperatorFactoryManager operatorFactoryManager) {

		this.operatorFactoryManager = operatorFactoryManager;
	}

	/**
	 * Log4j logger
	 */
	private static Logger logger = Logger.getLogger(PlainFilePlanManager.class.getName());

	/**
	 * Instancia o plano de execu��o deinido em planReader. Cria os operadores, m�dulos e liga os consumidores/produtores.
	 * 
	 * @param planReader Fluxo de leitura para o plano a ser instanciado.
	 * @param dsManager Gerente de fonte de dados utilizado nesta inst�ncia da m�quina de execu��o.
	 * 
	 * @return Operadores do plano instanciados e interligados. Operadores est�o indexados pelo seu id.
	 * 
	 * @throws IOException Se acontecer algum problema durante a leitura do plano.
	 * @throws Exception Se acontecer algum problema durante a cria��o de um operador.
	 */
         
        public QEP instantiatePlan(String qepString) throws OperatorException, InitializationException, OperatorInitializationException, PredicateEvaluatorException {


		QEP qepInitial = null;

                System.out.println("QEP = " + qepString);

		try {

			qepInitial = new QEP();
			qepInitial.setOpList(QEPFactory.
				loadQEP(qepString,Constants.qepAccessTypeRemote, qepInitial));

		} catch (QEPInitializationException ex) {
			ex.printStackTrace();
			throw new InitializationException("Cannot load the remote QEP : " + ex.getMessage());
		}

		buildConcretePlan(qepInitial);
		qepInitial.setConcreteOpList(operatorHashtable);

		return qepInitial;
	}


        public QEP instantiatePlan(Request request) throws InitializationException, OptimizerException, OperatorException, OperatorInitializationException {

            // long codimsPlanConstructionTime = System.currentTimeMillis();

            System.out.println("PlanFilePlan");
		QEP qepInitial = new QEP();
		qepInitial.existRemote = false;

		try {

			String qepInitialFile = QEPFactory.getQEP(request.getRequestType(), 0);
			qepInitial.setOpList(QEPFactory.
				loadQEP(qepInitialFile,Constants.qepAccessTypeLocal, qepInitial));

		} catch (CatalogException ex) {
			throw new InitializationException("InitializationException : " + ex.getMessage());
		}

		// Call the DiscoveryOptimizer if the flag NO_DISTRIBUTION in the requestParameter is set to FALSE
                //String noDistributionFromConfigFile =(String) request.getRequestParameter().getParameter(Constants.NO_DISTRIBUTION);
		String noDistributionFromConfigFile = SystemConfiguration.getSystemConfigInfo(Constants.NO_DISTRIBUTION);
		if (noDistributionFromConfigFile != null) {

			if (!noDistributionFromConfigFile.equalsIgnoreCase("TRUE")) {
				TCPOptimizer tcpOptimizer = new TCPOptimizer(request);
				qepInitial.existRemote = tcpOptimizer.optimize(qepInitial);
			}

		} else {

			RequestParameter requestParameter = request.getRequestParameter();
			if (requestParameter.containsKey(Constants.NO_DISTRIBUTION)) {
				String noDistribution = (String) requestParameter.getParameter(Constants.NO_DISTRIBUTION);
				if (!noDistribution.equalsIgnoreCase("TRUE")) {
					TCPOptimizer tcpOptimizer = new TCPOptimizer(request);
					qepInitial.existRemote = tcpOptimizer.optimize(qepInitial);
                                }
			} else {
				TCPOptimizer tcpOptimizer = new TCPOptimizer(request);
				qepInitial.existRemote = tcpOptimizer.optimize(qepInitial);
			}
		}

		// Build the operators if there is no remote node
		if (qepInitial.existRemote == false) {
                    try {
                        buildConcretePlan(qepInitial);
                        qepInitial.setConcreteOpList(operatorHashtable);
                    } catch (PredicateEvaluatorException ex) {
                        java.util.logging.Logger.getLogger(PlainFilePlanManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
		}

		// logger.debug("codimsPlanConstructionTime : " + (System.currentTimeMillis() - codimsPlanConstructionTime));

		return qepInitial;
        }

        private  void buildConcretePlan(QEP qep) throws OperatorException, OperatorInitializationException, PredicateEvaluatorException {

		long codimsBuildingOpTime = System.currentTimeMillis();

		operatorHashtable = new Hashtable<Integer, Operator>();

		for (int i = 1; i <= qep.getOperatorList().size(); i++) {

			OpNode opNode = (OpNode) qep.getOperatorList().get(i+"");

			if (opNode != null) {

				Operator op = operatorFactoryManager.createOperator(opNode);
				operatorHashtable.put(new Integer(op.getId()), op);
			}
		}

                ModFix modFix = null;
                boolean crioumodfix = false;
                
		// create producer-consumer relations using ModFix class
		for (int i = 1; i <= qep.getOperatorList().size(); i++) {

			OpNode opNode = (OpNode) qep.getOperatorList().get(i+"");

			if (opNode != null) {

				Operator op = (Operator)operatorHashtable.get(new Integer(i));

				if (opNode.getProducerIDs()[0] != 0) {

					int[] intProducers = opNode.getProducerIDs();

					for (int j = 0; j< intProducers.length; j++) {

						Operator producer = (Operator)operatorHashtable.get(new Integer(intProducers[j]));
                                                if (!crioumodfix)
                                                {
							modFix = new ModFix(op, producer);
							crioumodfix = true;
						} else
                                                        modFix.adicionar(op, producer);
					}
				}
			}
		}

		// logger.debug("codimsBuildingOpTime : " + (System.currentTimeMillis() - codimsBuildingOpTime));
	}
}
