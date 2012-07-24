package ch.epfl.codimsd.qeef.trajectory.control;

import java.util.Properties;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.AsyncControlOperator;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.datastructure.Buffer;
import ch.epfl.codimsd.qeef.datastructure.Semaphore;
import ch.epfl.codimsd.qep.OpNode;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Operador de controle respons�vel por implementar processamento iterativo.
 * <p>
 * Os dados a serem processados de forma iterativa ser�o fornecidos pelo
 * primeiro produtor da lista de produtores, que � conhecido como produtor de
 * abstecimento. O segundo produtor, deve ser a raiz do fragmento a ser
 * executado de forma iterativa. O funcionamento deste operador pode ser
 * descrito pelas etapas descritas abaixo:
 * <ol>
 * <li>Inicialmente um conjunto de tamanho loadSize unidades de dado ser� lido
 * e disponibilizado para processamento. Ap�s reload unidades completarem o
 * processamento de todas as suas iterações uma nova carga de tamanho reload
 * ser� realizada. Esta pol�tica tem como objetivo evitar o problema conhecido
 * como buffer overflow onde o buffer fica "entupido" de dados e as tuplas
 * processadas n�o podem ser devolvidas ao buffer.
 * <li>O operador Eddy inicia o consumo de tuplas do fragmento de execução
 * iterativo, que por sua vez consumir� unidades de dado do Eddy. Uma c�pia da
 * unidade de dado procesada pelo fragmento e adicionada a um buffer de sa�da e
 * a inst�ncia original continua no ciclo at� ser processada um n�mero
 * maxDataUnitIterations de iterações.
 * <li>Um consumidor "final" consome as unidades de dados processadas pelo
 * processamento iterativo.
 * </ol>
 * 
 * Um exemplo de aplicação que requer este tipo de processamento � calculo de
 * trajet�ria de particula. Neste problema desejamos saber o caminho tra�ado por
 * uma part�cula em n iterações.
 * <p>
 * 
 * Nesta implementação os dados s�o consumidos de forma assincrona de seus
 * produtores, que devem ter a seguinte ordemseguinte ordem:
 * <ol>
 * <li>Produtor de Alimentação do sistema, possivelmente um operador scan.
 * <li>Raiz do fragmento de execução iterativo.
 * </ol>
 * J� os consumidores devem ter a seguinte ordem:
 * <ol>
 * <li>Produtor do fragmento que consumir� dados do Eddy.
 * <li>Consumidor final do processamento iterativo.
 * </ol>
 * 
 * Esta implementação suporta qualquer unidade de dados.
 * 
 * @author Vinicius Fontes.
 *  
 */

//Saida aleat�ria

