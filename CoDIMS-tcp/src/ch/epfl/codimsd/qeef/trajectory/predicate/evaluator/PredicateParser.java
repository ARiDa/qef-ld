package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.Metadata;

import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.datanodes.ValueNode;

/**
 * Componente respons�vel por realizar o parser de uma express�o e montar a
 * �rvore de avalia��o. Os operadores suportados ser�o definidos em um arquivo
 * de configura��o a ser procurado no local
 * definido pela propriedade de sistema PARSER_CONFIG_FILE. Esta vari�vel deve ser
 * definida na tabela de propriedade definda em System.properties. Obs1:
 * Expres�o deve ser escrita de forma que os dados refer�nciados de uma
 * inst�ncia estejam sempre a Esquerda ou a direita em todos os predicados da
 * express�o. Tarefa que normalmente � feita pelo analisador sint�tico ou
 * otimizador do BD. Obs2: N�o oferece suporte a parentetiza��o e verifica��o de
 * tipo em tempo de compila��o do predicado. Operadores devem verificar se tipo
 * de dado recebido no m�todo apply � suportado.
 *
 * Formato do arquivo de configura��o:
 *
 *   #Tipo de dados suportados
 *     "Nome";"Classe de implementa��o incluindo nome do pacote"
 *
 *   #Operadores de Acesso aos Dados ou DataNodes
 *      "Representacao do operador";"Classe de implementa��o incluindo nome do pacote"
 * 
 *   #Operadores Bin�rios
 *      "Representacao do operador";"Classe de implementa��o incluindo nome do pacote"
 *
 *   #Operadores Un�rios
 *      "Representacao do operador";"Classe de implementa��o incluindo nome do pacote"
 *
 *
 * Diferencia��o entre os dois tipos de operadores realizado pela linha em branca.
 * Linhas que come�arem com "#" () seram interpretadas como coment�rio.
 *
 * Adicionar suporte a parentetiza��o e verifica��o de tipo nas vers�es futuras.
 *
 * @author Vin�cius Fontes Vieira da Silva
 *
 * @date 05 FEV 2005
*/

/**
 * Adi��o de suporte a defini��o dos operadores por arquivo de configura��o.
 *
 * @author Vin�cius Fontes Vieira da Silva
 *
 * @date 10 FEV 2005
 */

public class PredicateParser {

    /*#QEEF.predicate.evaluator.Predicate Dependency_Link*/
    /**
     * Padr�o que ser� utilizado no parse de express�es. Dividi uma express�o em
     * predicados.
     */
    private static Pattern expPattern;

    /**
     * Padr�o que ser� utilizada no parse de predicados. Dividi um predicado em
     * algo na forma operando operador operando ou operador operando.
     */
    private static Pattern predicatePattern;

    /**
     * Tabela na qual se encontra a defini��o dos operadores bin�rios
     * suportados.
     */
    private static Hashtable binaryOperators;

    /**
     * Tabela na qual se encontra a defini��o dos operadores bin�rios
     * suportados.
     */
    private static Hashtable unaryOperators;

    /**
     * Tabela na qual se encontra a defini��o dos operadores de acesso a dados
     * suportados.
     */
    private static Hashtable dataNodes;

    /**
     * Tabela na qual se encontra a defini��o dos tipos suportados.
     */
    private static Hashtable types;

    /**
     * Realiza carga da tabela que define operadores suportados se ela n�o foi
     * feita anteriormente.
     */
    public PredicateParser() throws PredicateEvaluatorException {

        if (types == null || dataNodes == null || binaryOperators == null
                || unaryOperators == null) {

            loadConfigurationFile();
        }
    }

