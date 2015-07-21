/*
 * This represents one of the experiments stored within an experiment file.
 * It is a very simple container class with a fairly lackluster constructor. 
 */

package experimentalserverservice;

import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class Experiment
{
    public String name;
    public String description;
    public boolean report;
    public String resServer;
    public int resPort;
    public ArrayList<DataElement> urlList;
    public int idCounter;
    
    public Experiment()
    {
        name=null;
        description=null;
        report=false;
        resServer="localhost";
        resPort=9001;
        urlList=new ArrayList<DataElement>();
        idCounter=0;
    }
    
    public Experiment(Experiment e)
    {
        name=e.name;
        description=e.description;
        report=e.report;
        resServer=e.resServer;
        resPort=e.resPort;
        urlList=new ArrayList<DataElement>();
        idCounter=e.idCounter;
        
        for(DataElement elem:e.urlList)
        {
            urlList.add(elem);
        }
    }
    
    @Override
    public String toString()
    {
        String s="Title: "+name;
        s+="\nDescription: "+description+"\n";
        
        return s;
    }
    
    /**
     * Used to generate experiment digest files.
     * @return 
     */
    public String saveToString()
    {
        String outStr;
        
        outStr="title\t"+name;
        outStr+="\ndescription\t"+description;
        outStr+="\nidcounter\t"+idCounter;
        
        for(DataElement e:urlList)
        {
            outStr+=e.toString()+"\n";
        }
        
        return outStr;
    }
    
    /**
     * Used to load from experiment digest files.
     * @param s 
     */
    public void loadFromString(String s)
    {
        Scanner strScanner=new Scanner(s);
        Scanner lineScanner;
        String temp;
        
        while(strScanner.hasNextLine())
        {
            
        }
    }
}
