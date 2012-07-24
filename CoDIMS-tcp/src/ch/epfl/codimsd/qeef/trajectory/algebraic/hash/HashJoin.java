package ch.epfl.codimsd.qeef.trajectory.algebraic.hash;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateParser;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qep.OpNode;

/**
 * Implementação genérica para um algoritmo de junção baseado em hash. Esta
 * implementação segue uma versão limitada do framework proposto por Ming-Ling
 * Lo, Chinya V. Ravishankar em Spatial Hash Hoins. A implementação realizada
 * difere da proposta original por não suportar um número de buckets diferentes
 * no particionamento das relações além de utilizar a mesma função de
 * particionamento.
 * <p>
 * O algoritmo de junção hash proposto realiza o particionamento das relações
 * Inner e Outer pela utilização de uma função de particionamento. Os dados de
 * uma relação particionada serão armazenados em uma estrutura auxiliar
 * denominada bucket, que são numerados de 1 até o nr de Buckets utilizados.
 * Desta forma, se uma tupla da relação outer deve realizar o join com uma tupla
 * da relação inner, então elas foram particionadas para o bucket de mesmo
 * identificador de sua respectiva relação.
 * <p>
 * Após realizado o particionamento dos dados um algortimo de junção mais
 * simples como loops aninhados pode ser aplicado em um bucket da relação inner
 * com um da relação outer.
 * <p>
 * 
 * <b>Trabalhos futuros: </b>
 * <p>
 * <ol>
 * <li>Estudar eficiência do algortimo de loops aninhados para realizar a
 * junção das tuplas de buckets correspondentes. Talvez atribuir a busca por
 * tuplas que realizaem o match ao bucket, que por sua vez poderia utilizar
 * alguma estrutura de dados mais eficiente.
 * </ol>
 * 
 * @author Vinicius Fontes
 */

//Outer Relation - producers[0]
//Inner Relation - producers[1]
public abstract class HashJoin extends Operator {

	/**
     * Constante utilizada para referenciar uma outer relation.
     */
    public static final int OUTER_RELATION = 0;

    /**
     * Constante utilizada para referenciar uma inner relation.
     */
    public static final int INNER_RELATION = 1;

    /**
     * Metadados da relação inner
     */
    protected Metadata innerMetadata;

    /**
     * Metadados da relação outer
     */
    protected Metadata outerMetadata;	
	
	
    /**
     * Tamanho do inner bucket
     */
    protected int innerBucketSize;

    /**
     * Tamanho do outer bucket
     */
    protected int outerBucketSize;

    /**
     * Função de hash utilizada no particionamento da relação inner.
     */
    protected HashFunction innerFunction;
    
    /**
     * Função de hash utilizada no particionamento da relação outer.
     */
    protected HashFunction outerFunction;

    /**
     * Buckets da relação inner indexados pelo sei identificador. Buckets são
     * numerados de 1 em diante.
     */
    protected Hashtable innerBuckets;

    /**
     * Buckets da relação outer indexados pelo sei identificador. Buckets são
     * numerados de 1 em diante.
     */
    protected Hashtable outerBuckets;

    /**
     * Identificador do bucket que está em memória no instante. -1 indica que
     * nenhum bucket foi carregado ainda.
     */
    protected int currBucketNumber;

    /**
     * Iterador utilizado para percorrer as tupls do inner bucket.
     */
    protected BucketIterator itInnerBucket;

    /**
     * Iterador utilizado para percorrer as tupls do outer bucket.
     */
    protected BucketIterator itOuterBucket;

    /**
     * Indica se deve ser realizado a alteração de bucket. Ver
     * HashJoin#applyHash()
     */
    protected boolean changeBucket;

    /**
     * String com predicado de junção.
     */
    private String strJoinPredicate;

    /**
     * Predicado de junção.
     */
    protected ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Predicate joinPredicate;

    /**
     * Número de buckets utilizado. Igual para ambos os inner e outer buckets.
     */
    protected int nrBuckets;

    /**
     * Componente de log utilizado nos operadores de hash
     */
    protected Logger hashLogger;

    /**
     * Local onde buckets irão persistir seus dados temporariamente.
     */
    protected File home;

    /**
     * Construtor padrão.
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
     * @param joinPredicate
     *            Predicado de junção a ser utilizado.
     */
    public HashJoin(int id, OpNode op) {

        super(id);

        this.nrBuckets = Integer.parseInt(op.getParams()[0]);

        this.innerBucketSize = Integer.parseInt(op.getParams()[1]);
        this.outerBucketSize = Integer.parseInt(op.getParams()[2]);

        this.strJoinPredicate = op.getParams()[7];

        hashLogger = Logger.getLogger("qeef.operator.relational.hash");
    }