//public class Eddy extends AsyncControlOperator {
//
//    /**
//     * Buffer com unidade de dados que ainda ser�o processadas. Ou seja, que
//     * continuam no ciclo.
//     */
//    private Buffer bfToProcess;
//
//    /**
//     * Indica quantas unidade de dados devem ser removidas do ciclo para que uma
//     * nova carga seja realizada.
//     */
//    private int reload;
//
//    /**
//     * Nr de unidade de dados que ser�o disponibilizadas no in�cio do
//     * processamento.
//     */
//    private int loadSize;
//
//    /**
//     * Nr m�ximo de iterac�es por unidade de dado.
//     */
//    private int maxDataUnitIterations;
//
//    /**
//     * Nr de unidade de dados removidas entre recargas. N�o considera as
//     * iterações de cada unidade de dado.
//     */
//    private int removedDatUnits;
//
//    /**
//     * Nr de unidade de dados a serem processadas. N�o considera as iterações de
//     * cada unidade de dado.
//     */
//    private int total;
//
//    /**
//     * Nr de unidade de dados a serem processadas x Nr de Iterações por unidade
//     * de dado.
//     */
//    private int totalIteration;
//
//    /**
//     * N�mero de unidade de dados que foram enviadas para o fragmento remoto
//     * para processar. Considera as iterações de cada tupla.
//     */
//    private int processedDataUnits;
//
//    /**
//     * Nr de unidade de dados removidas desde o in�cio do processamento. N�o
//     * considera as iterações de cada unidade de dadounidade de dados.
//     */
//    private int totalRemovedDataUnits;
//
//    /**
//     * Metadado do operador de abastecimento do sistema.
//     */
//    private Metadata systemInputMetadata;
//
//    /**
//     * Descreve o nome dado a propriedade que contabiliza as iterações de cada
//     * unidade de dado.
//     */
//    public static final String ITERATION_NUMBER = "ITERATION_NUMBER";
//
//    /**
//     * Controla a leitura de dados do operador que abastece o ciclo. N�o permite
//     * que todas as tuplas sejam inseridas de uma vez s�. Do contr�rio, o
//     * sistema pararia de funcionar.
//     */
//    private Semaphore smSystemInput;
//
//    /**
//     * Thread que realiza a carga do sistema com unidade de dados do produtor de
//     * abstecimento.
//     */
//    private ProdutorAlimentacaoSistema thrSystemInput;
//
//    /**
//     * Indica se o operador j� foi encerrado.
//     */
//    private boolean closed;
//
//    /**
//     * Indica se o operador j� foi inicializado.
//     */
//    private boolean opened;
//    
//    protected Logger logger;
//    
//    //*************
//    
//    private int outInstances;
//    
//    private Hashtable itOut;
//
//    /**
//     * Construtor padr�o.
//     * 
//     * @param id
//     *            Identificador do operador.
//     * @param blackBoard
//     *            Quadro de comunicação utlizado pelo operadores do plano.
//     * @param loadSize
//     *            Nr de unidade de dados disponibilizadas inicialmente para
//     *            processamento.
//     * @param reload
//     *            Nr de unidade de dados removidas do ciclo at� que se efetue
//     *            uma nova carga.
//     * @param maxDataUnitIterations
//     *            Nr m�ximo de iterações por unidade de dado.
//     * @param total
//     *            Nr de unidade de dados que se espera de input, independente do
//     *            nr de iterações.
//     */
//    public Eddy(int id, BlackBoard blackBoard, int loadSize, int reload,
//            int maxDataUnitIterations, int total) throws Exception {
//
//        super(id, blackBoard, 3 * loadSize);
//        
//        this.logger = Logger.getLogger("qeef.operator.control.eddy");
//
//        if (loadSize <= 0){
//        	logger.warn("EDDY: loadSize deve ser maior que zero.");
//            throw new Exception("EDDY: loadSize deve ser maior que zero.");
//        }
//
//        if (reload < 0 || reload > loadSize){
//        	logger.warn("EDDY: lowerLoadLevel deve ser maior igual a zero e menor que loadSize.");
//            throw new Exception(
//                    "EDDY: lowerLoadLevel deve ser maior igual a zero e menor que loadSize.");
//        }
//
//        if (maxDataUnitIterations < 1){
//        	logger.warn("EDDY: Numero minimo de iteracoes por unidade de dados e 1");
//            throw new Exception(
//                    "EDDY: Numero minimo de iteracoes por unidade de dados e 1");
//        }
//
//        this.loadSize = loadSize;
//        this.reload = reload;
//        this.maxDataUnitIterations = maxDataUnitIterations;
//        this.total = total;
//        this.totalIteration = total * maxDataUnitIterations;
//        this.opened = false;
//        this.closed = false;
//    }
//
//    /**
//     * Inicializa este produtor e seus produtores. Inicialmente ser�
//     * inicializado o produtor que abastece o sistema, e em seguida o fragmento
//     * a ser executado em ciclo. Ser� inicializada uma thread para cada um, ou
//     * seja, s�o processados de forma assincrona.
//     */
//    public void open() throws Exception {
//        Operator op;
//
//        if (!opened) {
//            opened = true;
//            removedDatUnits = 0;
//            processedDataUnits = 0;
//            smSystemInput = new Semaphore(loadSize);
//            blackBoard.put("EVOLUTION", new Float(1));
//
//            bfToProcess = new Buffer(3 * loadSize);
//
//            //Retira produtor de abstecimento para dar um tratamento especial
//            // a ele na inicialização, sen�o thread do Asincrono entende que ele � um frag. paralelo.
//            op = (Operator) producers.remove(0);
//            op.open();
//            systemInputMetadata = (Metadata) op.getMetadata(id).clone();
//            //Inicializa thread de abastecimento
//            thrSystemInput = new ProdutorAlimentacaoSistema(this, op,
//                    bfToProcess, smSystemInput);
//            thrSystemInput.start();
//
//            outInstances = 0;
//            
//            //prepara hash com saida das tuplas
//            //atualiza total de tuplas a serem produzidas
//            File home = (File)Config.getProperty("QEEF_HOME");
//            BufferedReader in = new BufferedReader( new FileReader( home + File.separator + "randsaida.txt" ) );
//            itOut = new  Hashtable();
//            totalIteration = 0;
//            for(int i=1; i <= total; i++){
//            	String aux = in.readLine().trim();
//            	totalIteration += Integer.parseInt(aux);
//            	itOut.put( new Integer(i), new Integer(aux) );
//            }                       
//            	
//            super.open();                      
//        }
//    }
//
//    /**
//     * Encerra este operador e seus produtores. Todas as thread inicializadas
//     * durante a etapa de open ser�o finalizadas.
//     * 
//     * @exception Se
//     *                acontecer algum erro no encerramento dos produtores.
//     */
//    public void close() throws Exception {
//
//    	logger.info("Eddy(" + id + "): close");
//    	
//        if (!closed) {
//            closed = true;
//            //fecha seus producers
//            super.close();
//
//            thrSystemInput.close();
//            thrSystemInput = null;
//
//            smSystemInput = null;
//            bfToProcess = null;
//        }
//    }
//
//    /**
//     * Obtem o metadado/formato das unidades de dados produzidas para este
//     * consumidor. O formato das unidades de dados produzidas para o fragmento
//     * de execução ser� o mesmo que o da fonte de dados. Enquato que o formato
//     * do consumidor final(que consome unidades de dados produzidas pelo
//     * processamento iterativo) ser� definido pelo formato das unidades de dados
//     * do �ltimo operador do fragmento que determina o ciclo.
//     */
//    public Metadata getMetadata(int consumerId) {
//
//        if (consumerId == getConsumer(getConsumers().size() - 1).getId()){
//            logger.debug("Metadados Enviado Eddy 1 " + metadata[0]);
//            return metadata[0];
//        } else {
//        	logger.debug("Metadados Enviado Eddy input " + systemInputMetadata);
//            return systemInputMetadata;
//        }
//    }
//
//    /**
//     * Obtem uma unidade de dado para este consumidor. Se for o consumidor
//     * final, ser� uma unidade de dado j� processada pelo fragmento de execução.
//     * Do contr�rio, ser� retornada uma unidade de dados vinda do produtor de
//     * abastecimento que ainda n�o processou todas as suas iterações.
//     * 
//     * @return Unidade de dado processada ou a ser processada.
//     * 
//     * @Exception Se
//     *                aconteceu algum erro durante o processamento de uma
//     *                unidade de dados pelo fragmento.
//     */
//    public DataUnit getNext(int consumerId) throws Exception {
//
//        if (consumerId == getConsumer(getConsumers().size() - 1).getId())
//            return super.getNext(consumerId);
//
//        if (processedDataUnits >= totalIteration) {
//        	logger.info("Eddy enviou null para operador " + consumerId);
//            return null;
//        } else {
//            processedDataUnits++;
//            return (DataUnit) bfToProcess.get();
//        }
//    }
//
//    /**
//     * Determina o percentual de unidades de dados n�o processadas. n�o
//     * considera as iterações de cada unidade de dado. Este valor � registrado
//     * na vari�vel de ambiente EVOLUTION do quadro de comunicação.
//     */
//    public void setEvolution() {
//        float perc = (1 - ((float) (totalRemovedDataUnits) / (float) total));
//        logger.info("Eddy: Percentual = " + perc);
//        blackBoard.put("EVOLUTION", new Float(perc));
//
//    }
//
//    /**
//     * Adiciona uma c�pia da unidade de dados processada pelo fragmento ao
//     * buffer de dados que � consumido pelo operador final.
//     * 
//     * @param dataUnit
//     *            Unidade de dado processada pelo fragmento.
//     */
//    int produzidasProj=0;
//    void addDataUnitBuffer(DataUnit dataUnit) {
//
//    	produzidasProj++;
//    	
//        DataUnit clonedDataUnit;
//
//        if (dataUnit != null) {
//            clonedDataUnit = (DataUnit) dataUnit.clone();
//            clonedDataUnit.removeProperty(ITERATION_NUMBER);
//            buffer.add(clonedDataUnit);
//
//        } //Thread de asyncControlOperator insere null
//    }
//
//    /**
//     * Determina se unidade de dados deve continuar a ser procesada. Se n�o
//     * completou o nr de iterações, uma c�pia da unidade de dados � adicionada
//     * ao buffer de dados a serem processados.
//     * 
//     * @param dataUnit
//     *            Unidade de dados processada pelo fragmento.
//     */
//    private void controlDataUnitInteration(DataUnit dataUnit) {
//
//        //Verifica se Tuple continua a fazer iteracoes
//        //se continua, clona ela, incrementa iteracao da Tuple clonada e coloca
//        // ela no primeiro buffer
//        //uma vez que ela ja foi adicionada ao buffer da projecao
//    	int id = ((Point)((Tuple)dataUnit).getData(0)).id;
//        int nrIterations = Integer.parseInt( dataUnit.getProperty(ITERATION_NUMBER) );
//        int max = ((Integer)itOut.get(new Integer(id))).intValue();
//
//        if ( nrIterations < max ) {
//
//            //clona Tuple
//            DataUnit newData = dataUnit;
//
//            //Incrementa iteracao
//            nrIterations++;
//            newData.setProperty(new Properties());
//            newData.setProperty(ITERATION_NUMBER, nrIterations + "");
//
//            bfToProcess.add(newData);
//
//        } else {
//
//            removedDatUnits++;
//            totalRemovedDataUnits++;
//
//            //Atualiza percentual Processado
//            setEvolution();
//
//            logger.info("Eddy(" + id + "); Out ; " + totalRemovedDataUnits + " ; tuplas enviadas ; " + processedDataUnits);
//
//            //Se buffer atingiu limite minimo solicita carga
//            if (removedDatUnits == reload) {
//                removedDatUnits = 0;
//                smSystemInput.release(reload);
//            }
//        }
//    }
//
//    /**
//     * Registra que um erro aconteceu durante o processamento de uma thread de
//     * execução. Na pr�xima requisição por dados a este operador esta excess�o
//     * ser� lan�ada.
//     * 
//     * exc Excess�o ocorrida.
//     */
//    void abort(Exception exc) {
//        abortReason = exc;
//    }
//
//    /**
//     * Implementação da interface do operador assincrono. Representa a thread
//     * que ir� consumir dados do fragmento de execução.
//     * 
//     * @param producer
//     *            Operador final do fragmento de execução iterativo.
//     * @param buffer
//     *            N�o utilizado. Ver m�todos addDataUnitBuffer(DataUnit) e
//     *            controlDataUnitIteration(DataUnit).
//     * 
//     * @throws Se
//     *             alguma excess�o ocorrer durante o processamento do fragmento.
//     */
//    public void consume(Operator producer, Buffer buffer) throws Exception {
//
//        DataUnit next;
//
//        next = (DataUnit) producer.getNext(id);
//
//        while (next != null && continueProcessing) {
//
//            addDataUnitBuffer(next);
//            controlDataUnitInteration(next);
//
//            next = (DataUnit) producer.getNext(id);
//        }
//    }
//
//    /**
//     * Atribui os metadados deste operador.
//     * O primeiro ser� o do proodutor de abastecimento, o segundo o do fragmento a ser processado.
//     * 
//     * @param prdMetadata Lista dos metadados dos produres.
//     */
//    public void setMetadata(Metadata prdMetadata[]){
//        
//        metadata = new Metadata[2];
//        this.metadata[0] = (Metadata)systemInputMetadata.clone();
//        this.metadata[1] = (Metadata)prdMetadata[0].clone();
//    }
//}


