package ch.epfl.codimsd.qeef.trajectory.algebraic.hash;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.datastructure.Buffer;
import ch.epfl.codimsd.qeef.datastructure.Estrutura;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.relational.io.TupleReader;
import ch.epfl.codimsd.qeef.relational.io.TupleWriter;

/**
 * Define a estrutura e funcionalidades de um bucket do operador HashJoin. Esta
 * estrutura foi definida de forma que suas sub-classes possam definir a
 * estrutura a ser utilizada na carga em memória do bucket.
 * 
 * Um bucket é composto de duas estruturas: um buffer para o armazenamento das
 * tuplas que estão sendo particionadas e uma estrutura de memória utilizada na
 * carga do bucket. Esta última pode ser aproveitada para indexar as tuplas e
 * melhorar o desempenho da etapa de join do algoritmo de hash.
 * 
 * A propriedade capacity do bucket define o número de tuplas que podem ficar
 * simultaneamente na estrutura de memória. A implementação do bucket fornece
 * mecanismos para que nunca aconteça de um número de tuplas maior que capacity
 * estejam em memória. Já o número de tuplas no buffer de particionamento deve
 * ser gerenciado pelo algoritmo de hash.
 * 
 * Uma tupla adicionada no bucket permanece no buffer de tuplas particionadas
 * até que a operação de flush seja executada ou um iterador para o bucket seja
 * aberto. No último caso, as tuplas serão movidas para a estrutura de memória e
 * serão descartadas qdo o bucket for descarregado ou a operação refresh for
 * executada (ver implementações de BucketIterator).
 * 
 * Na implementação realizada, pode-se ter duas threads utilizando o bucket: uma
 * adicionando tuplas ao bucket e outra consumindo os dados. No entanto, desta
 * maneira, a thread que adiciona tuplas não deve realizar operações de flush.
 * Seu desenvolvimento foi realizado tendo em vista que o particionamento da
 * relação outer seria realizado em paralelo com seu particionamento e desta
 * forma não haveria necessidade de se gravas as tuplas desta relação em disco.
 * 
 * O funcionamento de um bucket pode ser descrito como a seguir:
 * 
 * 1- Construção/Inicialiazação
 * 
 * 2- Adição de novas instâncias - Nesta fase são utilizados os métodos
 * add(DataUnit) e flush(), que grava as instâncias deste bucket em disco e
 * libera a memória ocupada.
 * 
 * 3- Load - Nesta etapa as instâncias armazenadas neste bucket são carregadas
 * em memória.
 * 
 * 4- Consumo - As intâncias armazenadas no bucket podem ser consumidas pelo uso
 * de um iterador. Como o número de tuplas neste bucket pode requisitar mais
 * memória do que o disponível, a carga é realizada em sub-conjuntos de tamanho
 * capacity. O iterador encapsula as cargas intermediárias através do método
 * refresh, que é invocado sempre que se chegar ao fim de um sub-conjunto de
 * instâncias. O método refresh obtem
 * 
 * 5- Unload - Todo espaço de memória ocupado por este bucket é liberado. Se
 * houver a necessidade de consumos dos dados neste bucket a operação load deve
 * ser realizada novamente.
 * 
 * 6- Close- O bucket não será mais utilizado, desta forma todos os recursos
 * serão liberados, não só memória mas também o espaço em disco utilizado.
 * 
 * Os métodos a seguir devem ser implementados nas sub-classes de Bucket.
 * 
 * Trabalho Futuros: Implementar operação de match no bucket de forma a
 * aproveitar a estrutura de memória.
 * 
 * 
 * @author Vinicius Fontes
 * 
 * @date Mar 16, 2005
 * 
 * @see QEEF.relational.algebraic.hash.DoublePipelineHashJoin
 */
public abstract class Bucket implements Comparable {

    /**
     * Identificador do operador de hash que utiliza este bucket. Utilizado para
     * log das operações realizadas pelo bucket.
     */
    protected int hashId;

    /**
     * Identifica se este bucket é utilizado no particionamento da relação inner
     * ou outer. Valores defindos por HashJoin.INNER_RELATION e
     * HashJoin.OUTER_RELATION. Utilizado para log das operações realizadas pelo
     * bucket.
     */
    protected int relation;

    /**
     * N�mero de identificação deste bucket.
     */
    public int bcktId;

    /**
     * Indica se este bucket está carregado em memória.
     */
    protected boolean loaded;

    /**
     * Arquivo temporário utilizado para persistir o conjunto de dados
     * armazenados neste bucket.
     */
    protected File bcktFile;

    /**
     * Fluxo de leitura para bcktFile.
     */
    protected TupleReader bucketReader;

    /**
     * Fluxo de escrita para bcktFile.
     */
    protected TupleWriter bucketWriter;

    /**
     * Número de instâncias que podem estar simultaneamente em memória neste
     * bucket.
     */
    protected int capacity;

