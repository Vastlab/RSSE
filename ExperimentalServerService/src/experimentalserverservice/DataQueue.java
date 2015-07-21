/*
 * This class wraps all functionality for managing a client's current
 * data fetch index into a convenient queue structure.
 */

package experimentalserverservice;

import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public class DataQueue
{
    //This class uses an ArrayList at the present due to ease of use.
    private ArrayList<DataElement> internalList;
    
    /**
     * Generic constructor.
     */
    public DataQueue()
    {
        internalList=new ArrayList<DataElement>();
    }
    
    /**
     * Adds a dataelement to the client's set.
     * @param e 
     */
    public void insert(DataElement e)
    {
        internalList.add(e);
    }
    
    /**
     * Removes a dataelement from the client's set and returns it.
     * @return dataelement
     */
    public DataElement remove()
    {
        return internalList.remove(0);
    }
    
    /**
     * Gets the number of elements in the client's set.
     * @return 
     */
    public int getSize()
    {
        return internalList.size();
    }
    
    /**
     * Gets an individual dataelement.
     * @param index
     * @return 
     */
    public DataElement get(int index)
    {
        return internalList.get(index);
    }
}
