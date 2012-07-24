package ch.epfl.codimsd.qeef.trajectory.algebraic;
import java.util.Iterator;

import ch.epfl.codimsd.qeef.BlackBoard;
import ch.epfl.codimsd.qeef.Config;
import ch.epfl.codimsd.qeef.DataUnit;
import ch.epfl.codimsd.qeef.Instance;
import ch.epfl.codimsd.qeef.Metadata;
import ch.epfl.codimsd.qeef.Operator;
import ch.epfl.codimsd.qeef.datastructure.Buffer;
import ch.epfl.codimsd.qeef.relational.Column;
import ch.epfl.codimsd.qeef.types.PointListType;
import ch.epfl.codimsd.qeef.types.Type;
import ch.epfl.codimsd.qep.OpNode;

// Outer Relation - producers[0]
// Inner Relation - producers[1]

public class Unfold extends Operator {

    //==========================================================================================
    //Declara��o de Vari�veis
    //==========================================================================================
    private String columnName;

    private Buffer buffer;

    private int bufferSize, columnOrder;

    private Type newType;
   

    //==========================================================================================
    // Construtores e fun��es utilizadas por ele
    //==========================================================================================

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    public Unfold(int id, OpNode opNode) throws Exception {
        super(id);

        System.out.println("cheguei1");
        this.columnName = (opNode.getParams()[0]).trim().toUpperCase();
        System.out.println("cheguei2");
        this.bufferSize = Integer.parseInt(opNode.getParams()[1].trim());
        System.out.println("cheguei3");
        //this.newType = Config.getDataType(opNode.getParams()[2].trim());
        System.out.println("cheguei4");
    }

    //==========================================================================================
    //Overrrided functions
    //=========================================================================================

    //-----------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------
    public void open() throws Exception {

        super.open();
        
        buffer = new Buffer(bufferSize); 
    }
    
    public void setMetadata(Metadata prdMetadata[]){
        Column data;
        
        metadata[0] = (Metadata)prdMetadata[0].clone();

       /* columnOrder = metadata[0].getDataOrder(this.columnName);
        data = (Column)metadata[0].getData(columnOrder);
        
        data.setType(newType); //Atualiza tipo
      //  data.setSize();//Atualiza tamanho
        
        metadata[0].setData(data, columnOrder);*/
        
    }

    public void close() throws Exception {
        super.close();

        buffer = null;
    }

    //-----------------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------------
    public DataUnit getNext(int consumerId) throws Exception {

   //     System.out.println("UNFOLDNext = " + id + "consumerId"+ consumerId);
        Instance theNext;

        theNext = (Instance)super.getNext(id);

        //Thread.sleep(2000);
        
     /*   if (buffer.isEmpty()) {
            
            theNext = (Instance)super.getNext(id);
            
            if (theNext != null){
                unfold( theNext );
                theNext = (Instance)buffer.get();
            }
            
        } else {
            theNext = (Instance)buffer.get();
        }
        */
        return theNext;
    }
    
    //==========================================================================================
    //Other Functions
    //=========================================================================================
    private void unfold(Instance instance) throws Exception {

        Iterator iterator;
        Instance newInstance;
        PointListType list;
        Type value;

        //Obtem referencia para elements
        list = (PointListType)instance.getData(columnOrder);
        iterator = list.iterator();
        
        //Remove elements da tupla
        instance.updateData(null, columnOrder);

        //Enquanto houver dados na elements
        while (iterator.hasNext()) {

            //Clona tupla
            newInstance = (Instance)instance.clone();

            //Obtem um item da elements
            value = (Type)iterator.next();

            //Seta novo dado
            newInstance.updateData(value, columnOrder);

            buffer.add(newInstance);
        }
    }

}
