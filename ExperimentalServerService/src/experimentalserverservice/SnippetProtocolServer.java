/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentalserverservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public class SnippetProtocolServer
{
    private BufferedReader in;
    private PrintWriter out;
    private ClientDB db;
    private ArrayList<Experiment> experimentList;
    private ArrayList<URLFileManager> fileManagerList;
    
    public SnippetProtocolServer(BufferedReader newIn, PrintWriter newOut, ClientDB newDb, ArrayList<Experiment> newExpList, ArrayList<URLFileManager> newUrlFileManagerList)
    {
        in=newIn;
        out=newOut;
        experimentList=newExpList;
        fileManagerList=newUrlFileManagerList;
        db=newDb;
    }
    
    private String generateInformationNugget(Experiment e)
    {
        String s="";
        
        s+="<nugget>\n";
        s+="\t<title>"+e.name+"</title>\n";
        s+="\t<description>"+e.description+"</description>\n";
        s+="\t<resserver>"+e.resServer+"</resserver>\n";
        s+="\t<resport>"+e.resPort+"</resport>\n";
        s+="</nugget>";
        
        return s;
    }
    
    private String generateExperimentListNugget()
    {
        int i;
        Experiment e;
        String s="";
        
        s+="<nugget>\n";
        
        for(i=0;i<experimentList.size();i++)
        {
            s+="\t<title>"+experimentList.get(i).name+"</title>\n";
        }
        
        s+="</nugget>";
        
        return s;
    }
    
    private String generateUrlNugget(DataElement e)//String url)
    {
        String s="";
        
        s+="<nugget>\n";
        s+="\t<url>"+e.getUrl()+"</url>\n";
        s+="\t<class>"+e.getCls()+"</class>\n";
        
        if(e.isLabeled())
        {
            s+="\t<label>"+e.getLabel()+"</label>\n";
        }
        
        s+="</nugget>";
        
        return s;
    }
    
    private String generateFullUrlNugget(Experiment e)
    {
        int i;
        String s="";
        
        s+="<nugget>\n";
        
        for(i=0;i<e.urlList.size();i++)
        {
            s+="\t<url>"+e.urlList.get(i).getUrl()+"</url>\n";
        }
        
        s+="</nugget>";
        
        return s;
    }
    
    private String generateRegistrationNugget(long idNum)
    {
        String s="";
        
        s+="<nugget>\n";
        s+="\t<id>"+idNum+"</id>\n";
        s+="</nugget>";
        
        return s;
    }
    
    private String generateEmptyMessage()
    {
        return "<nugget>\n</nugget>";
    }
    
    public void serve() throws IOException
    {
        String buffer;
        long newId;
        ClientState tempState;
        int i;
        Experiment tempExp;
        
        //First, determine what the client wants:
        buffer=in.readLine();
        
        //Get it an ID.
        if(buffer.equals("REGISTER"))
        {
            newId=db.generateId();
            ClientState newState=new ClientState();
            newState.clientId=newId;
            newState.curDataIndex=0;
            
            //Check if the client sent along an experiment name:
            buffer=in.readLine();
            
            if(!buffer.equals("end"))
            {
                newState.expName=buffer;
                
                //Remove the end:
                in.readLine();
            }
            
            else
            {
                newState.expName=experimentList.get(0).name;
            }
            
            //Now print out the corresponding nugget:
            out.println(generateRegistrationNugget(newId));
        }
        
        else if(buffer.equals("UNREGISTER"))
        {
            newId=Long.parseLong(in.readLine());
            
            tempState=db.getClientById(newId);
            
            db.removeClientState(tempState);
            
            out.println("OK");
        }
        
        else if(buffer.equals("LIST"))
        {
            in.readLine(); //Get rid of "END"
            out.println(generateExperimentListNugget());
        }
        
        else if(buffer.equals("GETALL"))
        {
            newId=Long.parseLong(in.readLine());
            
            tempState=db.getClientById(newId);
            tempExp=null;
            
            for(i=0;i<experimentList.size();i++)
            {
                if(experimentList.get(i).name.equals(tempState.expName))
                {
                    tempExp=experimentList.get(i);
                    out.println(generateFullUrlNugget(tempExp));
                    break;
                }
            }
            
            if(tempExp==null)
            {
                out.println(generateEmptyMessage());
            }
        }
        
        else if(buffer.equals("GET"))
        {
            newId=Long.parseLong(in.readLine());
            
            tempState=db.getClientById(newId);
            tempExp=null;
            
            for(i=0;i<experimentList.size();i++)
            {
                if(experimentList.get(i).name.equals(tempState.expName))
                {
                    tempExp=experimentList.get(i);
                    break;
                }
            }
            
            if(tempExp!=null)
            {
                if(tempExp.urlList.size()>tempState.curDataIndex)
                {
                    out.println(generateUrlNugget(tempExp.urlList.get(tempState.curDataIndex)));
                }
                
                else
                {
                    out.println(generateEmptyMessage());
                    
                    tempState.curDataIndex++;
                    db.updateClient(tempState);
                }
            }
            
            else
            {
                out.println(generateEmptyMessage());
            }
        }
        
        //RESPOND connections are handled by another protocol.
        
        else if(buffer.equals("GETINFO"))
        {
            buffer=in.readLine();
            
            for(i=0;i<experimentList.size();i++)
            {
                if(experimentList.get(i).name.equals(buffer))
                {
                    out.println(generateInformationNugget(experimentList.get(i)));
                    break;
                }
            }
        }
    }
}
