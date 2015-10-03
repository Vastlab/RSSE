/*
 * Rather than have one specific, generap-purpose database, I'm just going to implement a simple BST.
 */

package experimentalserverservice2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author mgohde
 * @param <T>
 */
public class BST<T>
{
    private BSTNode<T> root;
    
    public BST()
    {
        root=null;
    }
    
    private void internalInsert(BSTNode<T> n, int index, T val)
    {
        if(index>n.index)
        {
            if(n.right==null)
            {
                n.right=new BSTNode<T>(null, null, val, index);
            }
            
            else
            {
                internalInsert(n.right, index, val);
            }
        }
        
        else if(index<n.index)
        {
            if(n.left==null)
            {
                n.right=new BSTNode<T>(null, null, val, index);
            }
            
            else
            {
                internalInsert(n.right, index, val);
            }
        }
        
        //Update the node:
        else
        {
            n.val=val;
        }
    }
    
    public void insert(int index, T val)
    {
        if(root==null)
        {
            root=new BSTNode<T>(null, null, val, index);
        }
        
        else
        {
            internalInsert(root, index, val);
        }
    }
    
    private T recursiveLookup(BSTNode<T> n, int index)
    {
        if(n==null)
        {
            return null;
        }
        
        if(index>n.index)
        {
            return recursiveLookup(n.right, index);
        }
        
        else if(index<n.index)
        {
            return recursiveLookup(n.left, index);
        }
        
        else
        {
            return n.val;
        }
    }
    
    public T lookup(int index)
    {
        return recursiveLookup(root, index);
    }
    
    private void recursiveDump(PrintWriter pw, BSTNode n)
    {
        if(n==null)
        {
            return;
        }
        
        //Fun fact: NetBeans caught that this would have recursed infinitely without the above if-statment.
        //That's pretty spiffy!
        recursiveDump(pw, n.left);
        pw.println(n.val);
        pw.println(n.index);
        recursiveDump(pw, n.right);
    }
    
    public void dump(File f) throws IOException
    {
        PrintWriter fileWriter;
        
        fileWriter=new PrintWriter(f);
        
        recursiveDump(fileWriter, root);
        fileWriter.flush();
        fileWriter.close();
    }
    
    //It looks like a restore function has to be externally implemented...
    //Maybe I need to make some wrapper classes?
}