    /**
     * Número de tuplas do bucket que estão em disco.
     */
    protected int tupleInDisk;

    /**
     * Número de tuplas que foram lidas do disco desde que o bucket foi
     * carregado em memória.
     */
    protected int readInstances;

    /**
     * Número de vezes que este bucket foi carregado em memória. Utilizado para
     * medir desempenho.
     */
    protected int loadTimes;

    /**
     * Metadado que descreve as tuplas neste bucket.
     */
    protected TupleMetadata metadata;

    /**
     * Estrutura de armazenamento utilizada na carga de um bucket.
     */
    protected Estrutura inMemoryTuples;

    /**
     * Estrutura utilizada para armazenar as tuplas que acabaram de ser
     * particionadas.
     */
    protected Buffer particionedTuples;

    /**
     * Componente de log utilizado.
     */
    protected Logger logger;

    /**
     * 
     *  
     */
    public Bucket(int hashId, int bcktId, int relation, int capacity,
            String path, Estrutura inMemoryStructure, TupleMetadata metadata)
            throws FileNotFoundException, IOException {

        logger = Logger.getLogger("ch.epfl.codimsd.qeef.trajectory.algebraic.hash.bucket");

        this.hashId = hashId;
        this.bcktId = bcktId;
        this.relation = relation;
        this.capacity = capacity;
        this.metadata = metadata;
        this.inMemoryTuples = inMemoryStructure;
        this.particionedTuples = new Buffer();
        this.loaded = false;
        this.loadTimes = 0;
        tupleInDisk = 0;
        this.readInstances = 0;

        this.bcktFile = new File(path + File.separator + "Hsh" + hashId
                + "Bckt" + bcktId + "R" + relation + ".tmp");

        bcktFile.delete();

        FileOutputStream fos = new FileOutputStream(bcktFile);
        this.bucketWriter = new TupleWriter(fos, metadata);
    }

    /**
     * Adiciona tuplas a este bucket.
     * 
     * @param tuple
     *            Tupla a ser adicionada no bucket.
     */
    public void add(Tuple tuple) {
        particionedTuples.add(tuple);
    }

    /**
     * Transfere as instâncias em memória na estrutura para um mecanismo de
     * armazenamento secundário (disco), para futura utilização.
     * 
     * @throws Se
     *             algum erro ocorrer durante a persistência.
     * @throws Se
     *             algum erro ocorrer durante a obtenção dos dados da estrutura.
     */
    public void flush() throws IOException {

        Iterator<Tuple> itElements;

        for (itElements = particionedTuples.iterator(); itElements.hasNext();) {

            bucketWriter.writeInstance(itElements.next());
            tupleInDisk++;
        }

        bucketWriter.flush();

        particionedTuples.removeAllElements();
    }

    /**
     * Realiza a substituição das tuplas em memória por um sub-conjunto de
     * tuplas (de tamanho definido por capacity) que estão na lista de tuplas
     * que acaparam de ser particionadasa ou em disco. As tuplas da lista de
     * tuplas que acabaram de ser removidas serão apagadas. Se não houver mais
     * tuplas a serem carregadas o bucket ficará sem nenhuma tupla em memória.
     * 
     * @return True se alguma tupla foi lida.
     */
    boolean refresh() throws IOException {

        int tuplesLoaded;//nr de tuplas carregadas

        if (!loaded){
//            throw new BucketNotLoadedException("Bucket id " + bcktId
//                    + " have to be loaded before the refresh operation.");
            return false;
        }

        //Limpa Buffer
        inMemoryTuples.removeAllElements();

        //Não tem mais tuplas
        if (readInstances >= tupleInDisk && particionedTuples.size() == 0) {
            return false;
        }

        tuplesLoaded = loadFromParticioned(capacity);

        //Carrega buffer com tuplas disponíveis
        tuplesLoaded += loadFromFile(capacity - tuplesLoaded);

        return tuplesLoaded > 0 ? true : false;
    }

    /**
     * Carrega Bucket com tuplas que acabaram de ser particionadas.
     * 
     * @param max
     *            N�mero m�ximo de tuplas que podem ser carregadas.
     * 
     * @result N�mero de tuplas carregadas.
     */
    private int loadFromParticioned(int max) {

        Collection subList;
        Iterator itSubList;

        subList = particionedTuples.getBlock(max);
        itSubList = subList.iterator();

        while (itSubList.hasNext()) {

            insert((Tuple) itSubList.next());
        }

        return subList.size();
    }

