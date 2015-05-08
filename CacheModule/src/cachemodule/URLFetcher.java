/*
 * This class contains methods and structures that enable it to fetch data from the Internet.
 * While it is technically the express intent for this to fetch data from the internet, it can technically
 * fetch any arbitrary data from anywhere as long as its location can be represented by a URI and is accessible.
 */

package cachemodule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class URLFetcher
{
    public static final String URLFETCHER_TAG="URLFetcher";
    
    private String storagePath;
    private Logger localL;
    
    public URLFetcher(String newStoragePath, Logger l) throws FileNotFoundException
    {
        storagePath=newStoragePath;
        localL=l;
        
        File f=new File(storagePath);
        
        if(f.exists())
        {
            if(!f.canRead()||!f.canWrite())
            {
                //This is a fairly crude way of forcing the rest of the program to respond, but it should work.
                throw new FileNotFoundException();
            }
        }
        
        //Make the storage directory if it doesn't already exist.
        else
        {
            if(!f.mkdir())
            {
                throw new FileNotFoundException();
            }
        }
    }
    
    public long getRemoteFileSize(String url) throws MalformedURLException
    {
        HttpURLConnection conn=null;
        
        try
        {
            URL u=new URL(url);
            
            //Do some vague kludge-y HTTP magic:
            conn=(HttpURLConnection) u.openConnection();
            conn.setRequestMethod("HEAD");
            conn.getInputStream();
            
            return conn.getContentLength();
        } catch(IOException e)
        {
            return -1;
        } finally
        {
            if(conn!=null)
            {
                conn.disconnect();
            }
        }
    }
    
    /**
     * This returns a filename sans path.
     * @param input
     * @return the raw filename.
     * @throws MalformedURLException 
     */
    public static String parsePath(String input) throws MalformedURLException
    {
        URL url=new URL(input);
        String outStr="";
        String urlStr;
        Scanner urlScanner;
        
        urlStr=url.getPath(); //This gets the filename and path.
        urlScanner=new Scanner(urlStr);
        urlScanner.useDelimiter("/"); //This is written for real operating systems.
        
        while(urlScanner.hasNext())
        {
            outStr=urlScanner.next();
        }
        
        System.out.println("Final filename="+outStr);
        return outStr;
    }
    
    public File fetchFromServer(String url) throws MalformedURLException
    {
        System.out.println("Fetching "+url+" from server...");
        File fileOut=null;
        //This throws the malformedurlexception that is thrown here because it 
        //is the responsibility of the calling code to check for such conditions.
        URL outUrl=new URL(url);
        fileOut=new File(storagePath+parsePath(url));//outUrl.getFile());
        HttpURLConnection connection;
        
        try {
            //This doesn't account for proxy server information.
            connection=(HttpURLConnection) outUrl.openConnection();
            System.out.println("Opened connection...");
            //This is so that I can write to the file in a reasonably nice way.
            PrintStream fileOutputStream=new PrintStream(fileOut);
            
            Scanner connectionReader=new Scanner((connection.getInputStream()));
            connectionReader.useDelimiter("\\Z");
            //Do a raw copy. This loop is technically unnecessary.
            while(connectionReader.hasNext())
            {
                fileOutputStream.print(connectionReader.next());
            }
        
        //This catch clause has to go before all of the others... for some reason. I blame... I don't even know what.
        } catch (FileNotFoundException e){
            localL.logErr(URLFETCHER_TAG, "ERROR: Couldn't fetch file!");
        } catch (IOException ex)
        {
            localL.logErr(URLFETCHER_TAG, "IOException trying to fetch file!");
        }
        
        return fileOut;
    }
    
    /**
     * Fetches a file as a binary stream.
     * @param url
     * @return
     * @throws MalformedURLException 
     */
    public File binFetchFromServer(String url) throws MalformedURLException
    {
        System.out.println("Binary fetching "+url+" from server...");
        System.out.println("Storage path="+storagePath);
        File fileOut=null;
        try
        {
            byte[] byteBuffer=new byte[8192]; //Many files are fairly granular in size;
            int cachedSize=-1;
            
            URL outURL=new URL(url);
            URLConnection connection=outURL.openConnection();
            InputStream in=connection.getInputStream();
            
            fileOut=new File(storagePath+"/"+parsePath(url));
            System.out.println(fileOut);
            OutputStream out=(new FileOutputStream(fileOut));
            
            while((cachedSize=in.read(byteBuffer))!=-1)
            {
                out.write(byteBuffer, 0, cachedSize);
            }
            
        } catch (IOException ex)
        {
           localL.logErr(URLFETCHER_TAG, "Couldn't fetch binary from URL: "+url);
        }
        
        return fileOut;
    }
}