public class Eddy extends AsyncControlOperator {

    /**
     * Indica quantas unidade de dados devem ser removidas do ciclo para que uma
     * nova carga seja realizada.
     */
    private int reload;

    /**
     * Nr de unidade de dados que ser�o disponibilizadas no in�cio do
     * processamento.
     */
    private int loadSize;

    /**
     * Nr m�ximo de iterac�es por unidade de dado.
     */
    private int maxDataUnitIterations;

    /**
     * Nr de unidade de dados removidas entre recargas. N�o considera as
     * iterações de cada unidade de dado.
     */
    private int removedDatUnits;

    /**
     * Nr de unidade de dados a serem processadas. N�o considera as iterações de
     * cada unidade de dado.
     */
    private int total;

    /**
     * Nr de unidade de dados a serem processadas x Nr de Iterações por unidade
     * de dado.
     */
    private int totalIteration;

    /**
     * N�mero de unidade de dados que foram enviadas para o fragmento remoto
     * para processar. Considera as iterações de cada tupla.
     */
    private int processedDataUnits;

    /**
     * Nr de unidade de dados removidas desde o in�cio do processamento. N�o
     * considera as iterações de cada unidade de dadounidade de dados.
     */
    private int totalRemovedDataUnits;

    /**
     * Metadado do operador de abastecimento do sistema.
     */
    private Metadata systemInputMetadata;

