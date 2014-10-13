package ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.datanodes;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.DataNode;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateEvaluatorException;
import ch.epfl.codimsd.qeef.trajectory.predicate.evaluator.PredicateParser;
import ch.epfl.codimsd.qeef.types.Type;

/**
 * Classe que representa uma constante de um predicado na �rvore de avalia��o.
 * Por exemplo, no predicado Aluno.nome = "Vinicius" este n� armazenaria o valor
 * "Vinicius".
 * 
 * @author Vin�cius Fontes Vieira da Silva
 * 
 * @date 01 FEV 2005
 */

public class ValueNode extends DataNode {

    /**
     * Define uma express�o regular para o reconhecimento de constantes nas
     * express�es.
     */
    private static String recognitionPattern = null;;

    /**
     * O valor representado por este n�.
     */
    protected Type value;

    /**
     *  
     */
    public ValueNode() {

    }

    /**
     * 
     * @param value
     *            Valor desta constante.
     */
    public ValueNode(Type value) {
        super();
        this.value = value;
    }

    /**
     * 
     * Cria uma nova inst�ncia desta classe.
     * 
     * @param value
     *            Valor desta constante.
     */
    public DataNode newInstance(String value)
            throws PredicateEvaluatorException {
        return new ValueNode(instantiateValue(value));
    }

    /**
     * Cria uma inst�ncia de um tipo QEEF que encapsular� o valor representado
     * por value.
     * 
     * @param String
     *            que representa um valor da constante que este n� representa.
     *            Segue o formato de algum tipo definido.
     * 
     * @return Valor representado por value em um tipo QEEF.
     * 
     */
    private Type instantiateValue(String value)
            throws PredicateEvaluatorException {

        Collection supportedTypes;
        Iterator itSupportedTypes;
        Type aux = null;

        supportedTypes = PredicateParser.supportedTypes();
        itSupportedTypes = supportedTypes.iterator();

        //Identifica qual foi o tipo de operador que fez o match
        while (itSupportedTypes.hasNext()) {

            aux = (Type) itSupportedTypes.next();
            if (value.matches(aux.recognitionPattern())) {
                //Achou o tipo
                break;
            }
            aux = null;
        }

        if (aux == null) {

            throw new PredicateEvaluatorException("ValueNode: Value " + value
                    + " doesn't match any Type pattern.");
        }

        aux = aux.newInstance();
        aux.setValue(value);

        return aux;
    }

    /**
     * @see QEEF.predicate.evaluator.Node#getParserRepresentation()
     */
    public String getParserRepresentation() throws PredicateEvaluatorException {

        if (recognitionPattern == null) {
            Collection supportedTypes;
            Iterator itTypes;
            Type aux;

            supportedTypes = PredicateParser.supportedTypes();
            itTypes = supportedTypes.iterator();
            recognitionPattern = "";

            while (itTypes.hasNext()) {

                aux = (Type) itTypes.next();
                recognitionPattern += aux.recognitionPattern() + "|";

            }

            recognitionPattern = recognitionPattern.substring(0,
                    recognitionPattern.length() - 1);
            recognitionPattern = "(" + recognitionPattern + ")";
        }

        return recognitionPattern;
    }

    /**
     * @see QEEF.predicate.evaluator.Node#evaluate(DataUnit, DataUnit)
     */
    public Object evaluate(Instance inst1, Instance inst2)
            throws PredicateEvaluatorException {

        return apply();
    }

    /**
     * 
     * Obtem o valor da constante representada por este n�.
     * 
     * @return Inst�ncia de QEEF.types.Type que representa o valor desta constante.
     *  
     */
    public Object apply() {
        return value;
    }

    /**
     *  @see Object#toString()
     */
    public String toString() {
        return "ValueNode value " + value;
    }

    public static void main(String[] args) throws PredicateEvaluatorException {

        String home = System.getProperty("user.home");
        System.setProperty("CODIMS_HOME", home + "\\CODIMS_HOME");

        ValueNode e = new ValueNode();
        Pattern p = Pattern
                .compile("(([0-9]+([,][0-9]+)*)|(([0-9]+([,][0-9]+)*)([.][0-9]+))|([\"][\\p{ASCII}][^\"]*[\"])");
        Matcher m = p.matcher("\"binidsf");

        System.out.println(e.getParserRepresentation());
        while (m.find())
            System.out.println(m.group());
    }
}
