package ch.epfl.codimsd.qeef.trajectory.control;

import ch.epfl.codimsd.qeef.DataUnit;
import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.datastructure.Buffer;
import ch.epfl.codimsd.qeef.datastructure.Semaphore;
import java.util.PriorityQueue;

class ProdutorAlimentacaoSistema extends Thread {

	private boolean continueProcessing;

	private Operator producer;

	private Semaphore smSystemInput;

        private PriorityQueue<DataUnit> inputBufer;

        //private Buffer inputBufer;

	private Eddy eddy;

	private Logger logger;

	ProdutorAlimentacaoSistema(Eddy eddy, Operator producer, PriorityQueue inputBufer,
			Semaphore smSystemInput) {
	//ProdutorAlimentacaoSistema(Eddy eddy, Operator producer, Buffer inputBufer,
	//		Semaphore smSystemInput) {

		super("EddyAlmt");
		
		this.eddy = eddy;
		this.producer = producer;
                this.inputBufer = inputBufer;
		this.smSystemInput = smSystemInput;
		this.continueProcessing = true;
		this.logger = Logger.getLogger("qeef.operator.eddy.input");
	}

	/**
	 * Realiza a leitura de inst�ncias do produtor que abstece o ciclo. O n�mero
	 * de tuplas lidas
	 * 
	 * @param producer
	 *            Produtor que abastece o ciclo.
	 * @param buffer
	 *            Buffer onde tuplas s�o armazenadas.
	 * @param smSystemInnput
	 *            Semaforo que sincroniza o abastecimento com a produ��o.
	 */

	public void run() {

		int carregadas = 0;
		int id = eddy.getId();

		try {

			Instance next;
			next = (Instance) producer.getNext(id);

			logger.info("Eddy tera carga inicial de:"+ smSystemInput.permits());

			while (next != null && continueProcessing) {
				smSystemInput.acquire();
				next.setProperty(Eddy.ITERATION_NUMBER, "1");
				if (continueProcessing) {
					synchronized (this) {
                                                //System.out.println("next = " + next);
                                                inputBufer.add(next);
						carregadas++;
						next = (Instance) producer.getNext(id);
					}
					logger.debug("Produtor de Abastecimento Eddy inseriu inst�ncia buffer. unidade de dado nr " + carregadas);
				}
			}

			logger.info("Eddy carregou " + carregadas + " unidades de dados. Nao existe mais tuplas a serem carregadas.");

		} catch (Exception exc) {
			eddy.abort(exc);
		}
	}

	synchronized void close() throws Exception {
		continueProcessing = false;
		smSystemInput.release();
		producer.close();
		producer = null;
                inputBufer = null;
		smSystemInput = null;
	}
}
