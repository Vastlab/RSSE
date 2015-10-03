/*
 * This represents an individual element of data to be distributed.
 */

package experimentalserverservice2;

/**
 *
 * @author mgohde
 */
public class DataElement
{
    public String URL;
    public boolean postBack;
    public String postBackAddress;
    public int postBackPort;
    
    public DataElement()
    {
        URL=null;
        postBack=false;
        postBackAddress=null;
        postBackPort=0;
    }
    
    public DataElement(String url, boolean postBack, String postBackAddress, int postBackPort)
    {
        this.URL=url;
        this.postBack=postBack;
        this.postBackAddress=postBackAddress;
        this.postBackPort=postBackPort;
    }
}
