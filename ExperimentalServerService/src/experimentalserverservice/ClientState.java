/*
 * This class represents an individual client to be stored in the ClientDB.
 *
 * CurDataIndex represents the index of the URL in the current experiment.
 * This index will be kept throughout experiments by 
 */

package experimentalserverservice;

import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ClientState
{
    public long clientId;
    public int curDataIndex;
    public ArrayList<Integer> dataMap; /* This is the map of data elements to give the client. */
    //public DataQueue dataQueue;
    public String expName;
    
    public ClientState()
    {
        dataMap=new ArrayList<Integer>();
        //dataQueue=new DataQueue();
    }
    
    public ClientState(String s)
    {
        Scanner strScanner=new Scanner(s);
        
        clientId=Long.parseLong(strScanner.next());
        curDataIndex=Integer.parseInt(strScanner.next());
    }
    
    public ClientState(String s, String newExpName)
    {
        Scanner strScanner=new Scanner(s);
        
        clientId=Long.parseLong(strScanner.next());
        curDataIndex=Integer.parseInt(strScanner.next());
        expName=newExpName;
    }
    
    @Override
    public String toString()
    {
        String s;
        
        s=clientId+" "+curDataIndex;
        
        return s;
    }
}
