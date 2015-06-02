/*
 * This class ensures that no two connections are going to overwrite each other's data. 
 * ie. this ensures further data coherency. 
 * It will be integrated into the server portion of the connection protocol, as that is the section most likely to be handling
 * concurrent requests. 
 *
 */

package cachemodule;

import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public class ConcurrentRequestManager
{
    //private Database d; //NOTE: This database is going to be unused for this purpose until removal is sorted out.
    private ArrayList<String> newD;
    
    public ConcurrentRequestManager()
    {
        //d=new Database();
        newD=new ArrayList<String>();
    }
    
    public synchronized void addFileFetch(String urlBeingFetched)
    {
        //d.add(new CacheNode(null, urlBeingFetched));
        newD.add(urlBeingFetched);
    }
    
    public synchronized boolean fileBeingFetched(String url)
    {
        int i;
        
        for(i=0;i<newD.size();i++)
        {
            if(newD.get(i).equals(url))
            {
                return true;
            }
        }
        
        return false;
        
        //return d.find(url)!=null;
    }
    
    public synchronized boolean finishFileFetch(String url)
    {
        int i;
        
        for(i=0;i<newD.size();i++)
        {
            if(newD.get(i).equals(url))
            {
                newD.remove(i);
                return true;
            }
        }
        
        return false;
        //return d.remove(url);
    }
}
