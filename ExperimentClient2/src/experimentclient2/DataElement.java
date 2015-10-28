/*
 * This represents an individual element of data to be distributed.
 */

package experimentclient2;

import java.util.Scanner;

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
    
    @Override
    public String toString()
    {
        String s="";
        s+=URL+" ";
        s+=postBack+" ";
        s+=postBackAddress+" ";
        s+=postBackPort;
        
        return s;
    }
    
    public void fromString(String s)
    {
        Scanner strScanner=new Scanner(s);
        
        URL=strScanner.next();
        postBack=strScanner.nextBoolean();
        postBackAddress=strScanner.next();
        postBackPort=strScanner.nextInt();
    }
}
