/*
 * This class loads and handles configuration for the ESS.
 */

package experimentalserverservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ConfigurationManager
{
    private int URLport;
    private int respPort;
    private String respServerName;
    
    //Set to defaults:
    public ConfigurationManager()
    {
        URLport=Defaults.nuggetServerPort;
        respPort=Defaults.responseServerPort;
        respServerName="localhost";
    }
    
    public void loadConfig(File confFile) throws FileNotFoundException
    {
        Scanner s=new Scanner(confFile);
        String tmp;
        
        if(s.nextLine().equals("ESS Configuration Version 1"))
        {
            while(s.hasNext())
            {
                tmp=s.next();
                
                if(tmp.equals("urlport"))
                {
                    URLport=Integer.parseInt(s.next());
                }
                
                else if(tmp.equals("respport"))
                {
                    respPort=Integer.parseInt(s.next());
                }
                
                else if(tmp.equals("respservername"))
                {
                    respServerName=s.next();
                }
            }
        }
        
        else
        {
            throw new FileNotFoundException();
        }
    }
    
    public void saveConfig(File confFile) throws FileNotFoundException
    {
        PrintWriter out=new PrintWriter(confFile);
        
        out.println("ESS Configuration Version 1"); //This should always be the first line.
        out.println("urlport "+URLport);
        out.println("respport "+respPort);
        out.println("respservername "+respServerName);
    }
    
    public String getRespServerName()
    {
        return respServerName;
    }
    
    public int getRespServerPort()
    {
        return respPort;
    }
    
    public int getUrlPort()
    {
        return URLport;
    }
}
