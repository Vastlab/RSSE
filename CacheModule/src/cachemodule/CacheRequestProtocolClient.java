/*
 * This is the client side of the cache request protocol. As such, it will only be used by the CM for debugging purposes and
 * is designed to be somewhat portable to other programs with minimal overhead.
 */

package cachemodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author mgohde
 */
public class CacheRequestProtocolClient
{
    public static final String CACHE_REQUEST_PROTOCOL_CLIENT_TAG="CacheRequestProtocolClient";
    private Socket s;
    private Logger l;
    private String dnldDir;
    
    public CacheRequestProtocolClient(InetAddress address, int port, Logger loggerToUse, String newDownloadDir)
    {
        try
        {
            s=new Socket(address, port);
            l=loggerToUse;
            
            dnldDir=newDownloadDir;
        } catch(IOException e)
        {
            loggerToUse.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Couldn't create new socket for "+address+":"+port);
        }
    }
    
    private void fetchBin(InputStream i, File f)
    {
        try
        {
            int numRead;
            byte bytes[]=new byte[1024];
            
            FileOutputStream out=new FileOutputStream(f);
            
            numRead=i.read(bytes);
            
            while(numRead!=-1)
            {
                out.write(bytes, 0, numRead);
            }
        } catch(FileNotFoundException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Couldn't download requested file.");
        } catch(IOException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "IOException when downloading sent file!");
        }
    }
    
    public void connectCache()
    {
        
    }
    
    public File connectExplicitRemote() throws CacheErrorException
    {
        try
        {
            String respCache;
            File f;
            s.connect(null);
            
            InputStream i;
            OutputStream o;
            
            i=s.getInputStream();
            o=s.getOutputStream();
            
            PrintWriter outWriter=new PrintWriter(o, true);
            BufferedReader r=new BufferedReader(new InputStreamReader(i));
            
            outWriter.println("FETCH BINARY");
            
            //Get the remote server's response.
            respCache=r.readLine();
            
            if(!respCache.equalsIgnoreCase("OK"))
            {
                
            }
        } catch(IOException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Couldn't connect to server socket in connectExplicitRemote()!");
        }
        
        return null;
    }
    
    public File connectExplicitLocal()
    {
        return null;
    }
    
    public File connectLocalWithBinaryTransfer()
    {
        return connectExplicitRemote();
    }
    
    /**
     * This does a fairly generic connect to a CacheManagementServer.
     * @return 
     */
    public File connect()
    {
        return null;
    }
}