    /**
     * Realiza carga da tabela que define operadores suportados. Arquivo de
     * configura��o utilizado ser� CODIMS_HOME/predicateEvaluator. Ver descri��o
     * da classe.
     */
    public static void loadConfigurationFile()
            throws PredicateEvaluatorException {
        //Le arquivo de configura��o para buscar pelos operadores
        // suportados
        //define tabela com defini��o dos operadores e string com express�o
        // regular
        File configFile;

        //configFile = (File)Config.getProperty("PARSER_CONFIG_FILE");
        configFile = new File(File.separator + "srv" + File.separator + "dadosTCP" + File.separator +"parserConfigFile.txt");

        if (configFile == null)
            throw new PredicateEvaluatorException(
                    "Propriedade do Sistema PARSER_CONFIG_FILE n�o foi definida. Use System.setProperty().");

        if (!configFile.exists())
            throw new PredicateEvaluatorException(
                    "Arquivo de configura��o do parser n�o encontrado: "
                            + configFile);

        loadConfigurationFile(configFile);
    }

    /**
     *
     * Realiza a carga do arquivo de configura��o. Pode ser executado caso
     * deseje-se atualizar a tabela de operadores suportados.
     *
     * @param configFile
     *            Arquivo de configura��o.
     *
     * @throws PredicateEvaluatorException
     *             Causas: O arquivo n�o exista ou ocorreu um erro durante a
     *             leitura do arquivo ou classe que represente um operador n�o
     *             existe.
     *
     */
    private static void loadConfigurationFile(File configFile)
            throws PredicateEvaluatorException {

        String line = null;//variavel auxiliar
        String definition[] = null;//variavel auxiliar para parse linha arquivo 
        String strExpPattern, strPredPattern;

        Operator auxOp;
        DataNode auxDn;

        try {
            //Cria fluxo de leitura para arquivo
            BufferedReader br = new BufferedReader(new FileReader(configFile));

            //Le tipos suportados
            types = new Hashtable();

            //Enquanto houver Tipos a serem lidos 
            //opDefinition[0] nome; opDefinition[1] classe
            while ((line = br.readLine()) != null) {

                line = line.replaceAll("[ ]+", "");
                if (line.equals(""))
                    break;

                if (!line.startsWith("#")) {
                    definition = line.split(";");
                    types.put(definition[0].toUpperCase(), instatiateObject(definition[1]));
                }
            }

            //Enquanto houver Operadores de acesso aos dados
            //opDefinition[0] nome; opDefinition[1] classe            
            strPredPattern = "(";
            dataNodes = new Hashtable();
            while ((line = br.readLine()) != null) {

                line = line.replaceAll("[ ]+", "");
                System.out.println(line);
                if (line.equals(""))
                    break;

                if (!line.startsWith("#")) {
                    definition = line.split(";");
                    auxDn = (DataNode) instatiateObject(definition[1]);
                    dataNodes.put(definition[0].toUpperCase(), auxDn);

                    strPredPattern += auxDn.getParserRepresentation() + "|"; //Monta expressao regular                    
                }
            }

            //Enquanto houver Operadores Binarios
            //opDefinition[0] nome; opDefinition[1] classe            
            strExpPattern = "(";
            binaryOperators = new Hashtable();
            while ((line = br.readLine()) != null) {

                line = line.replaceAll("[ ]+", "");
                System.out.println(line);
                if (line.equals(""))
                    break;

                if (!line.startsWith("#")) {
                    line = line.replaceAll("[ ]+", "");
                    definition = line.split(";");
                    auxOp = (Operator) instatiateObject(definition[1]);
                    definition[0] = definition[0].toUpperCase();
                    binaryOperators.put(definition[0], auxOp);

                    strPredPattern +=   definition[0]  + "|";
                    strExpPattern += auxOp.getParserRepresentation() + "|"; //Monta expressao regular
                }
            }

            System.out.println(strPredPattern);
            
            //Enquanto houver operadores un�rios
            unaryOperators = new Hashtable();
            while ((line = br.readLine()) != null) {

                line = line.replaceAll("[ ]+", "");
                if (line.equals(""))
                    break;

                if (!line.startsWith("#")) {
                    line = line.replaceAll("[ ]+", "");
                    System.out.println(line);
                    definition = line.split(";");
                    auxOp = (Operator) instatiateObject(definition[1]);
                    unaryOperators.put(definition[0].toUpperCase(), auxOp);

                    strPredPattern += "[" + definition[0] + "]" + "|";
                    strExpPattern += auxOp.getParserRepresentation() + "|"; //Monta expressao regular
                }
            }

            strPredPattern = strPredPattern.substring(0, strPredPattern
                    .length() - 1);
            strPredPattern += ")";

            strExpPattern = strExpPattern.substring(0,
                    strExpPattern.length() - 1);
            strExpPattern += ")";

            predicatePattern = Pattern.compile(strPredPattern);
            expPattern = Pattern.compile(strExpPattern);

        } catch (FileNotFoundException fnfExc) {
            throw new PredicateEvaluatorException(
                    "PredicateEvaluator: Arquivo de configura��o n�o existe.");

        } catch (IOException ioExc) {
            throw new PredicateEvaluatorException(
                    "PredicateEvaluator: Erro durante leitura do arquivo de configura��o. Formato inv�lido ou erro de I/O.");

        }

    }

