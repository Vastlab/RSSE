/*
 * This is the server end of the management protocol, which is more or less specific to this implementation
 * of RSSE. 
 * This will control several of the internal aspects of the software, though its functions will be made
 * public so that users can start the Cache Module without interactively running its individual components.
 */

package cachemodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author mgohde
 */
public class ManagementServer
{
    public static final String MANAGEMENT_SERVER_TAG="ManagementServer";
    private Database db;
    private Logger l;
    private CacheRequestServer s;
    private ServerSocket srvr;
    private Thread serverThread;
    private File snapshotFile;
    
    public ManagementServer(int port, boolean keepConfig)
    {
        l=new DefaultStreamLogger();
        db=new ArrayListDatabase(l);
        
        CacheModule.l=l;
        CacheModule.database=db;
        
        if(!keepConfig)
        {
            CacheModule.cfg=new CMConfig();
        }
        
        snapshotFile=new File(CacheModule.cfg.getSetting(CMConfig.SETTING_STORAGE_DIR)+"db.snapshot");
        
        if(snapshotFile.exists())
        {
            db.loadDatabase(snapshotFile);
        }
        
        try
        {
            srvr=new ServerSocket(port);
        } catch(IOException e)
        {
            l.logErr(MANAGEMENT_SERVER_TAG, "Couldn't listen on specified port.");
        }
    }
    
    private boolean threadRunning=false;
    
    public synchronized void start()
    {
        if(threadRunning)
        {
            try
            {
                stop();
            } catch(IOException e)
            {
                l.logErr(MANAGEMENT_SERVER_TAG, "Couldn't restart thread properly. This may be bad.");
            }
            
        }
        
        serverThread=new Thread(new ManagementServerRunnable());
        serverThread.start();
        
        threadRunning=true;
    }
    
    public synchronized void stop() throws IOException
    {
        if(threadRunning)
        {
            srvr.close();
            threadRunning=false;
        }
    }
    
    public class ManagementServerRunnable implements Runnable
    {
        public ManagementServerRunnable()
        {
            //Do I need to do anything here?
        }
        
