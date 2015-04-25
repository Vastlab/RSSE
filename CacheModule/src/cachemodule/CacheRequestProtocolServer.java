/*
 * This represents the server end of cache requests, ie. anything that the Experiment client is expected to do
 * with the cache. There will definitely be some redundancy between this and the CacheManagementProtocolServer, since
 * they do share quite a few similar features. 
 */

package cachemodule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class CacheRequestProtocolServer
{
    public static final String CACHE_REQUEST_PROTOCOL_SERVER_TAG="CacheRequestProtocolServer";
    Database availableDatabase;
    
    public CacheRequestProtocolServer(Database d)
    {
        availableDatabase=d;
    }
    
    /**
     * Handles a caching server request based on the given incoming socket.
     * @param s
     * @return 
     */
    public int handleConnection(Socket s)
    {
        //Set up some abstraction for the socket's interface:
        try
        {
            OutputStream rawOut=s.getOutputStream();
            PrintWriter sockOut=new PrintWriter(s.getOutputStream());
            BufferedReader sockIn=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            //Now get the fetch request type.
            String reqHeader=sockIn.readLine();
            Scanner strScanner=new Scanner(reqHeader);
            
            String reqType=strScanner.next();
            
            //Handle the shortest and easiest type of request:
            if(reqType.equalsIgnoreCase("CHECK"))
            {
                sockOut.println("OK");
                
                //Now get the URL:
                sockIn.readLine();
                String url=sockIn.readLine();
                
                if(availableDatabase.find(url)==null)
                {
                    sockOut.println("ERR 0");
                }
                
                else
                {
                    sockOut.println("OK");
                }
            }
            
            
            //Clean up the various things opened for the connection:
            
        } catch(IOException e)
        {
            CacheModule.l.logErr(null, null);
        }
        
        return -1; //This means that there's no error.
    }
}
