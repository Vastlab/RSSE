/*
 * This represents one of the experiments stored within an experiment file.
 * It is a very simple container class with a fairly lackluster constructor. 
 */

package experimentalserverservice;

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
        resPort=9001;
        urlList=new ArrayList<DataElement>();
    }
}
