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
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author mgohde
 */
public class ResponseServer
{
    private static final String RESPSERVER_TAG="ResponseServer";
    private ServerSocket sock;
    private Logger l;
    private int serverSocketPort;
    private Thread listenerThread;
    private String respDir;
    
    public ResponseServer(int portToUse, String newRespDir, Logger newLogger)
    {
        System.out.println(portToUse);
        serverSocketPort=portToUse;
        respDir=newRespDir;
        l=newLogger;
    }
    
    public void startServer()
    {
        l.logMsg(RESPSERVER_TAG, "Starting server thread...");
        if(listenerThread!=null)
        {
            stopServer();
        }
        
        listenerThread=new Thread(new ListenerRunnable());
        listenerThread.start();
    }
    
    public synchronized void stopServer()
    {
        l.logMsg(RESPSERVER_TAG, "Stopping server thread...");
        
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
                
                if(sock!=null)
                {
                    while(true)
                    {
                        try
                        {
                            System.out.println("Accepting connection...");
                            Socket in=sock.accept();
                            System.out.println("Connection accepted");
                            new Thread(new SocketConnectionRunnable(in, respDir)).start();
                        } catch(IOException e)
                        {
                            l.logMsg(RESPSERVER_TAG, "Socket closed. Stopping listener thread...");
                            return;
                        }
                        try
                        {
                            Thread.sleep(10);
                            l.logMsg(RESPSERVER_TAG, "Listener thread interrupted. Stopping...");
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
                }
            } catch(IOException e)
            {
                l.logErr(RESPSERVER_TAG, "IOException when trying to bind port "+serverSocketPort);
            }
        }
    }
    
    private class SocketConnectionRunnable implements Runnable
    {
        private ResponseProtocolServer server;
        private Socket sock;
        
        public SocketConnectionRunnable(Socket s, String respStorageDir)
        {
            server=new ResponseProtocolServer(l, respStorageDir);
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
                
                server.serve(out, in);
                
                out.flush();
                out.close();
                in.close();
            } catch(IOException e)
            {
                l.logErr(RESPSERVER_TAG, "Got IOException when collecting response!");
            }
        }
    }
}
