package ch.epfl.codimsd.qeef.trajectory.datasource;

import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataSourceManager;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.*;
import ch.epfl.codimsd.qeef.relational.io.TupleWriter;
import ch.epfl.codimsd.qeef.types.Type;
import java.io.*;
import java.util.*;

public class OldFonteBin extends DataSource {
    private DataInputStream in;     

    public static final int TIPO_STRING = 1;

    public static final int TIPO_INTEGER = 2;

    public static final int TIPO_FLOAT = 3;

    public static final int TIPO_POINT = 7;

    public static final int TIPO_POINT_LIST = 8;

    public static final int BUFFER_SIZE = 8192;
    
    /**
     * Arquivo que armazena dados desta fonte.
     */
    protected File dsFile;
    

    public OldFonteBin(String nom, String arq, Metadata m) {
        super(nom, m);
        
        dsFile = new File(arq);
    }

    public void open() throws Exception {
        try {
            
            //Log.println(" FONTE "+obterapelido()+" - open ");
            in = new DataInputStream(new BufferedInputStream(
                    new FileInputStream(dsFile), BUFFER_SIZE));
            
        } catch (FileNotFoundException e) {
            System.out.println("Erro - arquivo inexistente="
                    + dsFile);
            System.exit(0);
        }
    }

    public void close() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading URI: " + e.getMessage());
        }
        //		Log.println("FONTE "+obterapelido()+" - close");
    }


    public DataUnit read() throws IOException {

        Iterator itAttributes;
        Column currAttribute; 
        Type qeefValue = null;
        Tuple tuple;
        
        //No more tuples
        if( !in.readBoolean() )
        	return null;
        	
        tuple = new Tuple();
        itAttributes = metadata.getData().iterator();
        while ( itAttributes.hasNext() ) {

	        currAttribute = (Column)itAttributes.next();
	        qeefValue = currAttribute.getType().newInstance();
	        qeefValue.read(in);
	        
	        tuple.addData(qeefValue);
        }
        
        return tuple;
    }


//    public static void main(String[] args) throws Exception {
//
//        Config.setProperty("QEEF_HOME", new File("C:\\Documents and Settings\\Vinicius Fontes\\CODIMS_HOME"));
//        
//        DataSourceManager dsm = new DataSourceManager(new File("C:\\Documents and Settings\\Vinicius Fontes\\CODIMS_HOME\\metabase.txt"));
//        RelationalBinFileDS aux;
//        DataUnit t;
//        TupleWriter tw;
//        OldFonteBin ds;
//        
//        
//        aux = (RelationalBinFileDS)dsm.getDataSource("Particula");
//        ds = new OldFonteBin(aux.getAlias(), aux.getFile()+"", (TupleMetadata)aux.getMetadata());
//        tw = new TupleWriter(new FileOutputStream("c:\\temp\\fontes\\part.qds2"), aux.getMetadata());
//        
//        ds.open();
//        
//        while((t=ds.read()) != null)
//            tw.writeInstance(t);
//        
//        ds.close();
//        tw.close();
//        
//        aux = (RelationalBinFileDS)dsm.getDataSource("Velocidade");
//        ds = new OldFonteBin(aux.getAlias(), aux.getFile()+"", (TupleMetadata)aux.getMetadata());
//        tw = new TupleWriter(new FileOutputStream("c:\\temp\\fontes\\velocidade.qds2"), aux.getMetadata());
//        
//        ds.open();
//        
//        while((t=ds.read()) != null)
//            tw.writeInstance(t);
//        
//        ds.close();
//        tw.close();
//        
//        aux = (RelationalBinFileDS)dsm.getDataSource("Tetraedro");
//        ds = new OldFonteBin(aux.getAlias(), aux.getFile()+"", (TupleMetadata)aux.getMetadata());
//        tw = new TupleWriter(new FileOutputStream("c:\\temp\\fontes\\Tetraedro.qds2"), aux.getMetadata());
//        
//        ds.open();
//        
//        while((t=ds.read()) != null)
//            tw.writeInstance(t);
//        
//        ds.close();
//        tw.close();
//        
//
//    }
}







