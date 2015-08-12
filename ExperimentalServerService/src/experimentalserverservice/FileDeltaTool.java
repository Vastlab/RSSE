/*
 * This class conveniently wraps up all functionality necessary to implement checking and working
 * with changes to experiment files.
 * In the future, it would also be nice for the file to be able to merge 
 */

package experimentalserverservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class FileDeltaTool
{
    private static final String FILEDELTATOOL_TAG="FileDeltaTool";
    private Logger l;
    
    public FileDeltaTool(Logger newL)
    {
        l=newL;
    }
    
    /**
     * This loads and tags an initial experiment file.
     * @param inputFile
     * @return 
     */
    public ArrayList<Experiment> loadInitialFile(File inputFile)
    {
        ArrayList<Experiment> experimentList;
        Parser p;
        
        p=new Parser(l);
        
        experimentList=p.parseExperimentFile(inputFile);
        
        for(Experiment e:experimentList) /* Give each data element an id.*/
        {
            e.idCounter=0;
            
            for(DataElement de:e.urlList)
            {
                de.setId(e.idCounter);
                e.idCounter++;
            }
        }
        
        return experimentList;
    }
    
    public void saveDigests(File masterFile, ArrayList<Experiment> experimentList)
    {
        PrintWriter fileWriter, digestWriter;
        Scanner fileNameScanner;
        String name;
        
        if(masterFile.exists())
        {
            masterFile.delete();
        }
        
        try
        {
            fileWriter=new PrintWriter(masterFile);

            //Generate some filenames:
            for(Experiment e:experimentList)
            {
                fileNameScanner=new Scanner(e.name);
                name="";

                while(fileNameScanner.hasNext()) //Remove any spaces in the name.
                {
                    name+=fileNameScanner.next();
                }
                
                name+=".digest";
                
                fileWriter.println(name);
                
                //Now write the digest file:
                digestWriter=new PrintWriter(new File(name));
                digestWriter.println(e.saveToString());
                digestWriter.flush();
                digestWriter.close();
            }
        
        }catch(FileNotFoundException ex)
        {
            
        }
    }
    
    public ArrayList<Experiment> loadFromDigests(File digestListFile)
    {
        Scanner digestFileScanner;
        Scanner digestScanner;
        ArrayList<Experiment> returnList;
        File digestFile;
        Experiment tempE;
        returnList=new ArrayList<Experiment>();
        
        try
        {
            digestFileScanner=new Scanner(digestListFile);
            
            while(digestFileScanner.hasNextLine())
            {
                digestFile=new File(digestFileScanner.nextLine());
                
                if(digestFile.exists())
                {
                    tempE=new Experiment().loadFromDigestFile(digestFile);
                    returnList.add(tempE);
                }
                
                else
                {
                    l.logErr(FILEDELTATOOL_TAG, "Error: digest file "+digestFile.getAbsolutePath()+" not found!");
                }
            }
        } catch(FileNotFoundException e)
        {
            return null;
        }
        
        return returnList;
    }
    
    /**
     * Merges all changes into the client database.
     * @param existing
     * @param dList
     * @param db
     * @return 
     */
    public Experiment mergeWithClientDb(Experiment existing, ArrayList<Delta> dList, ClientDB db)
    {
        Experiment condensed=new Experiment(existing);
        ArrayList<Mapping> map=new ArrayList<Mapping>();
        ArrayList<Delta> removedList=new ArrayList<Delta>();
        ArrayList<Long> clientIdList;
        int i, remapDelta, lastVal;
        ClientState c;
        
        remapDelta=0;
        
        /* Separate the removals out: */
        for(i=0;i<dList.size();i++)
        {
            /* Mark it for deletion.*/
            if(!dList.get(i).added)
            {
                removedList.add(dList.get(i));
                map.add(new Mapping(i, -1));
                remapDelta++;
            }
            
            /* Calculate the remap: */
            else
            {
                map.add(new Mapping(i, i-remapDelta));
            }
        }
        
        /* Delete all values marked for deletion: */
        for(i=(removedList.size()-1);i>=0;i--)
        {
            condensed.urlList.remove(removedList.get(i).indexOfElemAffected);
        }
        
        lastVal=condensed.urlList.size();
        
        /* Merge all values not marked for deletion: */
        for(Delta d:dList)
        {
            if(d.added)
            {
                condensed.urlList.add(d.elemAffected);
            }
        }
        
        /* Update the clients: */
        clientIdList=db.getAllClientIds();
        
        for(Long l:clientIdList)
        {
            c=db.getClientById(l);
            
            if(c.expName.equals(existing.name))
            {
                for(i=0;i<c.dataMap.size();i++)
                {
                    for(Mapping m:map)
                    {
                        if(c.dataMap.get(i)==m.from)
                        {
                            c.dataMap.set(i, m.to);
                        }
                    }
                }
            }
        }
        
        return condensed;
    }
    
    /**
     * Compares two experiments and reports the changes between them.
     * Assumes that experiments are, in fact, supposed to be the same.
     * @param input
     * @param existing
     * @return 
     */
    public ArrayList<Delta> compareExperiments(Experiment input, Experiment existing)
    {
        boolean wasFound;
        ArrayList<Delta> deltaList=new ArrayList<Delta>();
        Delta d;
        
        /* Pass 1: get all of the additions: */
        for(DataElement i:input.urlList)
        {
            wasFound=false;
            for(DataElement e:existing.urlList)
            {
                if(e.equals(i))
                {
                    wasFound=true;
                }
            }
            
            if(!wasFound)
            {
                d=new Delta();
                d.added=true;
                d.elemAffected=i;
                d.indexOfElemAffected=existing.urlList.indexOf(i);
                
                deltaList.add(d);
            }
        }
        
        /* Pass 2: Get all of the removals:*/
        for(DataElement e:existing.urlList)
        {
            wasFound=false;
            for(DataElement i:input.urlList)
            {
                if(i.equals(e))
                {
                    wasFound=true;
                }
            }
            
            if(!wasFound)
            {
                d=new Delta();
                d.added=false;
                d.elemAffected=e;
                d.indexOfElemAffected=existing.urlList.indexOf(e);
                
                deltaList.add(d);
            }
        }
        
        return deltaList;
    }
}
