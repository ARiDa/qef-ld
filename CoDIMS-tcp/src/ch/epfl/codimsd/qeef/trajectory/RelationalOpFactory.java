package ch.epfl.codimsd.qeef.trajectory;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.exceptions.operator.OperatorInitializationException;
import ch.epfl.codimsd.qeef.DataSourceManager;
import ch.epfl.codimsd.qeef.trajectory.algebraic.DummyOperator;
import ch.epfl.codimsd.qeef.trajectory.algebraic.Fold;
import ch.epfl.codimsd.qeef.operator.algebraic.Project;
import ch.epfl.codimsd.qeef.trajectory.algebraic.Scan;
import ch.epfl.codimsd.qeef.trajectory.algebraic.Unfold;
import ch.epfl.codimsd.qeef.operator.control.Block2InstanceConverter;
import ch.epfl.codimsd.qeef.trajectory.control.Eddy;
import ch.epfl.codimsd.qeef.operator.control.Instance2BlockConverter;
import ch.epfl.codimsd.qeef.operator.control.Merge;
import ch.epfl.codimsd.qeef.operator.control.Split;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.Predicate;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateParser;
import ch.epfl.codimsd.qeef.trajectory.algebraic.hash.spatial.SpatialHashJoin;
import ch.epfl.codimsd.qeef.trajectory.algebraic.hash.temporal.TemporalHashJoin;
import ch.epfl.codimsd.qeef.relational.control.exchange.SenderOp;
import ch.epfl.codimsd.qeef.relational.control.exchange.ReceiverOp;
import ch.epfl.codimsd.qeef.trajectory.function.tcp.OperadorTCP;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qep.OpNode;

import java.net.URL;
import org.apache.log4j.Logger;

public class RelationalOpFactory{
    
    /**
     * Componente respons�vel por realizar o log das mensagens.
     */
    protected Logger logger;
    
    /**
     * Construtor padr�o.
     */
    public RelationalOpFactory(){
        
        logger = Logger.getLogger("qeef.operator.factory");
    }
    
    /**
     * Cria uma inst�ncia de um operador.
     * 
     * @param opId Identificador do operador
     * @param opName Nome do operador.
     * @param params Parametros utilizados na constru��o deste operador.
     * @param dsManager Gerente de fonte utilizado nesta inst�ncia.
     * @param blackBoard Quadro de comunica��o utilizado pelos operadores de um plano.
     * 
     * @return Inst�ncia do operador com nome opName.
     * 
     * @throws Exception Se acontecer algum erro durante a cria��o do operador.
     */
    public Operator createOperator(OpNode opNode) throws OperatorInitializationException {

        try {
            Operator op = null;
            DataSourceManager dsManager = DataSourceManager.getDataSourceManager();
            opNode.setOpName((String) opNode.getOpName().trim());

            if (opNode.getOpName().equalsIgnoreCase("SCAN")) {

                logger.debug(opNode.getOpName() + " " + opNode.getParams()[0] + " ; " + opNode.getParams()[1]);
                String dsName = opNode.getParams()[0].trim();
                String strPredicado = opNode.getParams()[1].trim();
                DataSource fonte = dsManager.getDataSource(dsName);

                PredicateParser parser = new PredicateParser();
                Predicate predicado = parser.parse(strPredicado,fonte
                        .getMetadata(), fonte.getMetadata());

                op = new Scan(opNode.getOpID(), opNode, predicado);

            } else if (opNode.getOpName().equalsIgnoreCase("REMOTESCAN")) {

//              op = criarRemoteScan(defOperador, blackBoard);

            } else if (opNode.getOpName().equalsIgnoreCase("PROJECT")) {
                String a1[];
                logger.debug(opNode.getOpName() + " " + opNode.getParams()[0] );
                
                if(opNode.getParams()[0].trim().equals(""))
                    a1 = new String[0];
                else
                    a1 = opNode.getParams()[0].split(",");
                
                op = new Project(opNode.getOpID(), opNode);

            } else if (opNode.getOpName().equalsIgnoreCase("Merge")) {
                int bufferSize = Integer.parseInt(opNode.getParams()[0]);
                op = new Merge(opNode.getOpID(), opNode);

            } else if (opNode.getOpName().equalsIgnoreCase("EDDY")) {

                int loadSize, reload, maxIteration, total;

                loadSize = Integer.parseInt( opNode.getParams()[0] );
                reload = Integer.parseInt( opNode.getParams()[1] );
                maxIteration = Integer.parseInt( opNode.getParams()[2] );
                total = Integer.parseInt( opNode.getParams()[3] );

                op = new Eddy(opNode.getOpID(), opNode);

            } else if (opNode.getOpName().equalsIgnoreCase("TEMPORALHASHJOIN")) {
                op = new TemporalHashJoin(opNode.getOpID(), opNode);

            } else if (opNode.getOpName().equalsIgnoreCase("SPATIALHASHJOIN")) {
                op = new SpatialHashJoin(opNode.getOpID(), opNode);
                
            } else if (opNode.getOpName().equalsIgnoreCase("Block2Instance")) {
                op = new Block2InstanceConverter(opNode.getOpID(), opNode);
                
            } else if (opNode.getOpName().equalsIgnoreCase("Instance2Block")) {
                int capacity = Integer.parseInt(opNode.getParams()[0]);
                op = new Instance2BlockConverter(opNode.getOpID(), opNode);

            } else if (opNode.getOpName().equalsIgnoreCase("TCP")) {
                op = new OperadorTCP(opNode.getOpID(), opNode);

            } else if (opNode.getOpName().equalsIgnoreCase("FOLD")) {

                op = new Fold(opNode.getOpID(), opNode);

            } else if (opNode.getOpName().equalsIgnoreCase("UNFOLD")) {

                op = new Unfold(opNode.getOpID(), opNode);

            } else if (opNode.getOpName().equalsIgnoreCase("Sender")) {
                op = new SenderOp(opNode.getOpID(), opNode);
                
            } else if (opNode.getOpName().equalsIgnoreCase("Receiver")) {
                URL gsh = null;
                int idRemote, blockSize;
                long waitTime;
                gsh = new URL(opNode.getParams()[0]);
                idRemote = Integer.parseInt(opNode.getParams()[1]);
                blockSize = Integer.parseInt(opNode.getParams()[2]);
                waitTime = Long.parseLong(opNode.getParams()[3]);
                
                op = new ReceiverOp(opNode.getOpID(), opNode);
                
            } else if (opNode.getOpName().equalsIgnoreCase("SPLIT")) {
                //String taxas = (String) tokens.get(1);
                int nrNodes, nrTuplas[], tamBuffer, nrIteration, prodRate[];

                nrNodes = Integer.parseInt(opNode.getParams()[0].trim());
                
                nrTuplas = new int[nrNodes];
                for(int i=0; i < nrNodes; i++){
                	nrTuplas[i] = (new Integer(opNode.getParams()[i+1])).intValue();
                }
                
                prodRate = new int[nrNodes];
                for(int i=0; i < nrNodes; i++){
                    prodRate[i] = (new Integer(opNode.getParams()[i+nrNodes+1])).intValue();
                }
                
                nrIteration = Integer.parseInt(opNode.getParams()[2*nrNodes+1].trim());

                op = new Split(opNode.getOpID(), opNode);

            } else if (opNode.getOpName().equalsIgnoreCase("Dummy")) {
            	int loop;
            	loop = Integer.parseInt(opNode.getParams()[0].trim());
                op = new DummyOperator(opNode.getOpID(), loop);
                
            } else {
                System.out.println("operador inexistente=" + opNode.getOpName());
                System.exit(0);
            }

            if (op == null) {
                logger.warn("OperatorFactory can't instantiate op " + opNode.getOpID() + " " + opNode.getOpName() );
            }

            return (op);
        } catch (CatalogException ex1) {
			throw new OperatorInitializationException("Error loading operator algebra from the catalog (operator " + opNode.getOpName() + ") : " + ex1.getMessage());
        }catch (Exception ex) {
			throw new OperatorInitializationException("Operator initialization error at " +
					opNode.getOpName() + " : " + ex.getMessage());
	}
    }

/*    public Fold instanciarFold(int opId, String []params) throws Exception {
        String sameColumns[], difColName[], prmSameCol, prmDifCol, prmSize, prmType;
        String strNewType[], strNewSize[];
        int ocurrence, tamBuffer;
        Type newType[];

        prmSameCol = params[0].trim().toUpperCase();
        prmDifCol  = params[1].trim().toUpperCase();
        ocurrence  = Integer.parseInt(params[2].trim());
        tamBuffer  = Integer.parseInt(params[3].trim());
        prmType    = params[4].trim();

        sameColumns = prmSameCol.split(",");
        difColName = prmDifCol.split(",");
        strNewType = prmType.split(",");

        //Converte tipo de size e type para int
        newType = new Type[strNewType.length];
        for (int i = 0; i < strNewType.length; i++) {
            newType[i] = Config.getDataType(strNewType[i]);
        }

        return new Fold(opId, sameColumns, difColName, ocurrence, tamBuffer, newType);
    }*/
}
    
