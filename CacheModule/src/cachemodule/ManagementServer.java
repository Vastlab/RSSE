/*
 * This is the server end of the management protocol, which is more or less specific to this implementation
 * of RSSE. 
 * This will control several of the internal aspects of the software, though its functions will be made
 * public so that users can start the Cache Module without interactively running its individual components.
 */

package cachemodule;

import java.io.BufferedReader;
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
    
    public ManagementServer(int port, boolean keepConfig)
    {
        db=new Database();
        l=new Logger();
        
        CacheModule.l=l;
        CacheModule.database=db;
        
        if(!keepConfig)
        {
            CacheModule.cfg=new CMConfig();
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
            
            System.out.println("Query: "+q);
            
            //Now try to handle the commands given:
            if(q.command.equalsIgnoreCase("cache"))
            {
                //Try to cache the data:
                CacheRequestProtocolClient c=new CacheRequestProtocolClient("localhost", Integer.parseInt(CacheModule.cfg.getSetting(CMConfig.SETTING_SERVER_PORT)), l, ".");
                
                returnQuery.command="PRINT";
                returnQuery.args.add(new String("Cached "+q.args.size()+" files."));
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
                    CacheModule.cacheSrvr=new CacheRequestServer(CacheModule.cfg, db, l);
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
                
            }
            
            //Now print out the query and exit:
            PrintWriter pw=new PrintWriter(s.getOutputStream(), true);
            pw.println(returnQuery.toString());
            
            pw.close();
            r.close();
            s.close();
        }
        
        public void run()
        {
            while(true)
            {
                try
                {
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
