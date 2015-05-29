/*
 * In order to keep ClientState objects simple, this implementation requires that all of the URLs in an experiment
 * be in some fixed order.
 *
 * Experiment files are subject to fairly regular change, so such an indexing scheme may be fairly 
 * difficult to keep in a workable state under normal circumstances.
 *
 * This class will ensure that 
 */

package experimentalserverservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class URLFileManager
{
    private static final String FILEMAN_TAG="URLFileManager";
    private ArrayList<DataElement> urlList;
    private String expName;
    private Logger l;
    
    public URLFileManager(Logger newLogger)
    {
        l=newLogger;
        urlList=new ArrayList<DataElement>();
    }
    
    public String getName()
    {
        return expName;
    }
    
    public void getURLs(Experiment e)
    {
        urlList=e.urlList;
        expName=e.name;
    }
    
    public void writeFile(String fileDir)
    {
        File outFile=new File(fileDir+"/"+expName);
        PrintWriter pw;
        int i;
        
        if(!outFile.exists())
        {
            try
            {
                outFile.createNewFile();
            } catch(IOException e)
            {
                l.logErr(FILEMAN_TAG, "Couldn't create URL dump file for: "+expName);
            }
        }
        
        try
        {
            pw=new PrintWriter(outFile);
            
            for(i=0;i<urlList.size();i++)
            {
                pw.println(urlList.get(i).toString());
            }
        } catch(FileNotFoundException e)
        {
            l.logErr(FILEMAN_TAG, "Couldn't dump URL file!");
        }
    }
    
    public void readFile(String fileDir, String experimentName)
    {
        File inFile=new File(fileDir+"/"+experimentName);
        String tempBuffer;
        Scanner fileScanner;
        
        expName=experimentName;
        
        try
        {
            fileScanner=new Scanner(inFile);
            
            while(fileScanner.hasNextLine())
            {
                tempBuffer=fileScanner.nextLine();
                urlList.add(new DataElement(tempBuffer));
            }
        } catch(FileNotFoundException e)
        {
            l.logErr(FILEMAN_TAG, "Couldn't read experiment file: "+(fileDir+"/"+experimentName));
        }
    }
    
    /**
     * Returns an updated index for the client's current state.
     * This function should make it easier for the client to be repositioned if a URL is removed.
     * @param url
     * @return 
     */
    public int getNewPosition(String url)
    {
        //Do a linear search. Nasty.
        int i;
        
        for(i=0;i<urlList.size();i++)
        {
            if(urlList.get(i).getUrl().equals(url))
            {
                return i;
            }
        }
        
        return 0;
    }
    
    /**
     * Updates the URL list given a new experiment.
     * Returns true when a URL was removed.
     * 
     * @param e Represents the experiment to verify against.
     */
    public boolean update(Experiment e)
    {
        boolean urlRemoved, urlUsed;
        ArrayList<Integer> removalList=new ArrayList<Integer>();
        ArrayList<Integer> additionList=new ArrayList<Integer>();
        int i, j, valRemoved, valInserted;
        String cmpUrl;
        
        urlRemoved=false;
        
        //Check for removals:
        for(i=0;i<urlList.size();i++)
        {
            urlUsed=false;
            for(DataElement otherElement:e.urlList)
            {
                if(otherElement.getUrl().equals(urlList.get(i).getUrl()))
                {
                    urlUsed=true;
                }
            }
            
            if(!urlUsed)
            {
                urlRemoved=true;
                removalList.add(i);
            }
        }
        
        //Now find all new elements:
        for(i=0;i<e.urlList.size();i++)
        {
            urlUsed=false;
            
            for(DataElement elem:urlList)
            {
                if(elem.equals(e.urlList.get(i)))
                {
                    urlUsed=true;
                }
            }
            
            if(!urlUsed)
            {
                additionList.add(i);
            }
        }
        
        //Resolve the removals by trying to insert additions in the removed spots:
        j=additionList.size()-1;
        for(i=removalList.size()-1;i>=0;i++) //Go backwards to keep everything in order no matter what.
        {
            valRemoved=removalList.get(i);
            
            if(j<0) //Remove without replacement
            {
                urlList.remove(valRemoved);
            }
            
            else //Replace.
            {
                valInserted=additionList.get(j);
                urlList.set(valRemoved, e.urlList.get(valInserted));
            }
            
            j--;
        }
        
        for(i=0;i<=j;i++)
        {
            urlList.add(e.urlList.get(additionList.get(i)));
        }
        
        return urlRemoved;
    }
}