    /**
     * Descreve o nome dado a propriedade que contabiliza as iterações de cada
     * unidade de dado.
     */
    public static final String ITERATION_NUMBER = "ITERATION_NUMBER";

    /**
     * Controla a leitura de dados do operador que abastece o ciclo. N�o permite
     * que todas as tuplas sejam inseridas de uma vez s�. Do contr�rio, o
     * sistema pararia de funcionar.
     */
    private Semaphore smSystemInput;

    /**
     * Thread que realiza a carga do sistema com unidade de dados do produtor de
     * abstecimento.
     */
    private ProdutorAlimentacaoSistema thrSystemInput;

    /**
     * Indica se o operador j� foi encerrado.
     */
    private boolean closed;

    /**
     * Indica se o operador j� foi inicializado.
     */
    private boolean opened;
    
    protected Logger logger;

    private BlackBoard blackBoard;

     /**
     * Lista com unidade de dados que ainda ser�o processadas. Ou seja, que
     * continuam no ciclo.
     */
    private PriorityQueue<DataUnit> priorityQueue;
   // private Buffer bfToProcess;

    private int executionMode;
    //*************
    
    private int iteration;
    private int count;
    
    /**
     * Construtor padr�o.
     * 
     * @param id
     *            Identificador do operador.
     * @param blackBoard
     *            Quadro de comunicação utlizado pelo operadores do plano.
     * @param loadSize
     *            Nr de unidade de dados disponibilizadas inicialmente para
     *            processamento.
     * @param reload
     *            Nr de unidade de dados removidas do ciclo at� que se efetue
     *            uma nova carga.
     * @param maxDataUnitIterations
     *            Nr m�ximo de iterações por unidade de dado.
     * @param total
     *            Nr de unidade de dados que se espera de input, independente do
     *            nr de iterações.
     */
    public Eddy(int id, OpNode opNode) throws Exception {

        super(id, 3 * Integer.parseInt(opNode.getParams()[0]));

        blackBoard = BlackBoard.getBlackBoard();
        
        this.logger = Logger.getLogger(Eddy.class.getName());

        this.loadSize = Integer.parseInt(opNode.getParams()[0]);
        this.reload = Integer.parseInt(opNode.getParams()[1]);
        this.maxDataUnitIterations = Integer.parseInt(opNode.getParams()[2]);
        this.total = Integer.parseInt(opNode.getParams()[3]);
        this.totalIteration = total * maxDataUnitIterations;

        //Execution Mode = 0 ---- FTF (First Tuple First)
        //Execution Mode = 1 ---- FIF (First Iteration First)
        //Execution Mode = 2 ---- Free
        this.executionMode = Integer.parseInt(opNode.getParams()[4]);

        this.opened = false;
        this.closed = false;

        if (loadSize <= 0){
            logger.warn("EDDY: loadSize deve ser maior que zero.");
            throw new Exception("EDDY: loadSize deve ser maior que zero.");
        }

        if (reload < 0 || reload > loadSize){
        	logger.warn("EDDY: lowerLoadLevel deve ser maior igual a zero e menor que loadSize.");
            throw new Exception(
                    "EDDY: lowerLoadLevel deve ser maior igual a zero e menor que loadSize.");
        }

        if (maxDataUnitIterations < 1){
        	logger.warn("EDDY: Numero minimo de iteracoes por unidade de dados e 1");
            throw new Exception(
                    "EDDY: Numero minimo de iteracoes por unidade de dados e 1");
        }
    }