    /**
     * Realiza a inicialização deste operador e de seus produtores. Os buckets
     * serão instanciados e utilizarão o local QEEF_HOME/tmp como local de
     * armazenamento temporário. O particionamento das relações devem ser
     * realizados nas implementações específicas de cada join. Por exemplo, no
     * algoritmo de join DoublePipelineHashJoin apenas a relação inner é
     * previamente particionada.
     * 
     * @throws IOException
     *             Se houver algum problema na instanciação dos buckets.\
     * @throws Exception
     *             Se houver algum problema na inicialização dos seus
     *             produtores.
     */
    public void open() throws IOException, Exception {
        super.open();

        hashLogger.debug("Hash(" + id + ") Metadata: " + metadata);

        //Define e Inicializa funcao de hash
        innerFunction = createInnerFunction();
        outerFunction = createOuterFunction();

        //Inicializa buckets
        home = new File(File.separator + "srv" + File.separator + "dadosTCP");
        // home = (File) Config.getProperty("QEEF_HOME");
        home = new File(home + File.separator + "tmp");
        if (!home.exists())
            home.mkdir();
        innerBuckets = new Hashtable();
        outerBuckets = new Hashtable();
        /*
         * This creates two bucket structures for both inputs. Seems more adequate
         * to Double pipeline hashjoin. Different implementations should override this method.
         */
        createBuckets(innerBuckets, nrBuckets, innerBucketSize, INNER_RELATION,
                (Metadata) innerMetadata.clone(), home);
        createBuckets(outerBuckets, nrBuckets, outerBucketSize, OUTER_RELATION,
                (Metadata) outerMetadata.clone(), home);

        //DataUnit predicado de juncao
        PredicateParser pp = new PredicateParser();

        joinPredicate = pp.parse(strJoinPredicate, outerMetadata, innerMetadata);

        //Inicializa parametros iniciais
        currBucketNumber = -1;
        changeBucket = true;
    }
    
    /**
     * Obtem uma instância que é resultado da junção de uma tupla da relação
     * inner com uma tupla da relação outer.
     * 
     * @param consumerId
     *            Consumidor que está fazendo a requisição.
     * @throws IOException
     *             Se ocorrer algum problema durante a leitura de dados do
     *             bucket.
     * @throws OutOfMemoryError
     *             Se não houver memória suficiente para realizar a carga do
     *             bucket.
     */
    public DataUnit getNext(int consumerId) throws Exception {

        this.instance = applyMatch();

        if (instance != null) {
            produced++;
        }

        return instance;

    }
    
    /**
     * Define o metadado das tuplas resultantes da junção. Metadado é dado por:
     * metadados outer união metadados inner, nesta ordem.
     * 
     * @param prdMetadata
     *            Metadados de seus produtores. Outer e inner respectivamente.
     */
    @Override
    public void setMetadata(Metadata[] prdMetadata) {
        Metadata joined;

        outerMetadata = (Metadata) prdMetadata[OUTER_RELATION].clone();
        innerMetadata = (Metadata) prdMetadata[INNER_RELATION].clone();

        joined = (Metadata) outerMetadata.clone();
        joined.join(innerMetadata.getData()); //Junta os dois metadados

        metadata[0] = joined;    
    }

    /**
     * Encerra este operador e seus produtores.
     * 
     * @throws IOException
     *             Se acontecer algum problema durante a remoção dos arquivos
     *             temporários utilizado pelos buckets.
     * @throws Exception
     *             Se acontecer algum problema no encerramento de seus
     *             produtores.
     */
    public void close() throws IOException, Exception {

        super.close();

        Bucket aux;
        Enumeration enumBuckets;

        //Fecha inner Buckets
        enumBuckets = innerBuckets.elements();
        while (enumBuckets.hasMoreElements()) {
            aux = (Bucket) enumBuckets.nextElement();
            aux.close();
        }
        //Fecha outer buckets
        enumBuckets = outerBuckets.elements();
        while (enumBuckets.hasMoreElements()) {
            aux = (Bucket) enumBuckets.nextElement();
            aux.close();
        }

        innerFunction = null;
        outerFunction = null;
        innerBuckets = null;
        outerBuckets = null;
        itOuterBucket = null;
        itInnerBucket = null;

    }