    private static Object instatiateObject(String className)
            throws PredicateEvaluatorException {

        Class objClass;
        Object obj = null;

        try {
            objClass = Class.forName(className);
            obj = objClass.newInstance();

        } catch (ClassNotFoundException cnfExc) {
            throw new PredicateEvaluatorException(
                    "PredicateParser: Class not found " + className);
        } catch (IllegalAccessException iaExc) {
            throw new PredicateEvaluatorException(
                    "PredicateParser: Constructor or class not visible "
                            + className);
        } catch (InstantiationException iExc) {
            throw new PredicateEvaluatorException(
                    "PredicateParser: Instantiation Exception " + className
                            + "." + iExc.getMessage());
        }

        return obj;
    }

    /**
     * Fornece a elements de tipos suportados pelo compenente de avalia��o de
     * Predicado
     *
     * @return Lista de Tipos suportados.
     *
     */
    public static Collection supportedTypes()
            throws PredicateEvaluatorException {

        if (types == null)
            loadConfigurationFile();

        return types.values();
    }

    public Predicate parse(String strExpression, Metadata leftMetadata,
            Metadata rightMetadata) throws PredicateEvaluatorException {

        Matcher expMatcher;
        Operator predicateNode;
        String strPredicate;

        expMatcher = expPattern.matcher(strExpression);

        //Se tem algum predicado
        if (!expMatcher.find())
            return null;

        strPredicate = expMatcher.group().trim();
        predicateNode = parsePredicate(strPredicate, leftMetadata,
                rightMetadata);

        Node aux = parseExpression(strExpression, expMatcher, predicateNode,
                leftMetadata, rightMetadata);
        Predicate pred = new Predicate(aux);

        return pred;
    }

    /**
     *
     * M�todo recursivo que realiza o parser dos predicados em uma dada
     * express�o.
     *
     * @param matcher
     *            componente do pacote java.regex que realiza match em strings.
     *            Este deve ter sido iniciado com a express�o sobre a qual se
     *            deseja fazer o parser dos predicados.
     * @param leftMetadata
     *            Metadados da inst�ncia referenciada pelo lado esquerdo dos
     *            predicados
     * @param rightMetadata
     *            Metadados da inst�ncia referenciada pelo lado direito dos
     *            predicados
     *
     * @return Raiz da �rvore de avalia��o.
     */
    private Node parseExpression(String strExpression, Matcher expMatcher,
            Operator leftPredicate, Metadata leftMetadata,
            Metadata rightMetadata) throws PredicateEvaluatorException {

        Operator connectionOperator;//operador que conecta dois predicados
        Operator rightPredicate;
        String strOperator, strPredicate;

        //Verifica se existem mais predicados
        //Neste caso existe um operador de conexao dos predicados. Um operador
        // binario
        if (!expMatcher.find()) //Nao tem mais predicados
            return leftPredicate;

        strOperator = expMatcher.group().trim();
        connectionOperator = instantiateOperator(strOperator, binaryOperators);

        //Verifica se predicado do lado direito existe
        if (!expMatcher.find())
            throw new PredicateEvaluatorException(
                    "PredicateParser: Invalid Expression:" + strExpression);

        //DataUnit predicado do lado direito
        strPredicate = expMatcher.group().trim();
        rightPredicate = parsePredicate(strPredicate, leftMetadata,
                rightMetadata);

        //Arruma links
        connectionOperator.leftChild = leftPredicate;
        connectionOperator.rightChild=rightPredicate;
        leftPredicate.parent=connectionOperator;
        rightPredicate.parent=connectionOperator;

        Node aux = parseExpression(strExpression, expMatcher,
                connectionOperator, leftMetadata, rightMetadata);
        return aux;
    }