    /**
     * Inicializa este produtor e seus produtores. Inicialmente ser�
     * inicializado o produtor que abastece o sistema, e em seguida o fragmento
     * a ser executado em ciclo. Ser� inicializada uma thread para cada um, ou
     * seja, s�o processados de forma assincrona.
     */
    public void open() throws Exception {
        Operator op;
        
        this.iteration = 1;
        this.count = total;

        if (!opened) {
            opened = true;
            removedDatUnits = 0;
            processedDataUnits = 0;
            smSystemInput = new Semaphore(loadSize);
            blackBoard.put("EVOLUTION", new Float(1));

            if(executionMode == 0)
            {
                priorityQueue = new PriorityQueue<DataUnit>(1, new Comparator<DataUnit>() {
                    public int compare (DataUnit p1, DataUnit p2) {
                            if(Integer.parseInt(p2.getProperty(ITERATION_NUMBER)) > Integer.parseInt(p1.getProperty(ITERATION_NUMBER)))
                                return 1;
                            if(Integer.parseInt(p2.getProperty(ITERATION_NUMBER)) < Integer.parseInt(p1.getProperty(ITERATION_NUMBER)))
                                return -1;
                            return 0;
                    }
                });
            }
            else if(executionMode == 1)
            {
                priorityQueue = new PriorityQueue<DataUnit>(1, new Comparator<DataUnit>() {
                    public int compare (DataUnit p1, DataUnit p2) {
                            if(Integer.parseInt(p2.getProperty(ITERATION_NUMBER)) > Integer.parseInt(p1.getProperty(ITERATION_NUMBER)))
                                return -1;
                            if(Integer.parseInt(p2.getProperty(ITERATION_NUMBER)) < Integer.parseInt(p1.getProperty(ITERATION_NUMBER)))
                                return 1;
                            return 0;
                    }
                });
            }
            else
            {
                priorityQueue = new PriorityQueue<DataUnit>(1, new Comparator<DataUnit>() {
                    public int compare(DataUnit p1, DataUnit p2) {
                        return 0;
                    }
                });
            }

            //Retira produtor de abstecimento para dar um tratamento especial
            // a ele na inicialização, sen�o thread do Asincrono entende que ele � um frag. paralelo.
            op = (Operator) producers.remove(0);
            op.open();

            systemInputMetadata = (Metadata) op.getMetadata(id).clone();
            //Inicializa thread de abastecimento
            thrSystemInput = new ProdutorAlimentacaoSistema(this, op, priorityQueue, smSystemInput);
            //thrSystemInput = new ProdutorAlimentacaoSistema(this, op, bfToProcess, smSystemInput);
            thrSystemInput.start();
            	
            super.open();          

        }
    }