    /*
     * Mantem referência para o bucket corrente e um iterador para cada bucket
     */
    /**
     * Procura por uma tupla na relação outer que faça o match com uma tupla da
     * relação inner. Carrega um bucket da relação outer e seu respectivo da
     * relação inner e aplica o algoritmo de junção por loops aninhados.
     * 
     * @return Uma tupla decorrente da junção de uma outer tuple com uma inner
     *         tuple.
     * @throws IOException
     *             Se acontecer algum problema ao ler as tuplas dos buckets.
     * @throws OutOfMemoryError
     *             Se não houver memória suficiente para carregar buckets em
     *             memória.
     * @throws PredicateEvaluatorException
     *             Se algum problema acontecer durante a avaliação do predicado
     *             de junção.
	 */
 	protected DataUnit applyMatch() throws IOException, OutOfMemoryError,
            PredicateEvaluatorException {

        boolean match;
        Instance outerTuple, innerTuple, joinedTuple;
        int comparisons = 0;

        match = false;
        outerTuple = null;
        innerTuple = null;

        //Enquanto nenhuma tupla fizer match e ainda houver tuplas
        //nos buckets para serem processadas
        //Itera sobre buckets -> outer Bucket --> inner Bucket
        while (!match) {
            
            if (changeBucket) {
                loadBucket();
               // if(!loadBucket());
                 //   break;
            }
            changeBucket = true;

            //Enquanto nenhuma tuplas fizer match e
            //Houver outer tuple
            while (!match && itOuterBucket.hasNext()) {

                comparisons = 0;
                outerTuple = itOuterBucket.next();

                //Enquanto nenhuma inner tuple fizer match e tiver proxima
                while (!match && itInnerBucket.hasNext()) {

                    innerTuple = itInnerBucket.next();

                    comparisons++;
                    
                    if (joinPredicate.evaluate((Tuple) outerTuple, (Tuple) innerTuple)) {
                        match = true;
                        //Reposiciona iterador inner
                        Bucket auxBucket;
                        auxBucket = (Bucket) innerBuckets.get(new Integer(currBucketNumber));
                        itInnerBucket = auxBucket.iterator();
                    }
                }

                if (match == false)
                    hashLogger.debug("Hash(" + id + ") Tupla Nao realizou match " + outerTuple);
            }

            Bucket aux = (Bucket) innerBuckets.get(new Integer(currBucketNumber));
            hashLogger.debug("Hash(" + id + ") produced  " + produced + " comparisons " + comparisons + " bucketSize " + aux.size());
        }

        changeBucket = false;

        if (match) {
            joinedTuple = outerTuple.join(outerTuple, innerTuple);
        } else {
            joinedTuple = null;
        }

        return joinedTuple;
    }

    /**
     * Realiza a carga do bucket da relação outer e seu respectivo da relação
     * inner. Se algum bucket estivesse sendo utilizado anteriormente ele será
     * fechado. Após realizar a carga dos buckets prepara os iteradores
     * utilizados para "varrer" os buckets.
     * 
     * @param bucketId
     *            Identificador do bucket a ser carregado.
     * 
     * @throws IOException
     *             Se algum problema acontecer ao ler dados do bucket.
     * @throws OutOfMemoryError
     *             Se não houver memória suficiente para carregar o bucket.
     */
    protected boolean loadBucket(int bucketId) throws IOException,
            OutOfMemoryError {

        Bucket innerBucket, outerBucket;

        innerBucket = (Bucket) innerBuckets.get(new Integer(bucketId));
        outerBucket = (Bucket) outerBuckets.get(new Integer(bucketId));

        if (innerBucket == null || outerBucket == null) {
            hashLogger.warn("Bucket id " + bucketId + " Inexistente.");
            return false;
        }

        //Verifica novo bucket a ser carregado e diferente ao que esta em
        // memoria
        //Se for descarrega o atual e carrega novo
        if (currBucketNumber != bucketId) {
            
            hashLogger.info("Hash(" + id + "): mudou de bucket " + currBucketNumber + " para " + bucketId);

            //Realiza unload dos buckets carregados
            if (currBucketNumber >= 0) {//Seu valor inicial � -1

                ((Bucket) innerBuckets.get(new Integer(currBucketNumber))).unload();
                ((Bucket) outerBuckets.get(new Integer(currBucketNumber))).unload();
            }

            currBucketNumber = bucketId;

            innerBucket.load();
            outerBucket.load();
        }

        //Incializa iteradores
        itInnerBucket = innerBucket.iterator();
        itOuterBucket = outerBucket.iterator();

        return true;
    }

