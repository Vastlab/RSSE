/*
 * This class represents an individual file in the RSSE cache. 
 * It will have methods to facilitate the automatic generation of mangled URIs for the interface portion.
 */

package cachemodule;

import java.io.File;

/**
 *
 * @author mgohde
 */
public class CacheNode
{
    private File f;
    private String origin;
    
    public CacheNode(File localFile, String url)
    {
        origin=url;
        f=localFile;
    }
    
    public File getFile()
    {
        return f;
    }
    
    public String getOrigin()
    {
        return origin;
    }
    
    /**
     * Generates a URI for a Cache Request client.
     * @return 
     */
    public String generateURI()
    {
        // TODO Implement!
        return null;
    }
    
    /**
     * Allows relocation of node's file.
     * @param f The new file.
     * @return boolean representing whether the file is valid.
     */
    public boolean setFile(File f)
    {
        if(f!=null)
        {
            if(f.exists()&&f.canRead())
            {
                this.f=f;
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 
     * @param s the new origin URI.
     * @return boolean representing whether the string was valid.
     */
    public boolean setOrigin(String s)
    {
        if(s!=null)
        {
            // TODO add more checking and validation here if the need presents itself.
            origin=s;
            return true;
        }
        
        return false;
    }
}
