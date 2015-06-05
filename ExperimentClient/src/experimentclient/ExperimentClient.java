/*
 * The experiment client consists mostly of existing codebases just with the added bonus of being able to communicate with
 * a caching server.
 */

package experimentclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ExperimentClient
{
    private static final String NAME="java -jar EC.jar";
    private static final String EC_TAG="ExperimentClient";
    
    private static final int CONNECTION_TYPE_NEXT=1;
    private static final int CONNECTION_TYPE_ALL=2;
    private static final int CONNECTION_TYPE_REGISTER=3;
    private static final int CONNECTION_TYPE_LIST=4;
    private static final int CONNECTION_TYPE_GETINFO=5;
    
    private static boolean initial=false;
    private static boolean saveConfig=false;
    private static String serverName=null;
    private static int serverPort=-1;
    private static int connectionType=0;
    private static String dir=".";
    private static int cacheModulePort=9001;
    private static String cacheModuleName=null;
    private static boolean configSet=false;
    private static CacheRequestProtocolClient cacheClient;
    private static SnippetProtocolClient snippetClient;
    private static ResponseProtocolClient responseClient;
    private static Logger l;
    private static String expName;
    
    
    
    public static void printHelp()
    {
        System.out.println("Usage: "+NAME+" [args]");
        System.out.println("Connect to Experiment Servers and fetch data.");
        System.out.println("\t-h, --help\tDisplay this message.");
        System.out.println("\t-i, --initial\tRegister with the Experiment Server.");
        System.out.println("\t--genconfig\tGenerate a configuration file for an experiment.");
        System.out.println("\t-s, --server\tUse the server specified rather than the one configured.");
        System.out.println("\t-p, --port\tUse the port specified rather than the one configured.");
        System.out.println("\t-l, --list\tList all experiments hosted by the Experiment Server.");
        System.out.println("\t-d, --usedir\tUse the specified directory instead of .");
        System.out.println("\t-n, --next\tFetch the next file from the server.");
        System.out.println("\t-a, --all\tFetch all files from the server.");
        System.out.println("\t-cp, --cacheprt\tConnect to the Cache Module on the port specified.");
        System.out.println("\t-cs, --cachesrv\tConnect to the Cache Module at the address specified.");
        System.out.println("\t-r, --respond\tSend an XML response to a the Experiment Server");
        System.out.println("\t-e, --expname\tExperiment name to use.");
        System.out.println("\t-g, --getinfo\tGet information about an experiment.");
    }
    
    public static void writeConfig()
    {
        
    }
    
    public static void parseArgs(String[] args)
    {
        int i;
        
        for(i=0;i<args.length;i++)
        {
            if(args[i].equals("-h")||args[i].equals("--help"))
            {
                printHelp();
                System.exit(1);
            }
            
            else if(args[i].equals("-i")||args[i].equals("--initial"))
            {
                initial=true;
                connectionType=CONNECTION_TYPE_REGISTER;
            }
            
            else if(args[i].equals("--genconfig"))
            {
                saveConfig=true;
            }
            
            else if(args[i].equals("-s")||args[i].equals("--server"))
            {
                if(args.length<=(i+1))
                {
                    System.err.println("Server not specified!");
                    System.exit(1);
                }
                
                else
                {
                    configSet=true;
                    serverName=args[i+1];
                    i++;
                }
            }
            
            else if(args[i].equals("-p")||args[i].equals("--port"))
            {
                if(args.length<=(i+1))
                {
                    System.err.println("Server port not specified!");
                    System.exit(1);
                }
                
                else
                {
                    serverPort=Integer.parseInt(args[i+1]);
                    i++;
                }
            }
            
            else if(args[i].equals("-l")||args[i].equals("--list"))
            {
                connectionType=CONNECTION_TYPE_LIST;
            }
            
            else if(args[i].equals("-d")||args[i].equals("--usedir"))
            {
                if(args.length<=(i+1))
                {
                    System.err.println("Directory not specified!");
                    System.exit(1);
                }
                
                else
                {
                    dir=args[i+1];
                    i++;
                }
            }
            
            else if(args[i].equals("-n")||args[i].equals("--next"))
            {
                connectionType=CONNECTION_TYPE_NEXT;
            }
            
            else if(args[i].equals("-a")||args[i].equals("--all"))
            {
                connectionType=CONNECTION_TYPE_ALL;
            }
            
            else if(args[i].equals("-cp")||args[i].equals("--cacheprt"))
            {
                if(args.length<=(i+1))
                {
                    System.err.println("Cache module port not specified!");
                    System.exit(1);
                }
                
                else
                {
                    cacheModulePort=Integer.parseInt(args[i+1]);
                    i++;
                }
            }
            
            else if(args[i].equals("-cs")||args[i].equals("--cachesrv"))
            {
                if(args.length<=(i+1))
                {
                    System.err.println("Cache module hostname not specified!");
                    System.exit(1);
                }
                
                else
                {
                    configSet=true;
                    cacheModuleName=args[i+1];
                    i++;
                }
            }
            
            else if(args[i].equals("-r")||args[i].equals("--respond"))
            {
                
            }
            
            else if(args[i].equals("-g")||args[i].equals("--getinfo"))
            {
                connectionType=CONNECTION_TYPE_GETINFO;
            }
            
            else if(args[i].equals("-e")||args[i].equals("--expname"))
            {
                if(args.length<=(i+1))
                {
                    System.err.println("Experiment name not set!");
                    System.exit(1);
                }
                
                else
                {
                    expName=args[i+1];
                    i++;
                }
            }
            
            else
            {
                System.err.println("Unrecognized argument: "+args[i]);
            }
        }
    }
    
    public static void saveConfig()
    {
        File confFile=new File(dir+"/config");
        
        if(!confFile.exists())
        {
            try
            {
                confFile.createNewFile();
            } catch(IOException e)
            {
                
            }
        }
        
        try
        {
            PrintWriter out=new PrintWriter(confFile);
            
            out.println("servername "+serverName);
            out.println("serverport "+serverPort);
            out.println("cachename "+cacheModuleName);
            out.println("cacheport "+cacheModulePort);
            
            out.flush();
            out.close();
        } catch(FileNotFoundException e)
        {
            System.err.println("Couldn't create local configuration file! Exiting...");
            System.exit(1);
        }
        
    }
    
    public static void readConfig()
    {
        String temp;
        File confFile=new File(dir+"/config");
        
        try
        {
            Scanner s=new Scanner(confFile);
            
            while(s.hasNext())
            {
                temp=s.next();
                
                if(temp.equals("servername"))
                {
                    serverName=s.next();
                }
                
                else if(temp.equals("serverport"))
                {
                    serverPort=Integer.parseInt(s.next());
                }
                
                else if(temp.equals("cachename"))
                {
                    cacheModuleName=s.next();
                }
                
                else if(temp.equals("cacheport"))
                {
                    cacheModulePort=Integer.parseInt(s.next());
                }
            }
        } catch(FileNotFoundException e)
        {
            System.err.println("Couldn't read configuration! Please generate a new config and try again.");
            System.exit(1);
        }
    }
    
    private static long clientId=-1;
    
    public static ClientState loadClientState()
    {
        ClientState state;
        Scanner fileScanner;
        File inputFile;
        
        inputFile=new File(dir+"/registration.info");
        state=null;
        
        try
        {
            fileScanner=new Scanner(inputFile);
            
            state=new ClientState(fileScanner.nextLine());
            
            fileScanner.close();
            
            clientId=state.clientId;
            expName=state.expName;
        } catch(FileNotFoundException e)
        {
            System.err.println("Couldn't load registration info. Please run \""+NAME+" -i\"");
            System.exit(1);
        }
        
        return state;
    }
    
    public static void saveClientState(ClientState state)
    {
        PrintWriter out;
        File outputFile;
        
        outputFile=new File(dir+"/registration.info");
        
        if(!outputFile.exists())
        {
            try
            {
                outputFile.createNewFile();
            } catch(IOException e)
            {
                System.err.println("Couldn't create new registration information file!");
                System.exit(1);
            }
        }
        
        try
        {
            out=new PrintWriter(outputFile);
            
            out.println(state);
            
            out.flush();
            out.close();
        } catch(FileNotFoundException e)
        {
            
        }
    }
    
    public static void connectNext()
    {
        DataElement de;
        File f;
        
        if(clientId==-1)
        {
            loadClientState();
        }
        
        de=snippetClient.get(serverName, serverPort, clientId);
        
        if(de.getUrl()!=null)
        {
            System.out.println("Got next url: "+de.getUrl());
            System.out.println("Fetching...");
            
            try
            {
                cacheClient=new CacheRequestProtocolClient(cacheModuleName, cacheModulePort, l, dir);

                f=cacheClient.connectExplicitRemote(de.getUrl());
                System.out.println("File: "+f);
            } catch(CacheErrorException e)
            {
                System.err.println("Error while caching data.");
            }
        }
        
        else
        {
            System.exit(1);
        }
    }
    
    public static void connectAll()
    {
        ArrayList<String> urlList;
        String s;
        int i;
        
        if(clientId==-1)
        {
            loadClientState();
        }
        
        urlList=snippetClient.getFullSet(serverName, serverPort, clientId);
        
        for(i=0;i<urlList.size();i++)
        {
            s=urlList.get(i);
            System.out.println("Contents of s: "+s);
            System.out.println("done");
            
            System.out.println("Fetching: "+s);
            
            try
            {
                cacheClient=new CacheRequestProtocolClient(cacheModuleName, cacheModulePort, l, dir);
                System.out.println("File: "+cacheClient.connectExplicitRemote(s));
            } catch(CacheErrorException e)
            {
                System.err.println("Error while caching data.");
            }
        }
    }
    
    public static void connectRegister()
    {
        ClientState state;
        long newId;
        
        newId=snippetClient.register(serverName, serverPort, expName);
        
        state=new ClientState();
        state.clientId=newId;
        state.expName=expName;
        state.curDataIndex=0;
        
        //Now save the newly obtained state:
        saveClientState(state);
        
        System.out.println("Successfully registered with client id: "+newId);
    }
    
    public static void connectList()
    {
        ArrayList<String> experimentList;
        int i;
        
        experimentList=snippetClient.getExperimentList(serverName, serverPort);
        
        if(experimentList.isEmpty())
        {
            System.out.println("No experiments sent from remote server.");
            System.exit(0);
        }
        
        else
        {
            System.out.println("Listing "+experimentList.size()+" experiment(s).");
            
            for(i=0;i<experimentList.size();i++)
            {
                System.out.println(experimentList.get(i));
            }
        }
    }
    
    public static void connectRespond()
    {
        //Not yet!
    }
    
    public static void connectGetInfo()
    {
        Experiment e;
        
        try
        {
            e=snippetClient.getInformation(serverName, serverPort, expName);
            
            System.out.println("Information for "+expName);
            System.out.println("Title: "+expName);
            System.out.println("Description: "+e.description);
            System.out.println("Response server name: "+e.resServer);
            System.out.println("Response server port: "+e.resPort);
            System.out.println("Number of URLs: "+e.urlList.size());
            
            System.exit(1);
        } catch(NoContentException ex)
        {
            System.err.println("Server returned no meaningful information. Exiting...");
            System.exit(1);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        l=new Logger();
        parseArgs(args);
        
        //Now proceed to execute:
        if(saveConfig)
        {
            saveConfig();
        }
        
        else //Saveconfig implies that the user set configuration options
        {
            //Verify:
            if(!configSet) //If the configuration isn't set.
            {
                System.out.println("Config not set through command line. Loading ./config...");
                readConfig();
            }
        }
        
        //Now basically go down the list of things to do:
        if(connectionType==0)
        {
            System.out.println("Nothing else to do. Exiting...");
            System.exit(1);
        }
        
        //Get the clients ready:
        //cacheClient=new CacheRequestProtocolClient(cacheModuleName, cacheModulePort, l, dir);
        snippetClient=new SnippetProtocolClient(l);
        //The response protocol client should be handled by its respective method.
        
        switch(connectionType)
        {
            case CONNECTION_TYPE_NEXT:
                connectNext();
                break;
            case CONNECTION_TYPE_ALL:
                connectAll();
                break;
            case CONNECTION_TYPE_REGISTER:
                connectRegister();
                break;
            case CONNECTION_TYPE_LIST:
                connectList();
                break;
            case CONNECTION_TYPE_GETINFO:
                connectGetInfo();
                break;
        }
    }
    
}
