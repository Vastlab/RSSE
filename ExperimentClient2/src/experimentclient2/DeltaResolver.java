/*
 * This class provides the tools necessary to modify experiments given a delta string.
 */

package experimentclient2;

import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public class DeltaResolver
{
    private String deltaString;
    private ArrayList<Delta> deltaList;
    private DeltaTool t;
    
    public DeltaResolver(String deltaString)
    {
        this.deltaString=deltaString;
        
        t=new DeltaTool();
        deltaList=t.readDeltaList(deltaString);
    }
    
    public Experiment resolve(Experiment curE)
    {
        int curExpIndex;
        Experiment e=(Experiment) curE.clone();
        
        //Do removals first:
        for(Delta d:deltaList)
        {
            if(d.getType()==Delta.REMOVED)
            {
                for(DataElement elem:e.dataList)
                {
                    if(elem.URL.equals(d.getElement().URL))
                    {
                        e.dataList.remove(elem);
                    }
                }
            }
        }
        
        //Now add all of the elements:
        for(Delta d:deltaList)
        {
            if(d.getType()==Delta.ADDED)
            {
                e.dataList.add(d.getElement()); //Note: I may have to manually create a DataElement.
            }
        }
        return e;
    }
}