    /**
     * Encerra este operador e seus produtores. Todas as thread inicializadas
     * durante a etapa de open ser�o finalizadas.
     * 
     * @exception Se
     *                acontecer algum erro no encerramento dos produtores.
     */
    public void close() throws Exception {

    	logger.info("Eddy(" + id + "): close");
    	
        if (!closed) {
            closed = true;
            //fecha seus producers
            super.close();

            thrSystemInput.close();
            thrSystemInput = null;

            smSystemInput = null;
            priorityQueue = null;
        }
    }

    /**
     * Obtem o metadado/formato das unidades de dados produzidas para este
     * consumidor. O formato das unidades de dados produzidas para o fragmento
     * de execução ser� o mesmo que o da fonte de dados. Enquato que o formato
     * do consumidor final(que consome unidades de dados produzidas pelo
     * processamento iterativo) ser� definido pelo formato das unidades de dados
     * do �ltimo operador do fragmento que determina o ciclo.
     */
    public Metadata getMetadata(int consumerId) {
        // eddy nao paralelizado
        if (consumerId == getConsumer(getConsumers().size() - 1).getId()){
              logger.debug("Metadados Enviado Eddy input " + systemInputMetadata);
              return systemInputMetadata;
        } else {
            logger.debug("Metadados Enviado Eddy 1 " + metadata[0]);
            return metadata[0];
        }
    }

