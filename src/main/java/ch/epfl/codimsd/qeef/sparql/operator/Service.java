package ch.epfl.codimsd.qeef.sparql.operator;

import java.util.Map;

import org.apache.log4j.Logger;

import ch.epfl.codimsd.qeef.Access;
import ch.epfl.codimsd.qeef.DataSource;
import ch.epfl.codimsd.qeef.DataSourceManager;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qep.OpNode;

/**
 * 
 * @author Regis Pires Magalhaes
 *
 */
public class Service extends Access {

    protected static Logger logger = Logger.getLogger(Service.class.getName());

    public Service(int id, OpNode op) {
        super(id, op);
        DataSourceManager dsManager = DataSourceManager.getDataSourceManager();
        this.dataSource = (DataSource) dsManager.getDataSource(op.getOpTimeStamp());
    }

    public void open() throws Exception {
        logger.debug("Service open");
        super.open();
    }

    public DataUnit getNext(int consumerId) throws Exception {
        this.instance = this.dataSource.read();
        logger.debug("Service.getNext: " + this.instance);
        return this.instance;
    }

    @Override
    public Operator cloneOperator(Map<String, Object> params) throws Exception {
    	Service op = (Service)super.cloneOperator(params);
    	op.dataSource = this.dataSource.cloneDatasource(params);
    	return op;
    }

    @Override
    public void sendMessage(Map<String, Object> params) {
    	this.dataSource.processMessage(params);
    }
}
