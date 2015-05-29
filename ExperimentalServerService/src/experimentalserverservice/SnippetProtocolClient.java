/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentalserverservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public class SnippetProtocolClient
{
    private static final String SPCLIENT_TAG="SnippetProcotocolClient";
    private Logger l;
    private Parser p;
    
    public SnippetProtocolClient(Logger newLogger)
    {
        l=newLogger;
        p=new Parser(l);
    }
    
    /**
     * Reads until it encounters a </nugget> tag.
     * @param r
     * @return 
     */
    private String readAllXML(BufferedReader r) throws IOException
    {
        String s="";
        String temp;
        
        do
        {
            temp=r.readLine();
            s+=temp;
        } while(!temp.equals("</nugget>"));
        
        return s;
    }
    
    public Experiment getInformation(String serverName, int port, String experimentName) throws NoContentException
    {
        String nugget;
        PrintWriter out;
        BufferedReader in;
        Experiment exp;
        Socket s;
        
        try
        {
            s=new Socket(serverName, port);
            
            out=new PrintWriter(s.getOutputStream());
            in=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            out.println("GETINFO");
            out.println(experimentName);
            
            //Read the info nugget:
            nugget=readAllXML(in);
            
            exp=p.parseNuggetStringForInformation(nugget);
            
            return exp;
        } catch(UnknownHostException e)
        {
            l.logErr(SPCLIENT_TAG, "Couldn't connect to "+serverName+".");
        } catch(IOException ex)
        {
            l.logErr(SPCLIENT_TAG, "IOException while trying to connect to "+serverName+":"+port);
        }
        
        return null;
    }
    
    public ArrayList<String> getExperimentList(long id)
    {
        return null;
    }
    
    /**
     * Connects to an ESS and get the full compliment of URLs defined in an experiment.
     * @return 
     */
    public Experiment getFullSet(long id)
    {
        Experiment exp=null;
        
        return exp;
    }
    
    public long register()
    {
        return 0;
    }
    
    public void unRegister()
    {
        
    }
    
    public DataElement get()
    {
        return null;
    }
}
