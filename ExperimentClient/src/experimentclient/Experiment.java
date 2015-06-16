/*
 * This represents one of the experiments stored within an experiment file.
 * It is a very simple container class with a fairly lackluster constructor. 
 */

package experimentclient;

import java.util.ArrayList;

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
    
    public Experiment()
    {
        name=null;
        description=null;
        report=false;
        resServer="localhost";
        resPort=9003;
        urlList=new ArrayList<DataElement>();
    }
    
    @Override
    public String toString()
    {
        String s="Title: "+name;
        s+="\nDescription: "+description+"\n";
        
        return s;
    }
}
