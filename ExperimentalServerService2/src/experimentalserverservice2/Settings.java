/*
 * This class is a Makefile-generate-able class containing static defaults for the rest of
 * the program.
 */

package experimentalserverservice2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class Settings
{
    public static String dbDir="/var/rsse/ess/db";
    public static String configDir="/var/rsse/ess";
    public static String tmpDir="/var/rsse/ess/tmp";
    public static String respDir="/var/rsse/ess/resp";
    public static String experimentDir="/var/rsse/ess/experiments";
    
    //Define some constants for the ConfigurationManager:
    public static int nuggetServerPort=9002;
    public static String responseServerName="localhost";
    public static int responseServerPort=9003;
    
    public static int managementServerPort=8000;
    public static int fileUpdateServerPort=8001;
    
    /**
     * Configures the server to run locally. This is especially useful for 
     * debugging.
     */
    public static void runningLocally()
    {
        dbDir="./ess/db";
        configDir="./ess";
        tmpDir="./ess/tmp";
        respDir="./ess/resp";
        experimentDir="./ess/experiments";
    }
    
    /**
     * This changes the default settings so that the ESS shouldn't completely break on Windows.
     * As such, it assumes that it is installed and running from (drive):\rsse\
     */
    public static void updateForOS()
    {
        if(ExperimentalServerService2.runningOnWindows)
        {
            dbDir="./ess/db";
            configDir="./ess";
            tmpDir="./ess/tmp";
            respDir="./ess/resp";
            experimentDir="./ess/experiments";
        }
    }
    
    /**
     * Loads settings.
     * @param f
     * @return 
     */
    public static boolean readSettingsFile(File f)
    {
        boolean success=false;
        Scanner s;
        String temp;
        
        try
        {
            s=new Scanner(f);
            
            while(s.hasNext())
            {
                temp=s.next();
                
                if(temp.equals("dbdir"))
                {
                    dbDir=s.next();
                }
                
                else if(temp.equals("configdir"))
                {
                    configDir=s.next();
                }
                
                else if(temp.equals("tmpdir"))
                {
                    tmpDir=s.next();
                }
                
                else if(temp.equals("respdir"))
                {
                    respDir=s.next();
                }
                
                else if(temp.equals("nuggetserverport"))
                {
                    try
                    {
                        nuggetServerPort=Integer.parseInt(s.next());
                    } catch(NumberFormatException e)
                    {
                        nuggetServerPort=9002;
                        System.err.println("Error: nuggetserverport invalid. Defaulting to 9002.");
                    }
                }
                
                else if(temp.equals("responseserverport"))
                {
                    try
                    {
                        responseServerPort=Integer.parseInt(s.next());
                    } catch(NumberFormatException e)
                    {
                        responseServerPort=9003;
                        System.err.println("Error: responseserverport invalid. Defaulting to 9003.");
                    }
                }
                
                else if(temp.equals("experimentdir"))
                {
                    experimentDir=s.next();
                }
                
                else if(temp.equals("responseservername"))
                {
                    responseServerName=s.next();
                }
            }
            
            success=true;
        } catch(FileNotFoundException e)
        {
            
        }
        
        return success;
    }
    
    /**
     * Stores settings.
     * @param f
     * @return 
     */
    public static boolean generateSettingsFile(File f)
    {
        boolean success=false;
        PrintWriter out;
        
        if(!f.exists())
        {   
            try
            {
                f.createNewFile();
            } catch(IOException e)
            {
                
            }
        }
        
        try
        {
            out=new PrintWriter(f);
            
            out.println("dbdir "+dbDir);
            out.println("configdir "+configDir);
            out.println("tmpdir "+tmpDir);
            out.println("respdir "+respDir);
            out.println("nuggetserverport "+nuggetServerPort);
            out.println("responseserverport "+responseServerPort);
            out.println("responseservername "+responseServerName);
            out.println("experimentdir "+experimentDir);
            
            out.flush();
            out.close();
            
            success=true;
        } catch(FileNotFoundException e)
        {
            System.err.println("Couldn't save config file: "+f);
        }
        
        return success;
    }
    
    /**
     * Prompts the user for all of the entries in the settings file.
     */
    public static void promptSettingsFile()
    {
        File outFile;
        Scanner userScanner=new Scanner(System.in);
        String tmp;
        
        System.out.print("Please enter a configuration filename: ");
        outFile=new File(userScanner.nextLine());
        
        try
        {
            outFile.createNewFile();
        } catch(IOException e)
        {
            
        }
        
        while(!outFile.exists())
        {
            System.out.println("Couldn't create specified file.");
            System.out.print("Enter a new filename or ^C to exit: ");
            
            outFile=new File(userScanner.nextLine());
        
            try
            {
                outFile.createNewFile();
            } catch(IOException e)
            {

            }
        }
        
        System.out.print("Enter a location for user database snapshots [default="+dbDir+"]: ");
        tmp=userScanner.nextLine();
        if(!tmp.equals(""))
        {
            dbDir=tmp;
        }
        
        //Skipping configdir
        
        System.out.print("Enter a location for temporary data [default="+tmpDir+"]: ");
        tmp=userScanner.nextLine();
        if(!tmp.equals(""))
        {
            tmpDir=tmp;
        }
        
        System.out.print("Enter a location to store responses in [default="+respDir+"]: ");
        tmp=userScanner.nextLine();
        if(!tmp.equals(""))
        {
            respDir=tmp;
        }
        
        System.out.print("Enter a port for clients to get experiments from [default="+nuggetServerPort+"]: ");
        tmp=userScanner.nextLine();
        if(!tmp.equals(""))
        {
            nuggetServerPort=Integer.parseInt(tmp);
        }
        
        System.out.print("Enter a port for clients to respond to [default="+responseServerPort+"]: ");
        tmp=userScanner.nextLine();
        if(!tmp.equals(""))
        {
            responseServerPort=Integer.parseInt(tmp);
        }
        
        System.out.print("Enter the path to the experiment definitions file [default="+experimentDir+"]: ");
        tmp=userScanner.nextLine();
        if(!tmp.equals(""))
        {
            experimentDir=tmp;
        }
        
        System.out.print("Configuration complete. Now attempting to write the configuration...");
        
        if(generateSettingsFile(outFile))
        {
            System.out.println("Configuration successfully written!");
            System.exit(0);
        }
        
        else
        {
            System.err.println("Configuration failure!");
            System.exit(1);
        }
        
    }
}