        private void handleConnection(Socket s) throws IOException
        {
            BufferedReader r=new BufferedReader(new InputStreamReader(s.getInputStream()));
            System.out.println("Reading query...");
            ManagementQuery q=new ManagementQuery().fromString(r.readLine());
            ManagementQuery returnQuery=new ManagementQuery();
            boolean stopService=false;
            
            System.out.println("Query: "+q);
            
            //Now try to handle the commands given:
            if(q.command.equalsIgnoreCase("cache"))
            {
                //Try to cache the data:
                CacheRequestProtocolClient c=new CacheRequestProtocolClient("localhost", Integer.parseInt(CacheModule.cfg.getSetting(CMConfig.SETTING_SERVER_PORT)), l, ".");
                
                returnQuery.command="PRINT";
                returnQuery.args.add("Cached "+q.args.size()+" files.");
                for(String str:q.args)
                {
                    try
                    {
                        c.connectCache(str);
                    } catch(CacheErrorException e)
                    {
                        l.logErr(MANAGEMENT_SERVER_TAG, e.toString());
                        
                        returnQuery.command="PRINTERR";
                        returnQuery.args.clear();
                        returnQuery.args.add("ERROR! Couldn't cache "+str);
                    }
                }
            }
            
            else if(q.command.equalsIgnoreCase("report"))
            {
                returnQuery.command="PRINT";
                
                returnQuery.args=db.generateReport();
                //That was easy...
            }
            
            else if(q.command.equalsIgnoreCase("startservice"))
            {
                l.logMsg(MANAGEMENT_SERVER_TAG, "Starting CM Service...");
                returnQuery.command="PRINT";
                returnQuery.args.add("OK");
                
                if(CacheModule.cacheSrvr==null)
                {
                    CacheModule.cacheSrvr=new CacheRequestServer(CacheModule.cfg, CacheModule.database, l, snapshotFile);
                }
                
                CacheModule.cacheSrvr.startServer();
            }
            
            else if(q.command.equalsIgnoreCase("stopservice"))
            {
                returnQuery.command="PRINT";
                returnQuery.args.add("OK");
                
                if(CacheModule.cacheSrvr!=null)
                {
                    CacheModule.cacheSrvr.stopServer();
                }
            }
            
            else if(q.command.equalsIgnoreCase("loadcfg"))
            {
                if(CacheModule.cfg==null)
                {
                    CacheModule.cfg=new CMConfig();
                }
                
                returnQuery.command="PRINT";
                
                if(CacheModule.cfg.loadSettings(new File(q.args.get(0))))
                {
                    returnQuery.args.add("Successfully loaded "+q.args.get(0));
                }
                
                else
                {
                    returnQuery.args.add("ERROR: Couldn't load "+q.args.get(0));
                }
                
                //Also restart the caching server and other such things that are dependent on the config:
                snapshotFile=new File(CacheModule.cfg.getSetting(CMConfig.SETTING_STORAGE_DIR)+"db.snapshot");
        
                if(snapshotFile.exists())
                {
                    db.loadDatabase(snapshotFile);
                }
                
                if(CacheModule.cacheSrvr!=null)
                {
                    if(CacheModule.cacheSrvr.isRunning())
                    {
                        CacheModule.cacheSrvr.stopServer();
                        CacheModule.cacheSrvr=new CacheRequestServer(CacheModule.cfg, CacheModule.database, l, snapshotFile);
                    }
                }
            }
            
            else if(q.command.equalsIgnoreCase("savecfg"))
            {
                returnQuery.command="PRINT";
                
                if(CacheModule.cfg!=null)
                {
                    if(CacheModule.cfg.saveSettings(new File(q.args.get(0))))
                    {
                        returnQuery.args.add("Saved configuration file: "+q.args.get(0));
                    }
                    
                    else
                    {
                        returnQuery.args.add("ERROR: Couldn't save configuration file "+q.args.get(0));
                    }
                }
            }
            
            else if(q.command.equals("shutdown"))
            {
                returnQuery.command="PRINT";
                returnQuery.args.add("Stopping Cache Module instance...");
                stopService=true;
            }
            
            else if(q.command.equals("listcommands"))
            {
                returnQuery.command="PRINT";
                returnQuery.args.add("cache -- cache a URL\n");
                returnQuery.args.add("report -- print a list of cached objects.\n");
                returnQuery.args.add("startservice -- start cache server service.\n");
                returnQuery.args.add("stopservice -- stop cache server service.\n");
                returnQuery.args.add("loadcfg -- load configuration from a file.\n");
                returnQuery.args.add("savecfg -- save configuration to a file.\n");
                returnQuery.args.add("shutdown -- stop the cache module completely.\n");
                returnQuery.args.add("listcommands -- print this message.\n");
            }
            
            //Now print out the query and exit:
            PrintWriter pw=new PrintWriter(s.getOutputStream(), true);
            pw.println(returnQuery.toString());
            
            pw.close();
            r.close();
            s.close();
            
            //Gracefully shut everything down:
            if(stopService)
            {
                CacheModule.cacheSrvr.stopServer();
                srvr.close();
                
                //Sleep for thread synchronization:
                try
                {
                    Thread.sleep(1000);
                } catch(InterruptedException e)
                {
                
                }
                
                System.exit(0);
            }
        }
        
        public void run()
        {
            while(true)
            {
                try
                {
                    if(srvr==null)
                    {
                        System.err.println("srvr is indeed null!");
                    }
                    Socket s=srvr.accept();
                    System.out.println("Got connection. Handling...");
                    handleConnection(s);
                } catch(IOException e)
                {
                    return;
                }
            }
        }
    }
}
