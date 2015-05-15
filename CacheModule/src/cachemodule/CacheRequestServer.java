/*
 * This class contains all of the bits necessary to listen on a certain port for caching requests.
 * The server implemented in this class will be used to communicate primarily with the EC portion of the RSSE stack.
 */

package cachemodule;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public class CacheRequestServer
{
    public static final String CACHE_REQUEST_SERVER_TAG="CacheRequestServer";
    private ConcurrentRequestManager m;
    private int portNum;
    private String dnldDir;
    private Thread listenerThread;
    private Thread snapshotThread;
    private Database db;
    private Logger l;
    private ArrayList<Thread> connections;
    
    public CacheRequestServer(CMConfig cfg, Database newDb, Logger newLogger)
    {
        portNum=Integer.parseInt(cfg.getSetting(CMConfig.SETTING_SERVER_PORT));
        dnldDir=cfg.getSetting(CMConfig.SETTING_STORAGE_DIR);
        db=newDb;
        m=new ConcurrentRequestManager();
        l=newLogger;
        connections=new ArrayList<Thread>();
        snapshotThread=new Thread(new DatabaseSnapshotRunnable(Integer.parseInt(cfg.getSetting(CMConfig.SETTING_SAVE_FREQUENCY)), new File(dnldDir+"/snapshot.file")));
        snapshotThread.start();
    }
    
    public boolean isRunning()
    {
        return listenerThread!=null;
    }
    
    public synchronized void startServer()
    {
        l.logMsg(CACHE_REQUEST_SERVER_TAG, "Starting server thread on port "+portNum);
        if(listenerThread!=null)
        {
            stopServer();
        }
        
        listenerThread=new Thread(new SocketListenerRunnable());
        listenerThread.start();
    }
    
    public synchronized void stopServer()
    {
        l.logMsg(CACHE_REQUEST_SERVER_TAG, "Stopping server thread...");
        listenerThread.interrupt();
        
        try
        {
            srvrSock.close();
        } catch(IOException e)
        {
            //Ignore the exception as it shouldn't matter.
        }
        
        listenerThread=null;
        System.out.println("Listener thread should have stopped.");
        
        if(snapshotThread!=null)
        {
            snapshotThread.interrupt();
        }
    }
    
    public synchronized void changeSnapshotThread(File newFile, int newInterval)
    {
        snapshotThread.interrupt();
        //This is to ensure that the thread caught the signal and stopped.
        try
        {
            Thread.sleep(100);
        } catch(InterruptedException e)
        {
            //Do nothing.
        }
        
        snapshotThread=new Thread(new DatabaseSnapshotRunnable(newInterval, newFile));
        snapshotThread.start();
    }
    
    private ServerSocket srvrSock;
    
    /*
     * This subclass listens for connections on its ServerSocket and splits off connection threads to 
     * handle the details.
     */
    private class SocketListenerRunnable implements Runnable
    {
        ServerSocket s;
        
        public SocketListenerRunnable()
        {
            try
            {
                s=new ServerSocket(portNum);
                srvrSock=s;
            } catch(IOException e)
            {
                s=null;
            }
            
        }
        
        public void run()
        {
            if(s!=null)
            {
                while(true)
                {
                    try
                    {
                        System.out.println("Accepting connection...");
                        Socket in=s.accept();
                        System.out.println("Connection accepted");
                        new Thread(new SocketConnectionRunnable(s, in)).start();
                    } catch(IOException e)
                    {
                        l.logMsg(CACHE_REQUEST_SERVER_TAG, "Socket closed. Stopping listener thread...");
                        return;
                    }
                    try
                    {
                        Thread.sleep(10);
                        l.logMsg(CACHE_REQUEST_SERVER_TAG, "Listener thread interrupted. Stopping...");
                    } catch(InterruptedException e)
                    {
                        //Break the thread:
                        try
                        {
                            s.close();
                        } catch(IOException ex)
                        {
                            //Does it seriously matter if there was an exception here?
                        }
                        
                        return;
                    }
                }
            }
        }
    }
    
    /*
     * This subclass is intended to handle an individual connection to the CM.
     */
    private class SocketConnectionRunnable implements Runnable
    {
        CacheRequestProtocolServer s;
        ServerSocket sock;
        Socket localSock;
        
        public SocketConnectionRunnable(ServerSocket newSock, Socket newLocalSock)
        {
            sock=newSock;
            localSock=newLocalSock;
        }
        
        public void run()
        {
            s=new CacheRequestProtocolServer(db, m);
            try 
            {
                s.handleConnection(localSock, sock);
            } catch(InterruptedException e)
            {
                l.logErr(dnldDir, dnldDir);
            }
            
        }
    }
    
    private class DatabaseSnapshotRunnable implements Runnable
    {
        //Use a tag for this here to distance this thread from the rest of the CacheRequestServer.
        private static final String DBSNAPSHOT_TAG="DatabaseSnapshotService"; 
        private int secPerSleep;
        private File snapshotFile;
        
        public DatabaseSnapshotRunnable(int newSecPerSleep, File newFile)
        {
            secPerSleep=newSecPerSleep*1000; //Specified in seconds, but Thread.sleep uses ms.
        }
        
        public void run()
        {
            while(true)
            {
                try
                {
                    Thread.sleep(secPerSleep);
                    db.saveDatabase(snapshotFile);
                } catch(InterruptedException e)
                {
                    l.logMsg(DBSNAPSHOT_TAG, "Stopping DB shapshot thread...");
                    return;
                }
            }
        }
    }
}