    /**
     * Obtem uma unidade de dado para este consumidor. Se for o consumidor
     * final, ser� uma unidade de dado j� processada pelo fragmento de execução.
     * Do contr�rio, ser� retornada uma unidade de dados vinda do produtor de
     * abastecimento que ainda n�o processou todas as suas iterações.
     * 
     * @return Unidade de dado processada ou a ser processada.
     * 
     * @Exception Se
     *                aconteceu algum erro durante o processamento de uma
     *                unidade de dados pelo fragmento.
     */
    public DataUnit getNext(int consumerId) throws Exception {

        //eddy não paralelizado
        if (consumerId != getConsumer(getConsumers().size() - 1).getId())
            return super.getNext(consumerId);

        if (processedDataUnits >= totalIteration) {
        	logger.info("Eddy enviou null para operador " + consumerId);
            return null;
        } else {
            processedDataUnits++;

            DataUnit dt = null,dtTest = null;
            
            int countSleep = 0;
            if(this.executionMode != 1)
            {
                while(priorityQueue.size() == 0)
                {
                    countSleep++;
                    Thread.sleep(1000);
                    if(countSleep == 100)
                        break;
                }
                dt = (DataUnit) priorityQueue.poll();
                //System.out.println(" dt = "+ Integer.parseInt(dt.getProperty(ITERATION_NUMBER)));
            }
            else
            {
                while(priorityQueue.size() == 0)
                {
                    countSleep++;
                    Thread.sleep(1000);
                    if(countSleep == 100) 
                        break;
                }

                while(dt == null)
                {
                    dtTest = (DataUnit) priorityQueue.peek();
                    System.out.println("count = "+ count + "iteration = " + iteration + " dt = "+ Integer.parseInt(dtTest.getProperty(ITERATION_NUMBER)));

                    if(Integer.parseInt(dtTest.getProperty(ITERATION_NUMBER)) == iteration)
                    {
                         dt = (DataUnit) priorityQueue.poll();
                         count--;
                         if(count == 0)
                         {
                               count = total;
                               iteration =  iteration + 1;
                         }
                    }
                    if(dt == null)
                         Thread.sleep(1000);
                }
            }
            return dt;
        }
    }

    /**
     * Determina o percentual de unidades de dados n�o processadas. n�o
     * considera as iterações de cada unidade de dado. Este valor � registrado
     * na vari�vel de ambiente EVOLUTION do quadro de comunicação.
     */
    public void setEvolution() {
        float perc = (1 - ((float) (totalRemovedDataUnits) / (float) total));
        logger.info("Eddy: Percentual = " + perc);
        blackBoard.put("EVOLUTION", new Float(perc));
    }

    /**
     * Adiciona uma c�pia da unidade de dados processada pelo fragmento ao
     * buffer de dados que � consumido pelo operador final.
     * 
     * @param dataUnit
     *            Unidade de dado processada pelo fragmento.
     */
    int produzidasProj=0;
    void addDataUnitBuffer(DataUnit dataUnit) {

    	produzidasProj++;
    	
        DataUnit clonedDataUnit;

        if (dataUnit != null) {
            clonedDataUnit = (DataUnit) dataUnit.clone();
            clonedDataUnit.removeProperty(ITERATION_NUMBER);
            buffer.add(clonedDataUnit);

        } //Thread de asyncControlOperator insere null
    }

