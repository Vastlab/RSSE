/*
 * Implements the database interface with an ArrayList. This should only be
 * used for validation and debugging, as it should get fairly slow
 * on larger datasets.
 */
package cachemodule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ArrayListDatabase implements Database
{
    private Logger l;
    private ArrayList<String> nameList;
    private ArrayList<CacheNode> nodeList;
    
    public ArrayListDatabase(Logger l)
    {
        nameList=new ArrayList<String>();
        nodeList=new ArrayList<CacheNode>();
        this.l=l;
    }
    
    @Override
    public boolean loadDatabase(File snapshot)
    {
        try
        {
            Scanner s=new Scanner(snapshot);
            Scanner lineScanner;
            String url, path;
            
            //Exit with error if we don't have the correct version code.
            if(!(s.next().equals("VER")&&s.next().equals(RSSEConstants.RSSE_VERSION_STRING)))
            {
                l.logErr("Database", "Version number of database file doesn't match current RSSE version.");
                return false;
            }
            
            //By now, any file read errors should have been thrown:
            nameList.clear();
            nodeList.clear();
            
            while(s.hasNextLine())
            {
                lineScanner=new Scanner(s.nextLine());
                
                //This should clear up improperly formatted files:
                if(lineScanner.hasNext())
                {
                    url=lineScanner.next();
                    path=lineScanner.next();

                    nameList.add(url);
                    nodeList.add(new CacheNode(new File(path), url));
                }
            }
            
            return true;
        } catch(FileNotFoundException e)
        {
            l.logErr("Database", "Couldn't read snapshot file: "+snapshot);
        }
        
        return false;
    }
    
    @Override
    public boolean saveDatabase(File snapshot)
    {
        try
        {
            PrintWriter pw=new PrintWriter(snapshot);
            
            pw.println("VER "+RSSEConstants.RSSE_VERSION_CODE);
            
            for(CacheNode n:nodeList)
            {
                pw.println(n.getOrigin()+" "+n.getFile().getAbsolutePath());
            }
            
            pw.flush();
            pw.close();
            
            return true;
        } catch(FileNotFoundException e)
        {
            l.logErr("Database", "Can't write snapshot file: "+snapshot);
            return false;
        }
    }
    
    @Override
    public synchronized boolean remove(String uri)
    {
        for(int i=0;i<nameList.size();i++)
        {
            if(nameList.get(i).equals(uri))
            {
                nameList.remove(i);
                nodeList.remove(i);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public synchronized CacheNode find(String uri)
    {
        for(CacheNode n:nodeList)
        {
            if(n.getOrigin().equals(uri))
            {
                return n;
            }
        }
        
        return null;
    }
    
    @Override
    public synchronized boolean add(CacheNode newNode)
    {
        //First check if the object exists:
        for(CacheNode c:nodeList)
        {
            if(c.getOrigin().equals(newNode.getOrigin()))
            {
                c.setFile(newNode.getFile());
                return true;
            }
        }
        
        //If nothing is found, add a new node:
        nameList.add(newNode.getOrigin());
        nodeList.add(newNode);
        
        return true;
    }
    
    public ArrayList<String> generateReport()
    {
        return nameList;
    }
}
