package ch.epfl.codimsd.qeef.trajectory.datasource;

import ch.epfl.codimsd.exceptions.dataSource.CatalogException;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.relational.io.TupleReader;
import ch.epfl.codimsd.connection.CatalogManager;
import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.relational.Tuple;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qeef.util.Constants;
import ch.epfl.codimsd.qeef.relational.Column;
import ch.epfl.codimsd.exceptions.dataSource.DataSourceException;
import ch.epfl.codimsd.qeef.BlackBoard;

import ch.epfl.codimsd.qeef.Instance;
import java.io.*;
import java.util.ArrayList;

/**
 * Fonte de dados relacional implementada via arquivo bin�rio.
 * O formato de armazenamento � o mesmo que o utilizado na serializa��o
 * das Tuplas implementados pelos m�todos write(DataOutputStream, Metadata) e 
 * read( DatadsInputStream, Metadata) da classe QEEF.relational.Tuple.
 * 
 * @author Vinicius Fontes
 * 
 * @date Jun 18, 2005
 */

public class RelationalBinFileDS extends DataSource {
    
    /**
     * Arquivo que armazena dados desta fonte.
     */
    protected File dsFile;

    private TupleReader dsInput;

    private ArrayList<Boolean> key;

    private ArrayList<String> type;

    private ArrayList<Integer> att_size;

    private ArrayList<String> name;

    private String dsName;
    
    private int nrColumns;

    /**
     * @param Nome da fonte de dados.
     * @param dsFile Arquivo com os dados desta fonte.
     * @param dsMetadata Metadado que descreve o formato da fonte de dados.
     * 
     * @throws FileNotFoundException Se o arquivo com os dados da fonte (dsFile) n�o existir.
     */
    public RelationalBinFileDS(String alias, TupleMetadata metadata, String typeFile, String numberOfColums) throws FileNotFoundException, CatalogException, DataSourceException{

        // Call super constructor.
	super(alias, null);

        this.name = new ArrayList();
        this.type = new ArrayList();
        this.att_size = new ArrayList();
        this.key = new ArrayList();
        this.dsName = alias;
        
        alias = alias.toUpperCase();
	nrColumns = Integer.parseInt(numberOfColums);
        String filePath;

	CatalogManager catalogManager = CatalogManager.getCatalogManager();
        filePath = (String) catalogManager.getSingleObject("ds_table", "filename", "name='"+alias+"'");
        this.dsFile = new File(filePath);

        for(int i = 1; i <= nrColumns; i++)
        {
            String name = (String) catalogManager.getSingleObject("ds_attributes", "name", "tb_name='"+alias+"'AND id_attribute="+i+"");
            this.name.add(name);

            String type = (String) catalogManager.getSingleObject("ds_attributes", "type", "tb_name='"+alias+"'AND id_attribute="+i+"");
            this.type.add(type);
            
            Integer att_size = (Integer) catalogManager.getSingleObject("ds_attributes", "att_size", "tb_name='"+alias+"'AND id_attribute="+i+"");
            this.att_size.add(att_size);
            
            String key = (String) catalogManager.getSingleObject("ds_attributes", "keyP", "tb_name='"+alias+"' AND id_attribute="+i+"");
            key = key.trim();
            this.key.add(Boolean.parseBoolean(key));
        }

        try {
            // Construct the metadata of the ResultSet in a TupleMetadata object.
            metadata = new TupleMetadata();

            for (int i = 1; i <= nrColumns; i++) {
                    this.name.set(i-1, this.name.get(i-1).trim());
                    Column column = new Column(dsName + "." + this.name.get(i-1), Config.getDataType(this.type.get(i-1)), this.att_size.get(i-1), i, this.key.get(i-1));
                    metadata.addData(column);
            }

            setMetadata(metadata);
            // Put Metadata of the dataSource in the BlackBoard.
            BlackBoard bl = BlackBoard.getBlackBoard();
            bl.put("Metadata", metadata);

        } catch (Exception ex) {
		throw new DataSourceException("Exception while opening the DataSource : " + ex.getMessage());
	}
    }

    /**
     * Inicializa o fluxo de leitura para o arquivo onde os dados desta fonte se encontra.
     * @throws IOException Se algum erro acontecer durante a leitura do arquivo.
     * 
     * @see DataSource#open()
     */
    public void open() throws IOException, DataSourceException {

            FileInputStream file =  new FileInputStream(dsFile);
            dsInput = new TupleReader(file, (TupleMetadata)metadata);
    }

    /**
     * Encerra o fluxo de leitura para o arquivo desta fonte.
     * @throws IOException Se algum erro acontecer no encerramento do fluxo de leitura do arquivo desta fonte de dados.
     * 
     * @see DataSource#close()
     */
    public void close() throws IOException, DataSourceException{

        dsInput.close();
    }

    /**
     * Realiza a leitura de uma tuple desta fonte de dados. Null se n�o existir mais tuplas.
     * @throws IOException Se algum erro acontecer durante a leitura da fonte de dados.
     * 
     * @see DataSource#read()
     */
    public DataUnit read() throws IOException, DataSourceException{

     //   Instance t;
        if(!dsInput.eof())
        {
         //  t = dsInput.readInstance();
        //   System.out.println("dsName = "+ dsName + "instance = " + t);
           return dsInput.readInstance();
        }
        else
            return  null;
    }

    
    /**
     * Obtem arquivo que armazena os dados desta fonte. 
     * @return Arquivo que armazena os dados desta fonte de dados.
     */
    public File getFile() {
        return (dsFile);
    }
}