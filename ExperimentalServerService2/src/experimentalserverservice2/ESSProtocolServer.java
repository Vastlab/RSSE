/*
 * This is the server side of the new and significantly better ESS.
 * In the second version of the Experimental Server Service, all functionality relating to keeping track of clients has been pushed
 * to clients themselves. Overall, this makes far more sense as a system and should significantly reduce complexity on the part 
 * of the server. 
 */

package experimentalserverservice2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public class ESSProtocolServer
{
    private static final String PROTOCOL_TAG="ESSProtocolServer";
    private Connection c;
    private ArrayList<Experiment> experiments;
    private Logger l;
    
    /**
     * Default constructor. 
     * @param c Connection to use.
     * @param experiments list of experiments to check and send to the client.
     * @param l Logger object to use.
     */
    public ESSProtocolServer(Connection c, ArrayList<Experiment> experiments, Logger l)
    {
        this.c=c;
        this.experiments=experiments;
        this.l=l;
    }
    
    public void serve()
    {
        PrintWriter out=c.out;
        BufferedReader in=c.in;
        InputStream rawIn=c.rawIn;
        String buf;
        
        l.logMsg("Protocol", "Contacted to get experiment data.");
        
        try
        {
            buf=in.readLine();
            //System.out.println("Got command: "+buf);
            l.logMsg("READ", buf);
            
            //Prints out a list of all of the experiments 
            if(buf.equals("getexperiments"))
            {
                //Print the number of experiments.
                out.println(experiments.size());
                l.logMsg("WRITE", ""+experiments.size());
                
                //Now print out all experiment titles:
                for(Experiment exp:experiments)
                {
                    out.println(exp.experimentName);
                    l.logMsg("WRITE", exp.experimentName);
                }
            }
            
            else if(buf.equals("getdata"))
            {
                //Read in the title of the experiment to get data for.
                buf=in.readLine();
                
                l.logMsg("READ", buf);
                
                Experiment expToUse=null;
                
                //This likely won't be too big an obstacle to scaling in the future.
                //I doubt that many servers will have more than just a few experiments.
                for(Experiment exp:experiments)
                {
                    if(exp.experimentName.equals(buf))
                    {
                        expToUse=exp;
                        break;
                    }
                }
                
                if(expToUse==null)
                {
                    l.logMsg("WRITE", "NOEXIST");
                    out.println("NOEXIST"); //Inform the client that that experiment doesn't exist.
                }
                
                else //Print out the dataset:
                {
                    l.logMsg("WRITE", "OK");
                    System.out.println("[WRITE] EXPERIMENT:");
                    System.out.println(expToUse);
                    out.println("OK");
                    expToUse.dumpToStream(out);
                }
            }
            
            else if(buf.equals("delta"))
            {
                //Read the experiment's name:
                buf=in.readLine();
                
                Experiment expToUse=null;
                
                //Try to find the experiment:
                for(Experiment exp:experiments)
                {
                    if(exp.experimentName.equals(buf))
                    {
                        expToUse=exp;
                        break;
                    }
                }
                
                if(expToUse==null)
                {
                    out.println("NOEXIST");
                }
                
                else
                {
                    out.println("OK");
                    
                    //Read the experiment sent:
                    Experiment remoteExp=new Experiment(l);
                    remoteExp.readFromStream(rawIn);
                    
                    //Now compute and print the deltas:
                    DeltaTool tool=new DeltaTool();
                    out.println(tool.diffString(remoteExp, expToUse));
                    out.println("END");
                }
            }
            
            c.close();
        
        //Now try to parse the value provided:
        } catch(IOException e)
        {
            l.logErr(PROTOCOL_TAG, "IOException while handling server connection.");
        }
    }
}
