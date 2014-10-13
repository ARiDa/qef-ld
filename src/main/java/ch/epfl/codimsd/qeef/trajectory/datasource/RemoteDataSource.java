package ch.epfl.codimsd.qeef.trajectory.datasource;

import java.io.File;
import java.io.IOException;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.globus.ftp.FTPClient;

import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.relational.TupleMetadata;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;

/**
 * @author Vinicius Fontes
 *
 * @date Apr 1, 2005
 */
public class RemoteDataSource /*extends RelationalBinFileDS*/ {
    
    private String host;
    private int port;
    private String user;
    private String password;
    private String remoteFile;
    private String name;
    
    public RemoteDataSource(String name, String remoteFile, String host, int port, String user, String password, Metadata metadata) throws Exception{

        //Invoca super
      //  super(name, remoteFile, (TupleMetadata)metadata);

        this.name = name;
        this.remoteFile = remoteFile;
        this.user = user;
        this.password = password;
        this.host = host;
        this.port = port;
    }
    
    public void close() throws IOException{
        
    }
    
    public void open() throws IOException {
        try {
            //Realiza o transfer�ncia do arquivo remoto para esta m�quina
            FTPClient ftp = new FTPClient(host, port);
            ftp.authorize(user, password);
            ftp.get(remoteFile, new File(System.getProperty("CODIMS_HOME" + File.separator + name)));
        } catch (ClientException ex) {
            Logger.getLogger(RemoteDataSource.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServerException ex) {
            Logger.getLogger(RemoteDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
