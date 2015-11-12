/*
 * This class is intended to generate quick and easily readable lists of all changes between two experiments.
 * It does not do change resolution, rather it just computes differences. 
 */

package experimentclient2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class DeltaTool
{
    public DeltaTool()
    {
        
    }
    
    private boolean existsInList(Delta d, ArrayList<Delta> list)
    {
        for(Delta otherD:list)
        {
            if(otherD.getElement().URL.equals(d.getElement().URL))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public ArrayList<Delta> diff(Experiment oldE, Experiment newE)
    {
        ArrayList<DataElement> uniqueExperiments;
        ArrayList<Delta> deltaList=new ArrayList<Delta>();
        
        for(DataElement elem:oldE.dataList)
        {
            deltaList.add(new Delta(elem, oldE.dataList, newE.dataList));
        }
        
        for(DataElement elem:newE.dataList)
        {
            Delta d=new Delta(elem, oldE.dataList, newE.dataList);
            
            if(!existsInList(d, deltaList))
            {
                deltaList.add(d);
            }
        }
        
        return deltaList;
    }
    
    /**
     * Returns the differences between two experiments as a string.
     * @param oldE
     * @param newE
     * @return 
     */
    public String diffString(Experiment oldE, Experiment newE)
    {
        ArrayList<Delta> deltaList=diff(oldE, newE);
        
        String s="numdiffs: "+deltaList.size()+"\n";
        
        for(Delta d:deltaList)
        {
            s+=d+"\n";
        }
        
        return s;
    }
    
    public ArrayList<Delta> readDeltaList(String deltaString)
    {
        ArrayList<Delta> deltaList=new ArrayList<Delta>();
        Scanner lineScanner=new Scanner(deltaString);
        Scanner tokenScanner;
        String buf;
        
        //Skip past the number of deltas.
        buf=lineScanner.nextLine();
        //buf=lineScanner.nextLine();
        //numDiffs=Integer.parseInt(buf);
        
        tokenScanner=new Scanner(buf);
        
        //Validate the string.
        if(!tokenScanner.next().equals("numdiffs:"))
        {
            return null;
        }
        
        int numDeltas=Integer.parseInt(tokenScanner.next());
        
        for(int i=0;i<numDeltas;i++)
        {
            buf=lineScanner.next();
            deltaList.add(Delta.fromString(buf));
        }
        
        return deltaList;
    }
    
    public ArrayList<Delta> readDeltaList(InputStream s)
    {
        int numDiffs;
        ArrayList<Delta> deltaList=new ArrayList<Delta>();
        Scanner lineScanner=new Scanner(s);
        Scanner tokenScanner;
        String buf;
        
        //Read in the number of differences:
        buf=lineScanner.nextLine();
        numDiffs=Integer.parseInt(buf);
        tokenScanner=new Scanner(buf);
        
        if(!tokenScanner.next().equals("numdiffs:"))
        {
            return null;
        }
        
        int numDeltas=Integer.parseInt(tokenScanner.next());
        
        for(int i=0;i<numDeltas;i++)
        {
            buf=lineScanner.next();
            deltaList.add(Delta.fromString(buf));
        }
        
        return deltaList;
    }
}