    /**
     * Realiza o particionamento de uma relação.
     * 
     * @param producer
     *            Define o produtor desta relação.
     * @param function Função de hash utilizada.
     * @param buckets
     *            Buckets utilizado para realizar o particionamento.
     * @param flushFrequency
     *            Nr de tuplas que podem ser particionadas, antes que o
     *            resultado seja gravado no disco.
     * 
     * @throws IOException
     *             Se ocorrer algum problema ao gravar os dados no bucket.
     * @throws Exception
     *             Se acontecer algum problema ao consumir os dados do produtor
     *             ou, algum problema ocorrer na função de hash.
     */
    protected void hashRelation(Operator producer, HashFunction function, Hashtable buckets,
            int flushFrequency) throws IOException, Exception {

        DataUnit instance;
        int assignedBuckets[];
        int assignedTuples;
        Bucket currBucket;

        assignedTuples = 0;
        instance = producer.getNext(id);//Obtem primeira tupla

        while (instance != null) {

            /*
             * Obtains the list of ids corresponding to the buckets where the tuple should be stored.
             */
            assignedBuckets = function.assign(instance);

            //Adiciona tupla ao bucket
            if (assignedBuckets.length == 0) {
                hashLogger.info("HashJoin(" + id + "): Exception : Tupla nao Realizou macth:" + instance);
            }


            for (int i = 0; i < assignedBuckets.length; i++) {
                currBucket = (Bucket) buckets.get(new Integer(assignedBuckets[i]));
                if (currBucket == null)
                    hashLogger.warn("HashJoin(" + id + "): Exception Tupla particionada em Bucket null Tuple:" + instance);
              
                 currBucket.add((Tuple) instance);
            }

            assignedTuples++;
           // System.out.println("assigned tuples = " + assignedTuples);
            if (assignedTuples == flushFrequency) {
                assignedTuples = 0;
                flushBuckets(buckets);
            }
            instance = producer.getNext(id);
        }
        flushBuckets(buckets);

      //  hashLogger.info("Particionamento hash(" + id + ") terminou particionamento relacao");
    }

    /**
     * Solicita que buckets gravem os dados particionados e liberem a memória
     * ocupada por essas tuplas.
     * 
     * @param buckets
     *            Buckets utilizados no particionamento.
     * 
     * @throws IOException
     *             Se algum problema acontecer ao gravar os dados no bucket.
     */
    protected void flushBuckets(Hashtable buckets) throws IOException {

        Bucket currBucket;
        Enumeration enumBuckets;

        //Itera sobre buckets solicitando persit�ncia dos buffers de
        // particionamento
        enumBuckets = buckets.elements();
        while (enumBuckets.hasMoreElements()) {
            currBucket = (Bucket) enumBuckets.nextElement();
            currBucket.flush();
        }

    }

    /**
     * Realiza a carga de um bucket. Define um bucket a ser carregado segundo
     * alguma política.
     * 
     * @return True se carregou algum bucket, false caso contrário.
     * 
     * @throws Exception
     *             Se alguma Exceção ocorrer durante a carga do bucket.
     */
    protected abstract boolean loadBucket() throws IOException;

    /**
     * DataUnit os buckets de uma relação. Os buckets devem ser indexados de 0 a
     * nrBuckets.
     * 
     * @param buckets
     *            Tabela hash na qual os buckets devem ser inseridos.
     * @param nrBuckets
     *            Número de buckets a ser criado.
     * @param bucketSize
     *            Capacidade do bucket.
     * @param metadata
     *            Metadado que descreve o formato das tuplas armazenadas neste
     *            bucket.
     * @param home
     *            Local onde o arquivo de persistência do bucket será criado.
     * 
     * @throws Exception
     *             Se ocorrer algum erro durante a criação dos buckets.
     */
    protected abstract void createBuckets(Hashtable buckets, int nrBuckets,
            int bucketSize, int relation, Metadata metadata, File home)
            throws Exception;

    /**
     * Define a função de hash a ser utilizada no particionamento da relação inner.
     * 
     * @return Função de hash a ser utilizada.
     */
    public abstract HashFunction createInnerFunction();

    /**
     * Define a função de hash a ser utilizada no particionamento da relação outer.
     * 
     * @return Função de hash a ser utilizada.
     */
    public abstract HashFunction createOuterFunction();

}