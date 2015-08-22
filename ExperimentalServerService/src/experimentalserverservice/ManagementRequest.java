/*
 * This class represents an individual request to be sent to the ManagementServer portion of
 * the ESS. 
 */

package experimentalserverservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ManagementRequest
{
    public int reqType;
    public String reqArgs;
    
    public static final int STOP=1; //Shuts down the entire program. 
    public static final int RELOAD_CONF=2; //Reloads configuration 
    public static final int STANDBY=3; //Shuts down all servers but the management server.
    public static final int MERGE_NEW=4; //Sent to prepare the ESS for a FileUpdate request.
    public static final int RESTART=5; //Restarts the program after it was suspended.
    public static final int INIT=6;
    
    @Override
    public String toString()
    {
        String s;
        
        s=reqType+"\n"+reqArgs;
        
        return s;
    }
    
    public void parse(BufferedReader r) throws IOException
    {
        reqType=Integer.parseInt(r.readLine());
        reqArgs=r.readLine();
    }
}
