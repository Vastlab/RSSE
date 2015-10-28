/*
 * Represents a client for each Experiment.
 */

package experimentclient2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class Client
{
    public int clientId;
    private ArrayList<Integer> objectKeyList;
    
    public Client()
    {
        clientId=0;
        objectKeyList=new ArrayList<Integer>();
    }
    
    /**
     * Inserts a new data element into the list.
     * @param i 
     */
    public void insert(Integer i)
    {
        objectKeyList.add(i);
    }
    
    /**
     * Gets the next data element in the list.
     * @return 
     */
    public Integer getNext()
    {
        if(objectKeyList.isEmpty())
        {
            return null;
        }
        
        else
        {
            return objectKeyList.remove(0);
        }
    }
    
    @Override
    public String toString()
    {
        String s=clientId+" ";
        
        for(int i:objectKeyList)
        {
            s+=clientId+" ";
        }
        
        return s;
    }
    
    public void fromString(String s)
    {
        Scanner sc=new Scanner(s);
        
        clientId=Integer.parseInt(sc.next());
        
        while(sc.hasNext())
        {
            objectKeyList.add(new Integer(sc.next()));
        }
    }
    
    /**
     * Writes the client's state to a stream.
     * @param w 
     */
    public void writeToStream(PrintWriter w)
    {
        w.print(clientId+" ");
        
        for(int i:objectKeyList)
        {
            w.print(clientId+" ");
        }
        
        w.println();
    }
    
    /**
     * Reads the client's state from a stream.
     * @param r
     * @throws IOException 
     */
    public void readFromStream(BufferedReader r) throws IOException
    {
        String clientData;
        Scanner dataScanner;
        
        clientData=r.readLine();
        dataScanner=new Scanner(clientData);
        
        clientId=Integer.parseInt(dataScanner.next());
        
        while(dataScanner.hasNext())
        {
            insert(Integer.parseInt(dataScanner.next()));
        }
    }
}
