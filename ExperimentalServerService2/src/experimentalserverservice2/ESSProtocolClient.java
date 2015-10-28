/*
 * This is the client end of the new ESS protocol.
 */

package experimentalserverservice2;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public class ESSProtocolClient
{
    private Logger l;
    
    public ESSProtocolClient(Logger l)
    {
        this.l=l;
    }
    
    public ArrayList<String> getExperiments(String server, int port) throws IOException
    {
        ArrayList<String> experimentList=new ArrayList<String>();
        Socket s=new Socket(server, port);
        Connection c=new Connection(s);
        String buf;
        int numExperiments;
        
        c.out.println("getexperiments");
        
        //Check for server response:
        buf=c.in.readLine();
        
        try
        {
            numExperiments=Integer.parseInt(buf);
        } catch(NumberFormatException e)
        {
            l.logErr("ESSProtocolClient.getExperiments", "ERROR: Server didn't return correct response. Terminating connection.");
            c.close();
            
            return null;
        }
        
        while(true)
        {
            try
            {
                buf=c.in.readLine();
                experimentList.add(buf);
            } catch(SocketException e)
            {
                break;
            }
        }
        
        return experimentList;
    }
    
    public Experiment getData(String server, int port, String experimentName) throws IOException
    {
        Experiment e;
        String buf;
        Socket s=new Socket(server, port);
        Connection c=new Connection(s);
        
        c.out.println("getdata");
        c.out.println(experimentName);
        
        //Check if the server has the experiment:
        buf=c.in.readLine();
        
        if(!buf.equals("OK"))
        {
            //This is bad:
            c.close();
            return null;
        }
        
        //Now try to read the experiment from the stream:
        e=new Experiment(l);
        e.readFromStream(c.rawIn);
        
        c.close();
        
        return e;
    }
    
    public String getDeltaString(String server, int port, String remoteExperiment, Experiment localExperiment) throws IOException
    {
        String outStr="";
        String buf;
        
        Socket s=new Socket(server, port);
        Connection c=new Connection(s);
        
        c.out.println("DELTA");
        c.out.println(remoteExperiment);
        
        buf=c.in.readLine();
        
        if(buf.equals("NOEXIST"))
        {
            return null;
        }
        
        buf=c.in.readLine();
        
        while(!buf.equals("END"))
        {
            outStr += buf;
            buf=c.in.readLine();
        }
        
        return outStr;
    }
}
