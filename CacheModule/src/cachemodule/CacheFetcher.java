/*
 * This is a sort of fire and forget class that adds some data to the database and then
 * sits idle.
 */

package cachemodule;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mgohde
 */
public class CacheFetcher
{
    public static final String CACHE_FETCHER_TAG="CacheFetcher";
    
    public static synchronized boolean fetch(Database d, String url)
    {
        try
        {
            URLFetcher f=new URLFetcher(CacheModule.cfg.getSetting(CMConfig.SETTING_STORAGE_DIR), CacheModule.l);
            File localFile=f.binFetchFromServer(url);
            
            System.out.println("Fileamajig:"+localFile);
            
            d.add(new CacheNode(localFile, url));
            return true;
        } catch(FileNotFoundException e)
        {
            CacheModule.l.logErr(CACHE_FETCHER_TAG, "Couldn't fetch requested URL: "+url);
            return false;
        } catch(MalformedURLException e)
        {
            CacheModule.l.logErr(CACHE_FETCHER_TAG, "Requested URL malformed: "+url);
            return false;
        } 
    }
}