    private Operator instantiateOperator(String strOperator,
            Hashtable supOperators) throws PredicateEvaluatorException {

        Operator op;

        op = (Operator) supOperators.get(strOperator.toUpperCase());
        if (op == null) //verifca se operador existe
            throw new PredicateEvaluatorException(
                    "PredicateParser: Invalid Operator: " + strOperator);

        //instancia operador de conexao dos predicados     
        op = op.newInstance();

        return op;
    }

    /**
     * N�o oferece suporte a parentetiza��o e verifica��o de tipo em tempo de
     * compila��o do predicado Operadores devem verificar se tipo de dado
     * recebido no evaluate � suportado.
     *
     * @param strPredicate
     *            Express�o
     * @param leftMetadata
     *            Metadados da inst�ncia referenciada pelo lado esquerdo dos
     *            predicados
     * @param rightMetadata
     *            Metadados da inst�ncia referenciada pelo lado direito dos
     *            predicados
     *
     * @return �rvore de avalia��o que representa a express�o.
     */
    private Operator parsePredicate(String strPredicate, Metadata leftMetadata,
            Metadata rightMetadata) throws PredicateEvaluatorException {

        Matcher predMatcher;
        String aux, strLeftOperand, strRightOperand, strOperator;
        Operator opNode;

        //Quebra predicado e descobre tipo - un�rio ou binario
        predMatcher = predicatePattern.matcher(strPredicate);

        if (!predMatcher.find())
            throw new PredicateEvaluatorException(
                    "PredicateParser: Invalid expression: " + strPredicate);

        aux = predMatcher.group().trim();
        //se for operador, encontramos operador un�rio
        if (unaryOperators.get(aux.toUpperCase()) != null) {

            strOperator = aux;
            if (!predMatcher.find())
                throw new PredicateEvaluatorException(
                        "PredicateParser: Invalid expression: " + strPredicate);
            strLeftOperand = predMatcher.group().trim();

            opNode = instantiateUnaryOperator(strOperator, strLeftOperand,
                    leftMetadata, null);

        } else { //operador binario

            strLeftOperand = aux;

            if (!predMatcher.find())
                throw new PredicateEvaluatorException(
                        "PredicateParser: Invalid expression: " + strPredicate);
            strOperator = predMatcher.group().trim();

            if (!predMatcher.find())
                throw new PredicateEvaluatorException(
                        "PredicateParser: Invalid expression: " + strPredicate);
            strRightOperand = predMatcher.group().trim();

            opNode = instantiateBinaryOperator(strOperator, strLeftOperand,
                    strRightOperand, leftMetadata, rightMetadata, null);
        }

        return opNode;
    }

