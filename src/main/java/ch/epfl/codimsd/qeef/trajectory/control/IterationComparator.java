/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.epfl.codimsd.qeef.trajectory.control;

import ch.epfl.codimsd.qeef.DataUnit;
import java.util.Comparator;

/**
 *
 * @author douglas
 */
public class IterationComparator implements Comparator<DataUnit>{

    public int compare(DataUnit p1, DataUnit p2) {
       /* if(Integer.parseInt(p2.getProperty(ITERATION_NUMBER)) > Integer.parseInt(p1.getProperty(ITERATION_NUMBER)))
            return -1;
        if(Integer.parseInt(p2.getProperty(ITERATION_NUMBER)) < Integer.parseInt(p1.getProperty(ITERATION_NUMBER)))
            return 1;*/
        return 0;
        
    }

}
