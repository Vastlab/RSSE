/*
 * This object represents an individual change between experiments. 
 */

package experimentalserverservice;

/**
 *
 * @author mgohde
 */
public class Delta
{
    /*
     * Explanation for the variables below:
     * 1. added: boolean representing whether the value was added or removed (false==removed, true==added).
     * 2. elemAffected: the DataElement that differs.
     */
    public boolean added;
    public DataElement elemAffected;
    public int indexOfElemAffected;
    
    /**
     * Default constructor.
     */
    public Delta()
    {
        added=false;
        elemAffected=null;
    }
    
    /**
     * Nicer constructor.
     * @param newAdded Represents whether the value was added or removed.
     * @param newElemAffected Represents the element that differs.
     * @param newIndex Index of element that differs.
     */
    public Delta(boolean newAdded, DataElement newElemAffected, int newIndex)
    {
        added=newAdded;
        elemAffected=newElemAffected;
        indexOfElemAffected=newIndex;
    }
}