    /**
     *
     * Realiza a instancia��o de um operador bin�rio assim como seus operandos.
     *
     * @param matcher
     *            componente do pacote java.regex que realiza match em strings.
     *            Este deve ter sido iniciado com a express�o sobre a qual se
     *            deseja fazer o parser dos predicados.
     * @param leftMetadata
     *            Metadados da inst�ncia referenciada pelo lado esquerdo dos
     *            predicados
     * @param rightMetadata
     *            Metadados da inst�ncia referenciada pelo lado direito dos
     *            predicados
     *
     * @return Operator Bin�rio criado a partir do predicado atual no matcher
     */
    private BinaryOperator instantiateBinaryOperator(String strOperator,
            String strleftOperand, String strRightOperand,
            Metadata leftMetadata, Metadata rightMetadata, Node root)
            throws PredicateEvaluatorException {

        DataNode leftOperand, rightOperand;
        BinaryOperator operator;

        //DataUnit operador
        operator = (BinaryOperator) binaryOperators.get(strOperator
                .toUpperCase());
        if (operator == null)
            throw new PredicateEvaluatorException(
                    "PredicateParser: Invalid Expression.");
        operator = (BinaryOperator) operator.newInstance();

        //DataUnit operandos
        leftOperand = instantiateOperand(strleftOperand, leftMetadata);
        rightOperand = instantiateOperand(strRightOperand, rightMetadata);

        //Acerta links
        operator.leftChild=leftOperand;
        operator.rightChild=rightOperand;
        leftOperand.parent = operator;
        rightOperand.parent = operator;

        return operator;
    }

    /**
     *
     *
     */
    private UnaryOperator instantiateUnaryOperator(String strOperator,
            String strOperand, Metadata metadata, Node root)
            throws PredicateEvaluatorException {

        DataNode operand;
        UnaryOperator operator;

        //DataUnit operador
        operator = (UnaryOperator) unaryOperators
                .get(strOperator.toUpperCase());
        if (operator == null)
            throw new PredicateEvaluatorException(
                    "PredicateParser: Invalid Expression.");
        operator = (UnaryOperator) operator.newInstance();

        //DataUnit Operando
        operand = instantiateOperand(strOperand, metadata);

        //Arruma links
        operator.setChild(operand);
        operand.parent = operator;

        return operator;
    }

    /**
     *
     * Realiza a instancia��o de um operando.
     *
     * @param token
     *            String com a defini��o do operando.
     * @param metadata
     *            Metadados da inst�ncia referenciada por este operando.
     *
     * @return Operando representado pelo token.
     */
    private DataNode instantiateOperand(String token, Metadata instanceMetadata)
            throws PredicateEvaluatorException {

        DataNode operand = (DataNode) findNodeType(token, dataNodes.values());

        if (operand == null) {
            throw new PredicateEvaluatorException(
                    "PredicateParser Error. Invalid Operand: " + token);
        }

        if (operand instanceof ValueNode) {

            return ((ValueNode) operand).newInstance(token);

        } else {

            return ((IndexedData) operand).newInstance(token, instanceMetadata);
        }
    }

    private Node findNodeType(String token, Collection nodes)
            throws PredicateEvaluatorException {

        Iterator itNodes;
        Node aux;

        itNodes = nodes.iterator();

        //Identifica qual foi o tipo de operador que fez o match
        while (itNodes.hasNext()) {

            aux = (Node) itNodes.next();
            if (token.matches(aux.getParserRepresentation())) {
                //Achou o tipo
                return aux;
            }
        }
        return null;
    }

//    public static void main(String[] args) throws Exception {
//
//        ColumnNode c = new ColumnNode();
//        Scan scan;
//
//         
//        //String strPred = "PARTICULA.Iteracao<=1,000 OR PARTICULA.Ponto>=3 AND Particula.ponto.id = \"part 1\" and Particula.ponto.x < Particula.ponto.y or Particula.ponto.x > 3.123";
//        String strPred = "Particula.iteracao&&123";
//        
//        System.out.println(strPred);
//
//        String home = System.getProperty("user.home");
//        System.setProperty("PARSER_CONFIG_FILE", home + "\\CODIMS_HOME\\predicateEvaluator.txt");
//
//        DataSourceManager gf = new DataSourceManager(new File(home + "\\CODIMS_HOME"));
//        RelationalOpFactory fr = new RelationalOpFactory(gf);
//
//        scan = (Scan) fr.fabricaroperador("1 SCAN/PARTICULA/-", true,
//                new BlackBoard());
//        scan.open();
//
//        PredicateParser parser = new PredicateParser();
//        Predicate p = parser.parse(strPred, scan.getMetadata(null), scan.getMetadata(null));
//
//        p.print();                
//    }
}
