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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class CacheRequestProtocolClient
{
    public static final String CACHE_REQUEST_PROTOCOL_CLIENT_TAG="CacheRequestProtocolClient";
    public static final int FILE_CANT_FIT_ERR=1000;
    public static final int URI_MALFORMED_ERR=1002;
    
    private Socket s;
    private Logger l;
    private String dnldDir;
    
    private String localAddress;
    private int portUsed;
    
    private boolean sendHeader;
    
    /**
     * Constructs a new CacheRequestProtocolClient which can be used to make one connection to a remote server.
     * @param address
     * @param port
     * @param loggerToUse
     * @param newDownloadDir 
     */
    public CacheRequestProtocolClient(String address, int port, Logger loggerToUse, String newDownloadDir)
    {
        try
        {
            localAddress=address;
            portUsed=port;
            
            s=new Socket(address, port);
            l=loggerToUse;
            
            dnldDir=newDownloadDir;
        } catch(IOException e)
        {
            loggerToUse.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Couldn't create new socket for "+address+":"+port);
            e.printStackTrace();
        }
        
        sendHeader=true;
    }
    
    /**
     * Fetches binary data over a stream.
     * @param i
     * @param f 
     */
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
                numRead=i.read(bytes);
            }
        } catch(FileNotFoundException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Couldn't download requested file.");
        } catch(IOException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "IOException when downloading sent file!");
        }
    }
    
    /**
     * This function closes all streams needed by the functions locally.
     * @param i
     * @param o
     * @param pw
     * @param r
     * @param sock
     * @throws IOException 
     */
    private void closeAll(InputStream i, OutputStream o, PrintWriter pw, BufferedReader r, Socket sock) throws IOException
    {
        r.close();
        pw.close();
        i.close();
        o.close();
        sock.close();
    }
    
    /**
     * This function returns whether or not something is stored in the CM's cache.
     * @param url
     * @return boolean representing whether or not the url is cached.
     */
    public boolean checkIfCached(String url)
    {
        boolean returnVal=false;
        
        try
        {
            InputStream i;
            OutputStream o;
            PrintWriter pw;
            BufferedReader r;
            String respCache;
            
            i=s.getInputStream();
            o=s.getOutputStream();
            pw=new PrintWriter(o, true);
            r=new BufferedReader(new InputStreamReader(i));
            
            pw.println("CHECK");
            
            r.readLine(); //The server can only say "OK"
            
            pw.println("URL");
            pw.println(url);
            
            if(r.readLine().equalsIgnoreCase("OK"))
            {
                returnVal=true;
            }
            
            closeAll(i, o, pw, r, s);
        } catch(IOException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Couldn't check if file cached due to IOException.");
        }
        
        return returnVal;
    }
    
    /**
     * This function connects to a caching server and requests that it cache some data.
     * @param url, the url to cache.
     * @throws CacheErrorException containing any errors encountered during the connection
     */
    public void connectCache(String url) throws CacheErrorException
    {
        try
        {
            InputStream i;
            OutputStream o;
            PrintWriter pw;
            String respCache;
            
            i=s.getInputStream();
            o=s.getOutputStream();
            pw=new PrintWriter(o, true);
            BufferedReader r=new BufferedReader(new InputStreamReader(i));
            
            pw.println("CACHE");
            respCache=r.readLine();
            
            if(!respCache.equalsIgnoreCase("OK"))
            {
                Scanner newScan=new Scanner(respCache);
                newScan.next();
                
                closeAll(i, o, pw, r, s);
                
                throw new CacheErrorException(newScan.nextInt());
            }
            
            pw.println("URL");
            pw.println(url);
            
            respCache=r.readLine();
            
            if(respCache==null)
            {
                l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Server unexpectedly closed connection.");
                closeAll(i, o, pw, r, s);
                return;
            }
            
            if(!respCache.equalsIgnoreCase("OK"))
            {
                Scanner newScan=new Scanner(respCache);
                newScan.next();
                
                closeAll(i, o, pw, r, s);
                
                throw new CacheErrorException(newScan.nextInt());
            }
            
           closeAll(i, o, pw, r, s);
            
        } catch(IOException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Couldn't send cache request.");
        }
    }
    
    /**
     * Connects to a server with the explicit intent to do a binary transfer.
     * @return file fetched
     * @param url the url to fetch
     * @throws CacheErrorException 
     */
    public File connectExplicitRemote(String url) throws CacheErrorException
    {
        try
        {
            long fSize;
            String respCache;
            File f, diskTestFile;
            
            InputStream i;
            OutputStream o;
            
            i=s.getInputStream();
            o=s.getOutputStream();
            
            PrintWriter outWriter=new PrintWriter(o, true);
            BufferedReader r=new BufferedReader(new InputStreamReader(i));
            
            outWriter.println("FETCH BINARY");
            
            //Get the remote server's response (it can only be OK for commands given)
            System.out.println(r.readLine());
            
            outWriter.println("URL");
            outWriter.println(url);
            
            //Now check for errors here:
            respCache=r.readLine();
            
            if(!respCache.equalsIgnoreCase("OK"))
            {
                Scanner newScan=new Scanner(respCache);
                newScan.next();
                
                closeAll(i, o, outWriter, r, s);
                
                throw new CacheErrorException(newScan.nextInt());
            }
            
            //Now get the filename:
            r.readLine();
            f=new File(dnldDir, r.readLine());
            
            //Get the file size.
            diskTestFile=new File(dnldDir);
            r.readLine();
            fSize=Long.parseLong(r.readLine());
            
            if(fSize>diskTestFile.getFreeSpace())
            {
                outWriter.println("ERR "+FILE_CANT_FIT_ERR);
                
                closeAll(i, o, outWriter, r, s);
                
                throw new CacheErrorException(FILE_CANT_FIT_ERR);
            }
            
            outWriter.println("OK");
            
            //Now start collecting the data:
            fetchBin(i, f);
            
            closeAll(i, o, outWriter, r, s);
            return f;
            
        } catch(IOException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Couldn't connect to server socket in connectExplicitRemote()!");
        }
        
        return null;
    }
    
    /**
     * Connects to a local CM when it is known that it is local.
     * @param url
     * @return file fetched
     * @throws CacheErrorException 
     */
    public File connectExplicitLocal(String url) throws CacheErrorException
    {
        try
        {
            
            //Yay abstraction!
            InputStream i;
            OutputStream o;
            PrintWriter pw;
            String respCache;
            
            i=s.getInputStream();
            o=s.getOutputStream();
            pw=new PrintWriter(o, true);
            BufferedReader r=new BufferedReader(new InputStreamReader(i));
            
            pw.println("FETCH LOCAL");
            r.readLine();
            
            pw.println("URL");
            pw.println(url);
            
            //Now handle some errors that can be thrown:
            respCache=r.readLine();
            
            if(!respCache.equalsIgnoreCase("OK"))
            {
                Scanner newScan=new Scanner(respCache);
                newScan.next();
                
                closeAll(i, o, pw, r, s);
                
                throw new CacheErrorException(newScan.nextInt());
            }
            
            //Read the URI:
            r.readLine();
            respCache=r.readLine();
            
            return new File(new URI(respCache));
        } catch(IOException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "CCouldn't connect to server socket in connectExplicitLocal()");
        } catch(URISyntaxException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "CM passed bad URI. Throwing exception...");
            throw new CacheErrorException(URI_MALFORMED_ERR);
        }
        
        return null;
    }
    
    /**
     * Connects with an explicit binary transfer. 
     * @param url
     * @return file fetched.
     * @throws CacheErrorException 
     */
    public File connectLocalWithBinaryTransfer(String url) throws CacheErrorException
    {
        return connectExplicitRemote(url);
    }
    
    /**
     * This does a fairly generic connect to a CacheManagementServer.
     * It internally uses a bit of a kludge to work properly, so its behavior will likely be changed in future releases.
     * @param url url to fetch
     * @return file fetched
     * @throws CacheErrorException containing any errors encountered during the connection.
     */
    public File connect(String url) throws CacheErrorException
    {
        try
        {
            InputStream i;
            OutputStream o;
            PrintWriter pw;
            String respCache;
            
            i=s.getInputStream();
            o=s.getOutputStream();
            pw=new PrintWriter(o, true);
            BufferedReader r=new BufferedReader(new InputStreamReader(i));
            
            pw.println("FETCH AUTODETECT");
            
            //Get the server's determination:
            respCache=r.readLine();
            l.logMsg(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Got the following connection type: "+respCache);
            
            //Terminate the connection so that we can reconnect later.
            closeAll(i, o, pw, r, s);
            
            try
            {
                Thread.sleep(100); //Give the server some time to catch up.
            } catch(InterruptedException e)
            {
                
            }
            
            //Since Java is weird, we have to rebuild the socket from scratch:
            s=new Socket(localAddress, portUsed);
            
            if(respCache.equalsIgnoreCase("REMOTE"))
            {
                return connectExplicitRemote(url);
            }
            
            else if(respCache.equalsIgnoreCase("LOCAL"))
            {
                return connectExplicitLocal(url);
            }
        } catch(IOException e)
        {
            l.logErr(CACHE_REQUEST_PROTOCOL_CLIENT_TAG, "Couldnt' connect to server in connect()!");
        }
        return null;
    }
}
