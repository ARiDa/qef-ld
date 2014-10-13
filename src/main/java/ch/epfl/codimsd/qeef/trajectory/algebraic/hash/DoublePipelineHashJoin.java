package ch.epfl.codimsd.qeef.trajectory.algebraic.hash;

import java.io.IOException;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.datastructure.Heap;
import ch.epfl.codimsd.qeef.datastructure.Semaphore;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qep.OpNode;

// Outer Relation - producers[0]
// Inner Relation - producers[1]

/**
 * Implementação do algortimo de hash double pipeline hash join. Este algortimo
 * realiza apenas o particionamento da relação inner durante a inicialização. O
 * particionamento da relação outer é realizado após a etapa de inicialização
 * por uma thread independente. Esta thread particiona um conjunto de dados da
 * relação outer e aguarda até que este subconjunto de tuplas tenham sido
 * processadas para realizar o particionamento de um novo conjunto. Isso evita
 * que as instâncias particionadas tenham que ser gravadas em disco pelo bucket
 * e desta forma minimiza o nr de I/Os.
 * <p>
 * Os buckets da relação outer são organizados em uma lista ordenada
 * decrescentemente pelo nr de tuplas que estão no bucket e ainda não foram
 * processadas.
 * 
 * @author Vinicius Fontes.
 */