//    private RemoteScan criarRemoteScan(String defOperador, BlackBoard blackBoard) throws Exception{
//        
//        
//        String parametros = defOperador.substring(12);
//        Vector tokens = decodificarMantendoCase(parametros, "!");
//        System.err.println("Parametros " + tokens);
//        
//        String arq = ((String) tokens.get(0)).trim(); //nome fonte
//        String strPredicado = ((String) tokens.get(1)).trim(); //predicado	   
//        
//        //Parametros de criacao da fonte remota
//        String name    = ((String) tokens.get(2)).trim(); //user
//        String remFile = ((String) tokens.get(3)).trim(); //password
//        String host    = ((String) tokens.get(4)).trim(); //nome host
//        int   porta    = Integer.parseInt( ((String)tokens.get(5)).trim() ); //porta
//        String user    = ((String) tokens.get(6)).trim(); //user
//        String passwd  = ((String) tokens.get(7)).trim(); //password
//        String strMetadata= ((String) tokens.get(8)).trim(); //metadata
//        
//        //Realiza parser Metadata
//        Metadata metadata = new TupleMetadata();
//        String []attrs = strMetadata.split("#");
//        String []attr;
//
//        Column currAtribute;
//        Type type;
//        
//        for(int i=0; i < attrs.length; i++){
//        
//            attr =  attrs[i].split(";");
//            type = getType( Integer.parseInt(attr[1]) );
//            
//            currAtribute = new Column(attr[0], type, Integer.parseInt(attr[2]), i, Boolean.getBoolean(attr[3]));
//               
//            metadata.addData(currAtribute);
//        }
//        
//        RemoteDataSource rds = new RemoteDataSource(name, remFile, host, porta, user, passwd, metadata);
//        
//
//        //Realiza parser do predicado
//        //DataUnit predicado
//        PredicateParser parser = new PredicateParser();
//        Predicate predicate = parser.parse(strPredicado, metadata, metadata);
//
//        RemoteScan op = new RemoteScan(rds, predicate, blackBoard);
//        
//        return op;
//
//    }
