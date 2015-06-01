/*
 * This component of RSSE is intended to provide a low level means by which all of the functions
 * required of the Experimental Server are implemented. 
 * The ESS will contain the following threads and other such features:
 *  1. 
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
public class ExperimentalServerService
{
    private static final String NAME="java -jar ESS.jar";
    private static boolean loadConfig=true;
    private static String altConfigFile=null;
    
    public static Logger l=null;
    public static ResponseServer respSrv=null;
    public static SnippetServer snippetSrv=null;
    public static ClientDB db=null;
    public static ClientDBSaver saver=null;
    public static File dbSnapshotFile=null;
    
    public static void printHelp()
    {
        System.out.println("Usage: "+NAME+" [args]");
        System.out.println("Start and manage the RSSE Experimental Server Service");
        System.out.println("\t-h, --help\tPrint this message.");
        System.out.println("\t--genconfig\tPrompt for a configuration file.");
        System.out.println("\t--defconfig\tWrites default configuration to ./ess.conf");
        System.out.println("\t--defaults\tIgnores the configuration file and uses defaults");
        System.out.println("\t--useconf [file]\tUses the specified configuration file rather than the default.");
    }
    
    public static void interpretArgs(String[] args)
    {
        int i;
        
        for(i=0;i<args.length;i++)
        {
            if(args[i].equals("-h")||args[i].equals("--help"))
            {
                printHelp();
                System.exit(0);
            }
            
            else if(args[i].equals("--genconfig"))
            {
                Settings.promptSettingsFile(); //This function exits for me.
            }
            
            else if(args[i].equals("--defconfig"))
            {
                if(Settings.generateSettingsFile(new File("./ess.conf")))
                {
                    System.out.println("Default config written to ./ess.conf");
                    System.exit(0);
                }
                
                else
                {
                    System.err.println("ERROR: Unable to write default configuration file!");
                    System.exit(1);
                }
            }
            
            else if(args[i].equals("--defaults"))
            {
                loadConfig=false;
            }
            
            else if(args[i].equals("--useconf"))
            {
                if(args.length<=(i+1)) //Check if the next value would go out of bounds.
                {
                    System.err.println("ERROR: Configuration file not specified!");
                    System.exit(1);
                }
                
                else
                {
                    altConfigFile=args[i+1];
                }
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException
    {
        File settingsFile;
        
        l=new Logger();
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownRunnable()));
        
        interpretArgs(args);
        
        if(altConfigFile!=null)
        {
            if(Settings.readSettingsFile(new File(altConfigFile)))
            {
                System.out.println("Read configuration from "+altConfigFile);
            }
            
            else
            {
                System.err.println("Specified configuration file doesn't exist!");
                System.exit(1);
            }
        }
        
        else
        {
            //Try the default settings file:
            settingsFile=new File(Settings.configDir+"/ess.conf");
            
            if(!settingsFile.exists())
            {
                //Try the other settings file:
                settingsFile=new File("./ess.conf");
            }
            
            if(!settingsFile.exists())
            {
                System.err.println("Couldn't read either default or local settings file!");
                System.exit(1);
            }
            
            else
            {
                if(Settings.readSettingsFile(settingsFile))
                {
                    System.out.println("Read configuration from "+altConfigFile);
                }
                
                else
                {
                    System.err.println("No settings file can be found! Please run "+NAME+" --genconfig");
                }
            }
        }
        
        respSrv=new ResponseServer(Settings.responseServerPort, Settings.respDir);
        
        
    }
    
    private static final String ESS_TAG="ExperimentalServerService";
    
    //This runnable cleanly stops all threads and closes all connections.
    private static class ShutdownRunnable implements Runnable
    {
        @Override
        public void run()
        {
            if(respSrv!=null)
            {
                l.logMsg(ESS_TAG, "Stopping response server...");
                respSrv.stopServer();
            }
            
            if(snippetSrv!=null)
            {
                l.logMsg(ESS_TAG, "Stopping snippet server...");
                snippetSrv.stopServer();
            }
            
            if(saver!=null)
            {
                l.logMsg(ESS_TAG, "Stopping ClientDB snapshot thread...");
                saver.stop();
            }
            
            if(db!=null)
            {
                l.logMsg(ESS_TAG, "Writing ClientDB to file...");
                
                if(dbSnapshotFile!=null)
                {
                    try
                    {
                        PrintWriter outWriter=new PrintWriter(dbSnapshotFile);

                        db.dump(outWriter);
                        outWriter.flush();
                        outWriter.close();
                    } catch(FileNotFoundException e)
                    {
                        l.logErr(ESS_TAG, "Couldn't save database to: "+dbSnapshotFile);
                    }
                }
            }
        }
    }
    
}
