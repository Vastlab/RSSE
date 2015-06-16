/*
 * This class represents an individual client to be stored in the ClientDB.
 *
 * CurDataIndex represents the index of the URL in the current experiment.
 * This index will be kept throughout experiments by 
 */

package experimentclient;

import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ClientState
{
    public long clientId;
    public int curDataIndex;
    public String expName;
    
    public ClientState()
    {
        
    }
    
    public ClientState(String s)
    {
        Scanner strScanner=new Scanner(s);
        
        clientId=Long.parseLong(strScanner.next());
        curDataIndex=Integer.parseInt(strScanner.next());
    }
    
    @Override
    public String toString()
    {
        String s;
        
        s=clientId+" "+curDataIndex;
        
        return s;
    }
    
    public String otherToString()
    {
        String s;
        
        s=toString()+"\n"+expName;
        
        return s;
    }
}
