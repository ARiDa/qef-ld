/*
* CoDIMS version 1.0 
* Copyright (C) 2006 Othman Tajmouati
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package ch.epfl.codimsd.qeef.datastructure;


// LeftistHeap class
//
// CONSTRUCTION: with a negative infinity sentinel
//
// ******************PUBLIC OPERATIONS*********************
// void insert( x )   --> Insert x
// KeyIdentifiableObject deleteMin( )  --> Return and remove smallest item
// boolean update( )   --> Update the priority of an item. 
// KeyIdentifiableObject findMin( )  --> Return smallest item
// boolean isEmpty( ) --> Return true if empty; else false
// boolean isFull( )  --> Return false in this implementation
// void makeEmpty( )  --> Remove all items
// void merge( rhs )  --> Absorb rhs into this heap

/**
 * Implements a leftist heap. Note that all "matching" is based on the compareTo
 * method.
 * 
 * @author Mark Allen Weiss
 */
public class LeftistHeap  {
    
    private LeftHeapNode root; // root

//    private Hashtable nodes; //Utilizada para procurar por um elemento do
                                  // heap em tempo cste. Utilizado durante
                                  // update

    /**
     * Construct the leftist heap.
     */
    public LeftistHeap() {
        this.root = null;
//        this.nodes = new Hashtable();
    }

    public synchronized void removeAll(){
        root = null;
    }
    
    /**
     * Merge rhs into the priority queue. rhs becomes empty. rhs must be
     * different from this.
     * 
     * @param rhs
     *            the other leftist heap. o(n)
     */
    public synchronized void merge(LeftistHeap rhs) throws Exception{
        
        if (this == rhs) // Avoid aliasing problems
            return;

        root = merge(root, rhs.root);
        rhs.root = null; //liberou memoria desalocando a rhs heap

        //realiza merge das tabela has com os identificadores
        //Copia elementos do array de rhs para este ..... indexacao deve ser
        // respeitada
        //pressupoe elmentos distintos para isso
//        Iterator itRhsNodes = rhs.nodes.values().iterator();
//        Object aux;
//        
//        while( itRhsNodes.hasNext() ) {
//
//            aux = (KeyIdentifiableObject)itRhsNodes.next();
//        
//            if ( nodes.get( new Integer(aux.getKey())) == null) 
//                nodes.put(new Integer(aux.getKey()), aux);
//            else
//                throw new Exception("Duplicated objects into the heap");                    
//        }

    }

    /**
     * Internal method to merge two roots. Deals with deviant cases and calls
     * recursive merge1.
     */
    @SuppressWarnings("unchecked")
	private LeftHeapNode merge(LeftHeapNode h1, LeftHeapNode h2) {
        if (h1 == null)
            return h2;
        if (h2 == null)
            return h1;

        if( h1.element.compareTo( h2.element ) > 0 )
        //if (comparator.compare(h1.element, h2.element) > 0)
            return merge1(h1, h2);
        else
            return merge1(h2, h1);
    }

    /**
     * Internal method to merge two roots. Assumes trees are not empty, and h1's
     * root contains smallest item.
     */
    private LeftHeapNode merge1(LeftHeapNode h1, LeftHeapNode h2) {
        if (h1.left == null) { // Single node
            h2.previous = h1;
            h1.left = h2; // Other fields in h1 already accurate
        } else {
            h1.right = merge(h1.right, h2);
            h1.right.previous = h1;

            if (h1.left.npl < h1.right.npl)
                swapChildren(h1);
            h1.npl = h1.right.npl + 1;
        }
        return h1;
    }

    /**
     * Swaps t's two children.
     */
    private void swapChildren(LeftHeapNode t) {
        LeftHeapNode tmp = t.left;
        t.left = t.right;
        t.right = tmp;
    }

