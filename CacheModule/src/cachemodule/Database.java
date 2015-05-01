/*
 * This class contains the methods and data required to keep track of all of the data
 * currently residing in the cache.
 */

package cachemodule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class Database
{
    public static final String DB_TAG="Database";
    private CacheNodeWrapper dbRoot;
    
    public Database()
    {
        dbRoot=null;
    }
    
    /**
     * This function loads a database snapshot from a file. 
     * Note: This function uses Java's abstract File IO and will be depreciated for faster implementations in later releases.
     * @param dbSnapshot
     * @return boolean representing success
     */
    public boolean loadDatabase(File dbSnapshot)
    {
        int ver;
        /*
         * The file format used for the database snapshots in this version is as follows:
         *
         * VER (version number)      -- To allow revisions to the file format.
         * Origin FileDest           -- Each entry in the database has fundamentally just two data items.
         */
        
        try
        {
            Scanner s=new Scanner(dbSnapshot);
            Scanner strScanner;
            String origin;
            File dest;
            
            //Get the version information
            s.next(); //Read the "VER"
            ver=s.nextInt(999999999); //I doubt there will be this many versions of RSSE.
            
            if(ver>RSSEConstants.RSSE_VERSION_CODE)
            {
                return false;
            }
            
            while(s.hasNextLine())
            {
                strScanner=new Scanner(s.nextLine());
                origin=strScanner.next();
                dest=new File(strScanner.next());
                
                add(new CacheNode(dest, origin));
            }
        } catch(FileNotFoundException e)
        {
            CacheModule.l.logErr(DB_TAG, "Couldn't load database from "+dbSnapshot.getAbsolutePath());
        }
        return false;
    }
    
    private synchronized void rcsvTraverse(PrintWriter pw, CacheNodeWrapper w)
    {
        if(w==null)
        {
            return;
        }
        
        rcsvTraverse(pw, w.left);
        
        pw.println(w.data.getOrigin()+" "+w.data.getFile().getAbsolutePath());
        
        rcsvTraverse(pw, w.right);
    }
    
    public synchronized boolean saveDatabase(File dbSnapshot)
    {
        try
        {
            PrintWriter pw=new PrintWriter(dbSnapshot);
            
            pw.println("VER "+RSSEConstants.RSSE_VERSION_CODE);
            
            //Do some horrible recursion:
            rcsvTraverse(pw, dbRoot);
        } catch(IOException e)
        {
            CacheModule.l.logErr(DB_TAG, "Couldn't save data to "+dbSnapshot.getAbsolutePath());
        }
        return false;
    }
    
    /**
     * Finds a node's parent for the remove method.
     * @param uri
     * @param n
     * @return 
     */
    private CacheNodeWrapper rcsvParentFind(CacheNodeWrapper findVal, CacheNodeWrapper n, String uri)
    {
        int cmpVal;
        
        if(n==null)
        {
            return null;
        }
        
        cmpVal=n.data.getOrigin().compareTo(uri);
        
        if(n.left==findVal||n.right==findVal)
        {
            return n;
        }
        
        if(cmpVal>0)
        {
            return rcsvParentFind(findVal, n.right, uri);
        }
        
        else if(cmpVal<0)
        {
            return rcsvParentFind(findVal, n.left, uri);
        }
        
        else
        {
            //We've found the node but not its parent... interesting.
            return null;
        }
    }
    
    private CacheNodeWrapper rcsvLowLevelFind(String uri, CacheNodeWrapper n)
    {
        int cmpVal;
        
        if(n==null)
        {
            return null;
        }
        
        cmpVal=n.data.getOrigin().compareTo(uri);
        
        if(cmpVal>0)
        {
            return rcsvLowLevelFind(uri, n.right);
        }
        
        else if(cmpVal<0)
        {
            return rcsvLowLevelFind(uri, n.left);
        }
        
        else
        {
            return n;
        }
    }
    
    private void rcsvTraverseInsert(CacheNodeWrapper w)
    {
        if(w!=null)
        {
            rcsvInternalAdd(w.data, dbRoot);

            rcsvTraverseInsert(w.left);
            rcsvTraverseInsert(w.right);
        }
    }
    
    /**
     * Inserts a tree into the database.
     * @param tree 
     */
    private void insertTree(CacheNodeWrapper tree)
    {
        if(tree!=null)
        {
            rcsvTraverseInsert(tree);
        }
    }
    
    private void rcsvDeleteNode(CacheNodeWrapper w)
    {
        if(w!=null)
        {
            rcsvDeleteNode(w.left);
            rcsvDeleteNode(w.right);
            w.left=null;
            w.right=null;
        }
    }
    
    /**
     * Ensures that all references inside the tree are null so that the garbage collector can take care of it.
     * (New garbage collectors should probably be able to handle this, but just in case...)
     */
    private void deleteTree(CacheNodeWrapper tree)
    {
        if(tree!=null)
        {
            rcsvDeleteNode(tree);
        }
    }
    
    public synchronized boolean remove(String uri)
    {
        boolean deletedElementFound=false;
        
        //See if we can find the parent of the node with this URI:
        CacheNodeWrapper n=rcsvLowLevelFind(uri, dbRoot);
        CacheNodeWrapper parent, leftTree, rightTree;
        
        //Now find that node's parent:
        if(n!=null)
        {
            parent=rcsvParentFind(n, dbRoot, uri);
            
            //This should hopefully always happen...
            if(parent!=null)
            {
                leftTree=n.left;
                rightTree=n.right;
                
                if(parent.left==n)
                {
                    parent.left=null;
                }
                
                else
                {
                    parent.right=null;
                }
                
                //Now recursively insert these trees:
                insertTree(leftTree);
                insertTree(rightTree);
                
                //Methinks this may be unncecessary. I need to see how the JVM garbage collects
                //in real-world situations.
                deleteTree(leftTree);
                deleteTree(rightTree);
                
                deletedElementFound=true;
            }
        }
        
        return deletedElementFound;
    }
    
    //This will be far trickier to implement since it involves a full traversal.
    public synchronized boolean remove(File f)
    {
        return false;
    }
    
    /**
     * Finds a given CacheNode when provided with a source URI.
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
    
    /**
     * Finds a given CacheNode when provided with the node's location on disk.
     * NOT YET IMPLEMENTED!
     * @param f
     * @return 
     */
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
    
    private CacheNode rcsvInternalFindByOrigin(String o, CacheNodeWrapper n)
    {
        int cmpVal;
        
        if(n==null)
        {
            return null;
        }
        
        cmpVal=n.data.getOrigin().compareTo(o);
        
        if(cmpVal>0)
        {
            return rcsvInternalFindByOrigin(o, n.right);
        }
        
        else if(cmpVal<0)
        {
            return rcsvInternalFindByOrigin(o, n.left);
        }
        
        else
        {
            return n.data;
        }
    }
    
    private CacheNode internalFindByOrigin(String origin)
    {
        return rcsvInternalFindByOrigin(origin, dbRoot);
    }
    
    private CacheNode internalFindByFile(File f)
    {
        return null;
    }
}
