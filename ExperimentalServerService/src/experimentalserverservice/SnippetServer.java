/*
 * Serves XML nuggets to clients.
 */

package experimentalserverservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public class SnippetServer
{
    private static final String SNIPPET_TAG="SnippetServer";
    private ServerSocket sock;
    private Logger l;
    private int serverSocketPort;
    private Thread listenerThread;
    private ClientDB db;
    private ArrayList<Experiment> experimentList;
    
    public SnippetServer(int portToUse, ClientDB newDb, ArrayList<Experiment> newExpList, Logger newLogger)
    {
        serverSocketPort=portToUse;
        System.out.println(serverSocketPort);
        db=newDb;
        experimentList=newExpList;
        l=newLogger;
    }
    
    public void changeExperimentList(ArrayList<Experiment> newExpList)
    {
        experimentList=newExpList;
    }
    
    public void startServer()
    {
        l.logMsg(SNIPPET_TAG, "Starting server thread...");
        if(listenerThread!=null)
        {
            stopServer();
        }
        
        listenerThread=new Thread(new ListenerRunnable());
        listenerThread.start();
    }
    
    public synchronized void stopServer()
    {
        l.logMsg(SNIPPET_TAG, "Stopping server thread...");
        
        if(listenerThread==null)
        {
            return;
        }
        
        listenerThread.interrupt();
        
        try
        {
            sock.close();
        } catch(IOException e)
        {
            //Ignore the exception as it shouldn't matter.
        }
        
        listenerThread=null;
        System.out.println("Listener thread should have stopped.");
        
        if(listenerThread!=null)
        {
            listenerThread.interrupt();
        }
    }
    
    private class ListenerRunnable implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                sock=new ServerSocket(serverSocketPort);
                while(true)
                {
                    try
                    {
                        System.out.println("Accepting snippet connection...");
                        Socket in=sock.accept();
                        System.out.println("Connection accepted");
                        new Thread(new SocketConnectionRunnable(in)).start();
                    } catch(IOException e)
                    {
                        l.logMsg(SNIPPET_TAG, "Socket closed. Stopping listener thread...");
                        return;
                    }
                    try
                    {
                        Thread.sleep(10);
                        l.logMsg(SNIPPET_TAG, "Listener thread interrupted. Stopping...");
                    } catch(InterruptedException e)
                    {
                        //Break the thread:
                        try
                        {
                            sock.close();
                        } catch(IOException ex)
                        {
                            //Does it seriously matter if there was an exception here?
                        }

                        return;
                    }
                }
            } catch(IOException e)
            {
                l.logErr(SNIPPET_TAG, "IOException when trying to bind port "+serverSocketPort);
            }
        }
    }
    
    private class SocketConnectionRunnable implements Runnable
    {
        private SnippetProtocolServer server;
        private Socket sock;
        
        public SocketConnectionRunnable(Socket s)
        {
            //server=new SnippetProtocolServer(l, respStorageDir);
            sock=s;
        }
        
        @Override
        public void run()
        {
            PrintWriter out;
            BufferedReader in;
            
            try
            {
                out=new PrintWriter(sock.getOutputStream(), true);
                in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
                
                server=new SnippetProtocolServer(in, out, db, experimentList, null); //I never actually use the file manager list.
                
                server.serve();
                
                out.flush();
                out.close();
                in.close();
            } catch(IOException e)
            {
                l.logErr(SNIPPET_TAG, "Got IOException when collecting response!");
            }
        }
    }
}