    /**
     * Insert into the priority queue, maintaining heap order.
     * 
     * @param x
     *            the item to insert.
     */
    public synchronized void insert(Comparable x) {
        
        if (x == null)
            return;

        LeftHeapNode newNode = new LeftHeapNode(x);

        //mantem indexacao alternativa em hash
        //para que os objetos possam ser localizados em tempo cste
        //nodes.put(  new Integer(x.getKey()), newNode );

        //insere novo elemento realizando o merge dos elementos
        root = merge(newNode, root);

        //Atualiza previous da raiz .... pode ter sido alterada
        root.previous = null;
        
        notify();
    }

//    private void arbitraryDelete(LeftHeapNode node) {
//
//        LeftHeapNode nh;
//
//        if (node.left != null)
//            node.left.previous = null;
//        if (node.right != null)
//            node.right.previous = null;
//
//        nh = merge(node.left, node.right);
//
//        if (nh != null)
//            nh.previous = null;
//
//        //Se n� a ser deletado nao � raiz
//        if (root != node) {
//
//            if (node.previous.left == node) {
//                node.previous.left = null;
//            } else {
//                node.previous.right = null;
//            }
//
//            //Se previous do n� a ser deletado nao e a raiz
//            if (node.previous != root) {
//                if (node.previous.previous.left == node.previous) //filho
//                                                                  // esquerdo
//                    node.previous.previous.left = merge(node.previous, nh);
//                else
//                    node.previous.previous.right = merge(node.previous, nh);
//            } else {
//
//                root = merge(node.previous, nh);
//            }
//
//        } else {
//            root = nh;
//        }
//
//    }

//    /**
//     * Update the priority of the specified item
//     * 
//     * @param x
//     *            the item to update.
//     * @return true se o elemento est� na heap e foi possivel atualizar seu
//     *         valor false caso contrario
//     */
//    public synchronized boolean update(KeyIdentifiableObject x) {
//        LeftHeapNode node;
//        node = (LeftHeapNode)nodes.get( new Integer(x.getKey()) );
//
//        //Elemento nao esta na heap
//        if (node == null)
//            return false;
//
//        arbitraryDelete(node);
//
//        //Atualiza conteudo
//        node.element = x;
//        node.previous = null;
//        node.left = null;
//        node.right = null;
//
//        //insere no excluido na heap
//        root = merge(root, node);
//
//        //Atualiza previous da raiz .... pode ter sido alterada
//        root.previous = null;
//
//        return true;
//    }

    /**
     * Find the smallest item in the priority queue.
     * 
     * @return the smallest item, or null, if empty.
     */
    public Comparable findMin() {
        if (isEmpty())
            return null;
        return root.element;
    }

    /**
     * Remove the smallest item from the priority queue.
     * 
     * @return the smallest item, or null, if empty.
     */
    public synchronized Comparable deleteMin() {

        while( isEmpty() ){
            try{
                wait();
            }  catch( InterruptedException iExc){
                iExc.printStackTrace();
            }
        }

        Comparable minItem = root.element;
        root = merge(root.left, root.right);

//        nodes.remove( new Integer(minItem.getKey()) ); //remove referencia cruzada

        //Nova raiz deve ter previous apontando para null
        if (root != null)
            root.previous = null;

        return minItem;
    }

    /**
     * Test if the priority queue is logically empty.
     * 
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return root == null ? true : false;
    }

    /**
     * Test if the priority queue is logically full.
     * 
     * @return false in this implementation.
     */
    public boolean isFull() {
        return false;
    }

    /**
     * Make the priority queue logically empty.
     */
    public synchronized void makeEmpty() {
        root = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see datastructure.heap.Heap#print()
     */
//    public boolean isInside(KeyIdentifiableObject obj) {
//        
//        Integer label = new Integer(obj.getKey());
//        
//        if (nodes.get(label) == null)
//            return false;
//        else
//            return true;
//    }

 
    public void print(){
        
        print(root, 0);
    }
    
    private void print(LeftHeapNode node, int space){
        
        if( node!=null ) {
            
            printSpace(space);
	        System.out.println(node); 
	        
	        print(node.left, space+4);
	        print(node.right, space+4);	        
        }        
    }
    
    private void printSpace(int i){
        for(int j=0; j<i; j++)
            System.out.print(" ");
    }
}