public abstract class DoublePipelineHashJoin extends HashJoin implements
        Runnable {


    /**
     * Indica se thread de particionamento deve continuar processando.
     */
    private boolean continueProcessing;

    /**
     * Semáforo utilizado para sincronizar thread de particionamento da relação
     * outer com thread de match.
     */
    private Semaphore sync;

    /**
     * Número de tuplas particionadas da relação inner até que a operação de flush
     * dos buckets seja efetuada.
     */
    private int innerFlushFrequency;

    /**
     * Nr de tuplas da relação outer que devem ser partcionadas em cada
     * intervalo.
     */
    private int partitionBlockSize;

    /**
     * Heap que definirá qual bucket a ser carregado. Segundo nr de tuplas em cada bucket.
     */
    private Heap bucketsPriorityQueue;

    /**
     * Bucket dummy utilizado para sinalizar o final do particionamento da
     * relação outer.
     */
    protected Bucket dummyBucket;

    /**
     * Contrutor padrão.
     * 
     * @param id
     *            Identificador do operador.
     * @param blackBoard
     *            Quadro de comunicação utilizado pelos operadores de um plano.
     * @param nrBuckets
     *            Número de buckets que será utilizado para realizar o
     *            particionamento das relaçães. Será utilizado o mesmo nr de
     *            buckets para o particionamento de ambas as relaçães.
     * @param outerBucketSize
     *            Nr de tuplas da relação outer que podem estar em memória
     *            simultaneamente.
     * @param innerBucketSize
     *            Nr de tuplas da relação inner que podem estar em memória
     *            simultaneamente.
     * @param partitionBlockSize
     *            Nr de tuplas da relação outer que podem ser particionadas em
     *            cada intervalo(partição/macth).
     * @param innerFlushFrequency
     *            Nr de tuplas da relação inner que podem ser particionadas sem
     *            que sejam gravadas em disco. Indica espaço de memória livre
     *            para fase de particionamento da relação inner.
     * 
     * @param joinPredicate
     *            Predicado de junção a ser utilizado.
     */
    public DoublePipelineHashJoin(int id, OpNode op) {

        super(id, op);

        this.innerFlushFrequency = Integer.parseInt(op.getParams()[3]);
        this.partitionBlockSize = Integer.parseInt(op.getParams()[4]);

        sync = new Semaphore(partitionBlockSize);
    }

    /**
     * Realiza a inicialização deste operador e de sus produtores. 
     * Durante a inicialização será realizado o particionamento da relação
     * inner e depois será criada uma thread que particionará a relação inner.
     * 
     * @throws IOException
     *             Se houver algum problema na instanciação dos buckets.\
     * @throws Exception
     *             Se houver algum problema na inicialização dos seus
     *             produtores.
     */
    public void open() throws IOException, Exception {

        //executa open de seus producers
        super.open();

        //Inicializa buckets e fila de prioridade usada no escalonamento dos
        // buckets
        bucketsPriorityQueue = new Heap(nrBuckets);

        //Inicia particionamento das relaçães
        this.continueProcessing = true;

        //Particiona relação inner
        hashRelation(super.getProducer(INNER_RELATION), innerFunction, innerBuckets, innerFlushFrequency);

        //Particiona relação outer de forma assincrona
        dummyBucket = new ListBucket(-1, -1, -1, -1, home + "", null);
        Thread t = new Thread(this, "HshPrt:" + id);
        t.start();
    }
    
    /**
     * Encerra este operador e seus produtores. Deterimina o fim da thread de particionamento da relação outer.
     * 
     * @throws IOException
     *             Se acontecer algum problema durante a remoção dos arquivos
     *             tempor�rios utilizado pelos buckets.
     * @throws Exception
     *             Se acontecer algum problema no encerramento de seus
     *             produtores.
     */
    public void close() throws IOException, Exception {

        //Para Particionamneto da relação outer
        continueProcessing = false;

        super.close();
    }

    /**
     * Determina o pr�ximo bucket a ser carregado. Na pol�tica adotada o bucket a ser carregado � aquele
     * da relação outer que possui mais tuplas n�o processadas.
     * 
     * @throws IOException Se algum erro acontecer durante a recuperação dos dados da relação inner.
     */
    protected boolean loadBucket() throws IOException {

        Bucket outerBucket;

        //Se este bucket n�o tem novas tuplas ou � nulo um novo outer bucket �
        // carregado
        outerBucket = null;

        //Determina proximo bucket
        while (outerBucket == null) {

            //Carrega outer bucket
            outerBucket = (Bucket) bucketsPriorityQueue.deleteMax();

            if (outerBucket.isEmpty()) {

                if (outerBucket.equals(dummyBucket)) {
                    return false;
                }
                outerBucket = null;

            } else {

                break;
            }
        }

        return loadBucket(outerBucket.bcktId);

    }

    /**
     * Procura por uma tupla na relação outer que fa�a o match com uma tupla da
     * relação inner. Carrega um bucket da relação outer e seu respectivo da
     * relação inner e aplica o algoritmo de junção por loops aninhados.
     * Sempre que uma tupla da relação outer � consumida a thread de particionamento � informada.
     * 
     * @return Uma tupla decorrente da junção de uma outer tuple com uma inner
     *         tuple.
     * 
     * @throws IOException
     *             Se acontecer algum problema ao ler as tuplas dos buckets.
     * @throws OutOfMemoryError
     *             Se n�o houver mem�ria suficiente para carregar buckets em
     *             mem�ria.
     * @throws PredicateEvaluatorException
     *             Se algum problema acontecer durante a avaliação do predicado
     *             de junção.
     */
    protected DataUnit applyMatch() throws IOException, OutOfMemoryError,
            PredicateEvaluatorException {

        DataUnit joinedTuple;

        joinedTuple = super.applyMatch();

        sync.release();

        return joinedTuple;
    }

    /**
     * Redefine forma com que relação � particionada para adicionar uma
     * sincronização entre thread de particionamento e match. O particionamento
     * da relação outer s� prossegue a medida que as tuplas particionadas s�o consumidas pela fase de match.
     * Mantem atualizada a lista de buckets segundo o nr de tuplas particionadas para cada um.
     * 
     * @throws Exception Se acontecer algum problema no consumo de tuplas do produtor da relação outer.  
     */
    void hashOuterRelation(int partiotionBlockSize) throws Exception {

        DataUnit instance1;
        int assignedBuckets[];
        Operator producer;
        int assignedTuples;
        Bucket currBucket;
        int acqLocks; //Locks adquiridos

        assignedTuples = 0;
        instance1 = null;
        producer = super.getProducer(OUTER_RELATION);

        while (continueProcessing) {

            //adquire n locks - ou seja n tuplas foram particionadas
            acqLocks = 0;
            while (acqLocks < partiotionBlockSize && continueProcessing) {
                sync.acquire();
                acqLocks++;
            }

            hashLogger.debug("DPLHash(" + id + "): Particionamento Outer:particionar� bloco de tupla relação outer.");
            
            //Particiona partitionBlockSize tuplas
            while (acqLocks > 0 && continueProcessing) {

                instance1 = producer.getNext(id);//Obtem primeira tupla

                if (instance1 == null)
                    break;
                
                /*
                 * Identifies the list of buckets each point should be stored.
                 */
                assignedBuckets = outerFunction.assign(instance1);
                assignedTuples++;

                //Adiciona tupla ao bucket
                if (assignedBuckets.length == 0)
                    hashLogger.debug("DPLHash(" + id + "): Particionamento Outer: Tupla n�o caiu em nenhum bucket: " + instance1);
                
                for (int i = 0; i < assignedBuckets.length; i++) {
                    currBucket = (Bucket) outerBuckets.get(new Integer(assignedBuckets[i]));
                    if (currBucket == null)
                        hashLogger.debug("DPLHash(" + id + "): Particionamento Outer: Tupla caiu em bucket NULL.");

                    currBucket.add((Tuple) instance1);
                    bucketsPriorityQueue.insert(currBucket);
                }

                acqLocks--;
            }
            hashLogger.debug("DPLHash(" + id + "): Particionamento Outer: Finalizou particionamento de um bloco de tuplas. Nr particionadas:" + assignedTuples);
            if (instance1 == null)
                break;
        }

        hashLogger.info("Hash(" + id + ") finalizou particionamento relação outer. Tuplas particionadas " + assignedTuples);
    }

//    void hashOuterRelation(int partiotionBlockSize) throws Exception {
//
//        DataUnit instance;
//        int assignedBuckets[];
//        Operator producer;
//        int assignedTuples;
//        Bucket currBucket;
//        int acqLocks; //Locks adquiridos
//        
//        assignedTuples = 0;
//        instance = null;        
//        producer = super.getProducer(OUTER_RELATION);
//
//        int pegou=0;
//
//    	//Obtem primeira tupla, forca que uma tupla seja produzida antes de come'car a contar os locks
//    	//num ambiente distribuido pode levar a obtencao de um bloco
//    	instance = producer.getNext(id);
//    	pegou++;
//    	System.out.println("Hash(" +id+ ") recebeu primeira e partition block size = " + partitionBlockSize );
//    	
//        while (continueProcessing && instance != null) {
//     	
//            //adquire n locks - ou seja n tuplas foram particionadas
//            acqLocks = 0;
//            while (acqLocks < partiotionBlockSize && continueProcessing && instance != null) {
//                sync.acquire();
//                acqLocks++;
//            }
//
//            hashLogger
//                    .debug("DPLHash("
//                            + id
//                            + "): Particionamento Outer:particionar� bloco de tupla relação outer.");
//            //Particiona partitionBlockSize tuplas
//            while (acqLocks > 0 && continueProcessing && instance != null) {
//                
//                assignedBuckets = outerFunction
//                        .assign(instance);
//                assignedTuples++;
//
//                //Adiciona tupla ao bucket
//                if (assignedBuckets.length == 0) {
//                    hashLogger
//                            .debug("DPLHash("
//                                    + id
//                                    + "): Particionamento Outer: Tupla n�o caiu em nenhum bucket: "
//                                    + instance);
//                }
//
//                for (int i = 0; i < assignedBuckets.length; i++) {
//                    currBucket = (Bucket) outerBuckets.get(new Integer(
//                            assignedBuckets[i]));
//                    if (currBucket == null)
//                        hashLogger
//                                .debug("DPLHash("
//                                        + id
//                                        + "): Particionamento Outer: Tupla caiu em bucket NULL.");
//
//                    currBucket.add((Tuple) instance);
//                    if (!bucketsPriorityQueue.changeOrder(currBucket))
//                        bucketsPriorityQueue.add(currBucket);
//                }
//                
//                acqLocks--;
//                instance = producer.getNext(id);//Obtem proxima tupla
//                pegou++;
//            }
//            hashLogger
//                    .debug("DPLHash("
//                            + id
//                            + "): Particionamento Outer: Finalizou particionamento de um bloco de tuplas. Nr particionadas:"
//                            + assignedTuples);
//            
//            if( instance == null )
//            	break;
//        }
//
//        hashLogger
//                .info("Hash("
//                        + id
//                        + ") finalizou particionamento relação outer. Tuplas particionadas "
//                        + assignedTuples);
//    }

    public void run() {

        try {
            hashLogger.debug("DPLHash(" + id
                    + "): Particionamento Outer: comecou.");
            hashOuterRelation(partitionBlockSize);
        } catch (Exception exc) {
            exc.printStackTrace();
        }

        //Insere bucket dummy
        this.bucketsPriorityQueue.insert(dummyBucket);

        hashLogger.debug("DPLHash(" + id
                + "): Particionamento Outer: terminou.");
    }

}