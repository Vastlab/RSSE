/*
 * This class contains all of the bits necessary to listen on a certain port for caching requests.
 * The server implemented in this class will be used to communicate primarily with the EC portion of the RSSE stack.
 */

package cachemodule;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author mgohde
 */
public class CacheRequestServer
{
    private ConcurrentRequestManager m;
    private int portNum;
    private String dnldDir;
    private Thread listenerThread;
    private Database db;
    
    public CacheRequestServer(CMConfig cfg, Database newDb)
    {
        portNum=Integer.parseInt(cfg.getSetting(CMConfig.SETTING_SERVER_PORT));
        dnldDir=cfg.getSetting(CMConfig.SETTING_STORAGE_DIR);
        db=newDb;
        m=new ConcurrentRequestManager();
    }
    
    public synchronized void startServer()
    {
        listenerThread=new Thread(new SocketListenerRunnable());
    }
    
    public synchronized void stopServer()
    {
        
    }
    
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
                        Thread.sleep(10);
                    } catch(InterruptedException e)
                    {
                        
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
        }
    }
}
