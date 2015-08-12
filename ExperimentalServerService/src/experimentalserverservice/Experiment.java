/*
 * This represents one of the experiments stored within an experiment file.
 * It is a very simple container class with a fairly lackluster constructor. 
 */

package experimentalserverservice;

import java.io.File;
import java.io.FileNotFoundException;
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
    
    /**
     * This method generates id numbers for all dataelements in the set.
     */
    public void id()
    {
        idCounter=0;
        
        for(DataElement e:urlList)
        {
            e.setId(idCounter);
            idCounter++;
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
        outStr+="\ndescription\n"+description;
        outStr+="\nidcounter\t"+idCounter;
        
        for(DataElement e:urlList)
        {
            outStr+=e.toString()+"\n";
        }
        
        return outStr;
    }
    
    /**
     * Used to load from experiment digest files.
     * @param f the digest file to read.
     * @return the experiment loaded so that 
     */
    public Experiment loadFromDigestFile(File f) throws FileNotFoundException
    {
        Scanner fileScanner=new Scanner(f);
        Scanner lineScanner;
        String tempLine, temp;
        
        while(fileScanner.hasNextLine())
        {
            tempLine=fileScanner.nextLine();
            lineScanner=new Scanner(tempLine);
            
            temp=lineScanner.next();
            
            if(temp.equals("title"))
            {
                name=lineScanner.next();
            }
            
            else if(temp.equals("description"))
            {
                description=fileScanner.nextLine();
            }
            
            else if(temp.equals("idcounter"))
            {
                idCounter=Integer.parseInt(lineScanner.next());
            }
            
            else
            {
                urlList.add(new DataElement(tempLine));
            }
        }
        
        return this;
    }
}
