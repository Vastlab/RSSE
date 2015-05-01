/*
 * This class ensures that no two connections are going to overwrite each other's data. 
 * ie. this ensures further data coherency. 
 * It will be integrated into the server portion of the connection protocol, as that is the section most likely to be handling
 * concurrent requests. 
 *
 */

package cachemodule;

/**
 *
 * @author mgohde
 */
public class ConcurrentRequestManager
{
    private Database d;
    
    public ConcurrentRequestManager()
    {
        d=new Database();
    }
    
    public synchronized void addFileFetch(String urlBeingFetched)
    {
        d.add(new CacheNode(null, urlBeingFetched));
    }
    
    public synchronized boolean fileBeingFetched(String url)
    {
        return d.find(url)!=null;
    }
    
    public synchronized boolean finishFileFetch(String url)
    {
        return d.remove(url);
    }
}
