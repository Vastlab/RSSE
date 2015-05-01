/*
 * This represents the server end of cache requests, ie. anything that the Experiment client is expected to do
 * with the cache. There will definitely be some redundancy between this and the CacheManagementProtocolServer, since
 * they do share quite a few similar features. 
 */

package cachemodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class CacheRequestProtocolServer
{
    public static final String CACHE_REQUEST_PROTOCOL_SERVER_TAG="CacheRequestProtocolServer";
    private Database availableDatabase;
    private ConcurrentRequestManager reqMan;
    
    public CacheRequestProtocolServer(Database d, ConcurrentRequestManager r)
    {
        availableDatabase=d;
    }
    
    private static final int CONNECTION_TYPE_LOCAL=1;
    private static final int CONNECTION_TYPE_REMOTE=2;
    private static final int CONNECTION_TYPE_AUTODETECT=3;
    
    private int autoDetect(Socket s, ServerSocket srvr)
    {
        InetAddress remote=s.getInetAddress();
        InetAddress local=srvr.getInetAddress();
        
        if(!local.equals(remote))
        {
            return CONNECTION_TYPE_REMOTE;
        }
        
        else
        {
            return CONNECTION_TYPE_LOCAL;
        }
    }
    
    /**
     * This function dumps binary data on a socket output stream.
     * @param f, the file to load and dump.
     * @param o, the stream to dump the data onto.
     */
    private void dumpBin(File f, OutputStream o)
    {
        try
        {
            int numRead;
            byte bytes[]=new byte[1024];
            FileInputStream s=new FileInputStream(f);
            
            numRead=s.read(bytes);
            
            //Keep going until the input stream can no longer read anything:
            while(numRead!=-1)
            {
                o.write(bytes, 0, numRead);
            }
        } catch(FileNotFoundException e)
        {
            CacheModule.l.logErr(CACHE_REQUEST_PROTOCOL_SERVER_TAG, "Couldn't load file for binary transfer: "+f.getAbsolutePath());
        } catch(IOException e)
        {
            CacheModule.l.logErr(CACHE_REQUEST_PROTOCOL_SERVER_TAG, "Couldn't read from file for binary transfer: "+f.getAbsolutePath());
        }
    }
    
    /**
     * Handles a caching server request based on the given incoming socket.
     * @param s
     * @return 
     */
    public int handleConnection(Socket s, ServerSocket srvr) throws InterruptedException
    {
        //Set up some abstraction for the socket's interface:
        try
        {
            int retV=-1;
            OutputStream rawOut=s.getOutputStream();
            PrintWriter sockOut=new PrintWriter(s.getOutputStream());
            BufferedReader sockIn=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            //Now get the fetch request type.
            String reqHeader=sockIn.readLine();
            Scanner strScanner=new Scanner(reqHeader);
            
            String reqType=strScanner.next();
            
            String url;
            
            //Handle the shortest and easiest type of request:
            if(reqType.equalsIgnoreCase("CHECK"))
            {
                sockOut.println("OK");
                
                //Now get the URL:
                sockIn.readLine();
                url=sockIn.readLine();
                
                if(availableDatabase.find(url)==null)
                {
                    sockOut.println("ERR 0");
                    retV=0;
                }
                
                else
                {
                    sockOut.println("OK");
                }
            }
            
            //Just try to cache the requested data.
            else if(reqType.equalsIgnoreCase("CACHE"))
            {
                sockOut.println("OK");
                
                if(!sockIn.readLine().equalsIgnoreCase("URL"))
                {
                    sockOut.println("ERR 0");
                }
                
                else
                {
                    url=sockIn.readLine();
                    
                    //Check if the file is already being fetched:
                    if(!reqMan.fileBeingFetched(url))
                    {
                        //Register the request:
                        reqMan.addFileFetch(url);
                        
                        if(CacheFetcher.fetch(availableDatabase, url))
                        {
                            sockOut.println("OK");
                        }

                        else
                        {
                            sockOut.println("ERR 3");
                        }
                        
                        reqMan.finishFileFetch(url);
                    } //Otherwise don't even bother.
                }
            }
            
            //Now handle the FETCH request types:
            else if(reqType.equalsIgnoreCase("FETCH"))
            {
                int sockSettings;
                
                if(strScanner.hasNext())
                {
                    reqType=strScanner.next();
                    
                    if(reqType.equalsIgnoreCase("BINARY"))
                    {
                        sockSettings=CONNECTION_TYPE_REMOTE;
                    }
                    
                    else if(reqType.equalsIgnoreCase("LOCAL"))
                    {
                        sockSettings=CONNECTION_TYPE_LOCAL;
                    }
                    
                    //Yay for kludges!
                    else if(reqType.equalsIgnoreCase("AUTODETECT"))
                    {
                        sockSettings=CONNECTION_TYPE_AUTODETECT;
                    }
                    
                    else
                    {
                        sockOut.println("ERR 0");
                        
                        //Do some emergency cleanup:
                        sockOut.flush();
                        sockOut.close();
                        sockIn.close();
                        rawOut.close();
                        s.close();
                        CacheModule.l.logErr(CACHE_REQUEST_PROTOCOL_SERVER_TAG, "Error in request!");
                        
                        return 0;
                    }
                }
                
                //Autodetect settings from the socket:
                else
                {
                    sockSettings=autoDetect(s, srvr);
                }
                
                //Now do the actual execution, again easiest first:
                if(sockSettings==CONNECTION_TYPE_LOCAL)
                {
                    sockOut.println("OK");
                    
                    if(!sockIn.readLine().equalsIgnoreCase("URL"))
                    {
                        sockOut.println("ERR 0");
                        retV=0;
                    }
                    
                    else
                    {
                        url=sockIn.readLine();
                        
                        CacheNode n=availableDatabase.find(url);
                        
                        if(n!=null)
                        {
                            sockOut.println("OK");
                            sockOut.println("URI");
                            sockOut.println(n.generateURI());
                        }
                        
                        else
                        {
                            if(reqMan.fileBeingFetched(url))
                            {
                                while(reqMan.fileBeingFetched(url))
                                {
                                    Thread.sleep(100);
                                }
                            }
                            
                            else
                            {
                                reqMan.addFileFetch(reqHeader);
                                
                                if(!CacheFetcher.fetch(availableDatabase, url))
                                {
                                    sockOut.println("ERR 3");
                                    retV=3;
                                }
                                
                                reqMan.finishFileFetch(url);
                            }
                            
                            if(n==null)
                            {
                                sockOut.println("ERR 3");

                                retV=3;
                            }

                            else
                            {
                                sockOut.println("OK");
                                sockOut.println("URI");
                                sockOut.println(n.generateURI());
                            }
                        }
                    }
                }
                
                //Do the harder part:
                else if(sockSettings==CONNECTION_TYPE_REMOTE)
                {
                    sockOut.println("OK");
                    
                    if(!sockIn.readLine().equalsIgnoreCase("URL"))
                    {
                        sockOut.println("ERR 0");
                        retV=0;
                    }
                    
                    else
                    {
                        url=sockIn.readLine();
                        
                        CacheNode n=availableDatabase.find(url);
                        
                        if(n==null)
                        {
                            if(reqMan.fileBeingFetched(url))
                            {
                                while(reqMan.fileBeingFetched(url))
                                {
                                    Thread.sleep(100);
                                }
                            }
                            
                            else
                            {
                                reqMan.addFileFetch(reqHeader);
                                
                                if(!CacheFetcher.fetch(availableDatabase, url))
                                {
                                    sockOut.println("ERR 3");
                                    retV=3;
                                }
                                
                                reqMan.finishFileFetch(url);
                            }
                        }
                        
                        //Catch the above error condition and begin the binary transfer.
                        if(retV!=3)
                        {
                            sockOut.println("OK");
                            
                            sockOut.println("FILENAME");
                            sockOut.println(n.getFile().getName());
                            sockOut.println("FILESIZE");
                            sockOut.println(n.getFile().length());
                            
                            //Get whether the client can actually fit the file:
                            reqHeader=sockIn.readLine();
                            
                            strScanner=new Scanner(reqHeader);
                            
                            if(strScanner.next().equalsIgnoreCase("OK"))
                            {
                                dumpBin(n.getFile(), rawOut);
                            }
                            
                            else
                            {
                                retV=strScanner.nextInt();
                            }
                        }
                    }
                }
                
                //Add compatibility for a new type of connection
                else if(sockSettings==CONNECTION_TYPE_AUTODETECT)
                {
                    int connectionType=autoDetect(s, srvr);
                    
                    if(connectionType==CONNECTION_TYPE_REMOTE)
                    {
                        sockOut.println("REMOTE");
                    }
                    
                    else
                    {
                        sockOut.println("LOCAL");
                    }
                }
            }
            
            //Clean up the various things opened for the connection:
            sockOut.flush();
            sockOut.close();
            sockIn.close();
            rawOut.close();
            s.close();
            
            return retV;
            
        } catch(IOException e)
        {
            CacheModule.l.logErr(null, null);
        }
        
        return -1; //This means that there's no error.
    }
}
