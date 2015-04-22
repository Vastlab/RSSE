/*
 * This class contains the methods and data required to keep track of all of the data
 * currently residing in the cache.
 */

package cachemodule;

import java.io.File;

/**
 *
 * @author mgohde
 */
public class Database
{
    private CacheNodeWrapper dbRoot;
    
    public Database()
    {
        dbRoot=null;
    }
    
    public boolean loadDatabase(File dbSnapshot)
    {
        return false;
    }
    
    public boolean saveDatabase(File dbSnapshot)
    {
        return false;
    }
    
    /**
     * 
     * @param uri
     * @return 
     */
    public CacheNode find(String uri)
    {
        /* This function should allow for changes to the way that the database is internally implemented
         * later on.
         */
        return internalFindByOrigin(uri);
    }
    
    public CacheNode find(File f)
    {
        /*
         * See the above function.
         */
        return internalFindByFile(f);
    }
    
    public boolean add(CacheNode newNode)
    {
        return internalAdd(newNode);
    }
    
    /* Begin all of the various binary search tree specific methods: */
    
    private boolean rcsvInternalAdd(CacheNode newNode, CacheNodeWrapper w)
    {
        int cmpVal;
        
        cmpVal=newNode.getOrigin().compareTo(w.data.getOrigin());
        if(cmpVal>0)
        {
            if(w.right==null)
            {
                w.right=new CacheNodeWrapper(null, null, newNode);
                return true;
            }
            
            else
            {
                return rcsvInternalAdd(newNode, w.right);
            }
        }
        
        else if(cmpVal<0)
        {
            if(w.left==null)
            {
                w.left=new CacheNodeWrapper(null, null, newNode);
                return true;
            }
            
            else
            {
                return rcsvInternalAdd(newNode, w.left);
            }  
        }
        
        //Avoid duplicates. 
        else
        {
            return false;
        }
    }
    
    private boolean internalAdd(CacheNode newNode)
    {
        if(dbRoot==null)
        {
            dbRoot=new CacheNodeWrapper(null, null, newNode);
            return true;
        }
        
        else
        {
            //Do some recursion becuase it makes sense for this sort of datastructure.
            return rcsvInternalAdd(newNode, dbRoot);
        }
    }
    
    private CacheNode internalFindByOrigin(String origin)
    {
        return null;
    }
    
    private CacheNode internalFindByFile(File f)
    {
        return null;
    }
}