    /**
     * Determina se unidade de dados deve continuar a ser procesada. Se n�o
     * completou o nr de iterações, uma c�pia da unidade de dados � adicionada
     * ao buffer de dados a serem processados.
     * 
     * @param dataUnit
     *            Unidade de dados processada pelo fragmento.
     */
    private void controlDataUnitInteration(DataUnit dataUnit) {

        //Verifica se Tuple continua a fazer iteracoes
        //se continua, clona ela, incrementa iteracao da Tuple clonada e coloca
        // ela no primeiro buffer
        //uma vez que ela ja foi adicionada ao buffer da projecao
        int nrIterations = Integer.parseInt(dataUnit.getProperty(ITERATION_NUMBER));

        if (nrIterations < maxDataUnitIterations) {

            //clona Tuple
            DataUnit newData = dataUnit;

            //Incrementa iteracao
            nrIterations++;
            newData.setProperty(new Properties());
            newData.setProperty(ITERATION_NUMBER, nrIterations + "");

            priorityQueue.add(newData);
        } else {

            removedDatUnits++;
            totalRemovedDataUnits++;

            //Atualiza percentual Processado
            setEvolution();

            logger.info("Eddy(" + id + "); Out ; " + totalRemovedDataUnits + " ; tuplas enviadas ; " + processedDataUnits);

            //Se buffer atingiu limite minimo solicita carga
            if (removedDatUnits == reload) {
                removedDatUnits = 0;
                smSystemInput.release(reload);
            }
        }
    }

    /**
     * Registra que um erro aconteceu durante o processamento de uma thread de
     * execução. Na pr�xima requisição por dados a este operador esta excess�o
     * ser� lan�ada.
     * 
     * exc Excess�o ocorrida.
     */
    void abort(Exception exc) {
        abortReason = exc;
    }

    /**
     * Implementação da interface do operador assincrono. Representa a thread
     * que ir� consumir dados do fragmento de execução.
     * 
     * @param producer
     *            Operador final do fragmento de execução iterativo.
     * @param buffer
     *            N�o utilizado. Ver m�todos addDataUnitBuffer(DataUnit) e
     *            controlDataUnitIteration(DataUnit).
     * 
     * @throws Se
     *             alguma excess�o ocorrer durante o processamento do fragmento.
     */
    public void consume(Operator producer, Buffer buffer) throws Exception {

        DataUnit next;

        next = (DataUnit) producer.getNext(id);

        while (next != null && continueProcessing) {

            addDataUnitBuffer(next);
            controlDataUnitInteration(next);

            next = (DataUnit) producer.getNext(id);
        }
    }

    /**
     * Atribui os metadados deste operador.
     * O primeiro ser� o do proodutor de abastecimento, o segundo o do fragmento a ser processado.
     * 
     * @param prdMetadata Lista dos metadados dos produres.
     */
    public void setMetadata(Metadata prdMetadata[]){
        
        metadata = new Metadata[2];
        this.metadata[0] = (Metadata)systemInputMetadata.clone();
        this.metadata[1] = (Metadata)prdMetadata[0].clone();
    }
}

////-----------------------------------------------------------------------------------------
////-----------------------------------------------------------------------------------------
//boolean loadTuples(int tuplesToLoad) throws Exception {
//    
//    Operator scan = getProducer(0);
//    
//    Tuple t = null;
//    int dif, aux, loadSize;
//    
//    for (; tuplesToLoad > 0 &&
//           buffer[0].size() < buffer[0].capacity() &&
//           (t = (Tuple)scan.getNext(id)) != null;
//         tuplesToLoad--) {
//
//        //Prepara Tuple
//        
//        //Insere propriedade de controle utilizada pelo eddy
//        
//
//        //Atualiza nr de iteracoes/tuplas a serem processadas
//        totalIterations += maxDataUnitIterations; //Atualiza nr iteracoes sugeridas
// para
// eddy
//        buffer[0].add(t);
//        
//        loadedTuples++;
//        //System.out.println( t.toString() );
//    }
//    
//
//    if(t == null){
//        buffer[0].add(t);
//        produtorHasNext = false;
//        System.out.println("EDDY nao existem mais tuplas na fonte de dados");
//        return false;
//    } else {
//        return true;
//    }
//