    /**
     * 
     * Carrega o bucket com no m�ximo max inst�ncias a partir do arquivo de
     * persistencia deste bucket.
     * 
     * @return N�mero de tuplas lidas/carregadas.
     * 
     * @throws IOException
     *             Se acontecer algum erro durante a leitura do arquivo.
     * @throws ClassNotFoundException
     *             Se alguma classe necess�ria n�o existir.
     */
    protected int loadFromFile(int max) throws IOException {

        DataUnit newInstance;
        int loaded;

        try {
            
            for (loaded = 0; loaded < max && readInstances < tupleInDisk; loaded++, readInstances++) {

                if(bucketReader.eof())
                    throw new EOFException("Bucket detectou fim de arquivo prematuro. Imposs�vel terminar carga do bucket.");
                newInstance = (DataUnit) bucketReader.readInstance();
                insert((Tuple) newInstance);
            }

        } catch (Exception exc) {
            logger.warn("Bucket encontrou erro ao ler dados do arquivo.");
            logger.warn("Bucket id " + bcktId);
            logger.warn("tuples in disk " + tupleInDisk);
            logger.warn("read instances " + readInstances);
            throw (IOException)exc;
        }

        return loaded;
    }

    /**
     * Descarrega este bucket de memória. Todo espa�o de memória ocupado por ele
     * ser� liberado. Iteradores desde bucket n�o funcionar�o.
     */
    public void unload() throws IOException {

        loaded = false;
        inMemoryTuples.removeAllElements();
        readInstances = 0;
        bucketReader.close();
    }

    /**
     * Indica se o buffer possui tuplas a serem processadas. N�o requer que
     * bucket esteja carregado.
     * 
     * @return True se o bucket possuir tuplas em memória ou em disco a serem
     *         lidas, false caso contr�rio.
     */
    public boolean isEmpty() {

        if (tupleInDisk + particionedTuples.size() > 0)
            return false;
        else
            return true;
    }

    /**
     * Obtem o n�mero de tuplas que est�o no bucket, em disco e em memória.
     * 
     * @return N�mero de tuplas no bucket
     */
    public int size() {
        return tupleInDisk + particionedTuples.size();
    }

    /**
     * Carrega este bucket em memória. Se o n�mero de tuplas neste bucket for
     * maior que capacity, apenas um sub-conjunto das tuplas neste bucket ser�o
     * carregadas. Primeiro o bucket ser� carregado com tuplas que acabaram de
     * ser processadas e, se ainda houver espa�o, as tuplas do disco ser�o
     * carregadas.
     * 
     * @throws IOException
     *             Se algum erro acontecer durante a leitura do arquivo de
     *             persist�ncia dos dados utilizado por este bucket.
     * @throws OutOfMemoryError
     *             Se n�o houver memória suficiente para que o bucket seja
     *             carregado.
     */
    void load() throws IOException, OutOfMemoryError {

        FileInputStream fis = new FileInputStream(bcktFile);
        bucketReader = new TupleReader(fis, (TupleMetadata) metadata);

        loaded = true;
        loadTimes++;
        readInstances = 0;
    }

    /**
     * Encerra a utilização deste bucket. Todos os recursos utilizados por este
     * disco (disco/memória) ser�o liberados.
     */
    public void close() throws IOException {

        //Encerra fluxos de leitura e gravação para bcktFile
        if (bucketReader != null) {//Pode n�o ter sido aberto pois outer
            // correspondente era vazio
            bucketReader.close();
        }

        bucketWriter.close();
        bucketReader = null;
        bucketWriter = null;

        bcktFile.delete();

        inMemoryTuples = null;
        particionedTuples = null;
        inMemoryTuples = null;
    }

    /**
     * Retorna um iterador para os elementos que est�o no bucket. Encapsula do
     * usu�rio o processo de cargas intermedi�rias que pode ser necess�rio se
     * todas as tuplas n�o puderem ser carregadas em memória simultaneamente.
     * 
     * @return Iterador para bucket.
     */
    public BucketIterator iterator() {

        return new BucketIteratorImpl(this, inMemoryTuples);
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "Bucket(" + bcktId + ") Size: " + size();
    }

    /**
     * Define como um bucket pode ser comparado com outro. Pode ser �til na
     * implementação das pol�ticas de carga dos buckets pelo operador HashJoin.
     */
    public int compareTo(Object bucket) {

        return this.size() - ((Bucket) bucket).size();
    }

    /**
     * Insere uma tupla na estrutura utilizada por este bucket. Esta
     * implementação tem o objetivo de n�o limitar o tipo de estrutura
     * utilizada. Esta pode ser uma estrutura se armazenamento e recuperação
     * simples como uma pilha e fila, ou uma estrutura de busca mais
     * sofisticada, por exemplo, uma �rvore.
     * 
     * @param tuple
     *            Tupla a ser inserida.
     */
    protected abstract void insert(Tuple tuple);

    //    /**
    //     * Tenta realizar o match de uma tupla com alguma tupla que esteja no
    // bucket.
    //     * Esta funcionalidade foi implementada no bucket com o intuito de
    // aproveitar a
    //     * estrutura de memória utilizada pelo bucket para realizar algum tipo de
    // indexação.
    //     *
    //     * @return Coleção com tuplas que fizeram o match.
    //     */
    //    public abstract Collection match(Tuple tuple) throws
    // BucketNotLoadedException;

}
