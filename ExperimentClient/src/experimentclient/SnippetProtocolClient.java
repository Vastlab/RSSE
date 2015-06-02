/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentclient;

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
            
            out=new PrintWriter(s.getOutputStream(), true);
            in=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            out.println("GETINFO");
            out.println(experimentName);
            
            //Read the info nugget:
            nugget=readAllXML(in);
            
            exp=p.parseNuggetStringForInformation(nugget);
            
            out.close();
            in.close();
            
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
    
    /**
     * Connect to an ESS and list all experiments.
     * @param serverName
     * @param port
     * @param id
     * @return 
     */
    public ArrayList<String> getExperimentList(String serverName, int port)//, long id)
    {
        String expListNugget;
        PrintWriter out;
        BufferedReader in;
        ArrayList<String> returnList;
        Socket s;
        
        try
        {
            s=new Socket(serverName, port);
            
            out=new PrintWriter(s.getOutputStream(), true);
            in=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            out.println("LIST");
            
            //Read the info nugget:
            expListNugget=readAllXML(in);
            
            returnList=p.parseExperimentsFromString(expListNugget);
            
            out.close();
            in.close();
            
            return returnList;
        } catch(UnknownHostException e)
        {
            l.logErr(SPCLIENT_TAG, "Couldn't connect to "+serverName+".");
        } catch(IOException ex)
        {
            l.logErr(SPCLIENT_TAG, "IOException while trying to connect to "+serverName+":"+port);
        }
        
        return null;
    }
    
    /**
     * Connects to an ESS and get the full compliment of URLs defined in an experiment.
     * @return 
     */
    public ArrayList<String> getFullSet(String serverName, int port, long id)
    {
        String expListNugget;
        PrintWriter out;
        BufferedReader in;
        ArrayList<String> returnList;
        Socket s;
        
        try
        {
            s=new Socket(serverName, port);
            
            out=new PrintWriter(s.getOutputStream(), true);
            in=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            out.println("GETALL");
            out.println(id);
            
            //Read the info nugget:
            expListNugget=readAllXML(in);
            
            returnList=p.parseExperimentsFromString(expListNugget);
            
            out.close();
            in.close();
            
            return returnList;
        } catch(UnknownHostException e)
        {
            l.logErr(SPCLIENT_TAG, "Couldn't connect to "+serverName+".");
        } catch(IOException ex)
        {
            l.logErr(SPCLIENT_TAG, "IOException while trying to connect to "+serverName+":"+port);
        }
        
        return null;
    }
    
    public long register(String serverName, int port, String expName)
    {
        long valObtained;
        PrintWriter out;
        BufferedReader in;
        String nugget;
        Socket s;
        
        try
        {
            s=new Socket(serverName, port);
            
            out=new PrintWriter(s.getOutputStream(), true);
            in=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            out.println("REGISTER");
            System.out.println(expName);
            out.println(expName);
            out.println("end");
            
            //Read the info nugget:
            nugget=readAllXML(in);
            
            valObtained=p.parseIdNugget(nugget);
            
            out.close();
            in.close();
            
            return valObtained;
        } catch(UnknownHostException e)
        {
            l.logErr(SPCLIENT_TAG, "Couldn't connect to "+serverName+".");
        } catch(IOException ex)
        {
            l.logErr(SPCLIENT_TAG, "IOException while trying to connect to "+serverName+":"+port);
        }
        
        return -1;
    }
    
    public void unRegister(String serverName, int port, long id)
    {
        PrintWriter out;
        BufferedReader in;
        Socket s;
        
        try
        {
            s=new Socket(serverName, port);
            
            out=new PrintWriter(s.getOutputStream(), true);
            in=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            out.println("UNREGISTER");
            out.println(id);
            
            //Read server response:
            if(in.readLine().equals("OK"))
            {
                l.logMsg(SPCLIENT_TAG, "Successfully unregistered "+id);
            }
            
            else
            {
                l.logMsg(SPCLIENT_TAG, "Couldn't successfully unregister "+id+". ID may be on wrong server.");
            }
        } catch(UnknownHostException e)
        {
            l.logErr(SPCLIENT_TAG, "Couldn't connect to "+serverName+".");
        } catch(IOException ex)
        {
            l.logErr(SPCLIENT_TAG, "IOException while trying to connect to "+serverName+":"+port);
        }
    }
    
    public DataElement get(String serverName, int port, long id)
    {
        PrintWriter out;
        BufferedReader in;
        Socket s;
        String nugget;
        DataElement returnElement;
        
        try
        {
            s=new Socket(serverName, port);
            
            out=new PrintWriter(s.getOutputStream(), true);
            in=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            out.println("GET");
            out.println(id);
            
            nugget=readAllXML(in);
            
            returnElement=p.parseForDataElement(nugget);
            
            out.close();
            in.close();
            
            return returnElement;
        } catch(UnknownHostException e)
        {
            l.logErr(SPCLIENT_TAG, "Couldn't connect to "+serverName+".");
        } catch(IOException ex)
        {
            l.logErr(SPCLIENT_TAG, "IOException while trying to connect to "+serverName+":"+port);
        }
        
        return null;
    }
}
