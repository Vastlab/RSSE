/*
 * This class is a simple wrapper for a DataElement and a string.
 * It is to be used when posting a response to a server, as both data types are necessary to do so.
 */

package experimentalserverservice;

/**
 *
 * @author mgohde
 */
public class ReturnState
{
    public DataElement e;
    public String s;
    
    public ReturnState()
    {
        e=null;
        s=null;
    }
}
