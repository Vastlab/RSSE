/*
 * Wraps a change between two experiments such that it's easier to resolve them. 
 * An ArrayList of this class is used by the DeltaTool.
 */

package experimentclient2;

import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class Delta
{
    //Declare some types:
    public static final int NONE=0;
    public static final int ADDED=1;
    public static final int REMOVED=2;
    public static final int SAME=3;
    public static final String[] NAMES={"NONE", "ADDED", "REMOVED", "SAME"};
    
    private int type;
    private DataElement internalElement;
    
    public Delta(DataElement e, ArrayList<DataElement> oldList, ArrayList<DataElement> newList)
    {
        type=0;
        boolean wasFound;
        boolean isNew;
        
        internalElement=e;
        
        wasFound=false;
        isNew=true;
        
        for(DataElement elem:newList)
        {
            if(e.URL.equals(elem.URL))
            {
                wasFound=true;
                break;
            }
        }
        
        if(!wasFound)
        {
            type=REMOVED;
            return;
        }
        
        for(DataElement elem:oldList)
        {
            if(e.URL.equals(elem.URL))
            {
                isNew=false;
                break;
            }
        }
        
        if(isNew)
        {
            type=ADDED;
        }
        
        else
        {
            type=SAME;
        }
    }
    
    public Delta(DataElement e, DataElement newList[])
    {
        type=0;
    }
    
    public int getType()
    {
        return type;
    }
    
    public DataElement getElement()
    {
        return internalElement;
    }
    
    @Override
    public String toString()
    {
        String s;
        
        s=internalElement.URL+" "+NAMES[type];
        
        return s;
    }
    
    private Delta()
    {
        type=0;
        internalElement=null;
    }
    
    public static Delta fromString(String s)
    {
        Scanner stringScanner=new Scanner(s);
        Delta d=new Delta();
        String url;
        String type;
        
        url=stringScanner.next();
        type=stringScanner.next();
        
        for(int i=0;i<NAMES.length;i++)
        {
            if(type.equals(NAMES[i]))
            {
                d.type=i;
                break;
            }
        }
        
        DataElement e=new DataElement();
        e.URL=url;
        d.internalElement=e;
        
        return d;
    }
}
