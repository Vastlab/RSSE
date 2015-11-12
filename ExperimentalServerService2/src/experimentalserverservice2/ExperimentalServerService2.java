/*
 * The Experimental Server Service as originally conceived (mostly) worked for what it was 
 * written and intended to do: chew up and spit out small chunks of experiment files for clients to work with.
 *
 * This was all perfectly fine, but when it became necessary to add significant functionality to the project, the previous model
 * fell flat.
 *
 * This will be the same as the original ExperimentalServerService, but with the following:
 *  1. Significantly reworked client DB and experiment file support 
 *  2. Result support
 *  3. Proper experiment change support.
 *  4. A more powerful interface.
 */

package experimentalserverservice2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ExperimentalServerService2
{
    public static final boolean runningOnWindows=false;
    public static Logger l;
    public static Settings settings;
    public static boolean loadConfigFile=true;
    public static String defaultConfigFile="/var/rsse/ess/config.conf";
    public static String errorFile=null;
    public static String messageFile=null;
    public static DataServer dataServer;
    public static boolean runningLocally=false;
    
    public static ArrayList<Experiment> experimentList;
    
    public static void printHelp()
    {
        System.out.println("Usage: java -jar ExperimentalServerService2.jar [args]");
        System.out.println("Start and manage the RSSE Experimental Server Service");
        System.out.println("\t-h, --help\tPrint this message.");
        System.out.println("\t--genconfig\tPrompt for a configuration file.");
        System.out.println("\t--defconfig\tWrites default configuration to ./ess.conf");
        System.out.println("\t--defaults\tIgnores the configuration file and uses defaults");
        System.out.println("\t--useconf [file]\tUses the specified configuration file rather than the default.");
        System.out.println("\t-lf, --logfile\tLogs all output to a file.");
        System.out.println("\t-ef, --errfile\tIf \"--logfile\" is already specified, logs all errors to this ");
        System.out.println("\t\tfile rather than the file specified by \"--logfile\".");
        System.out.println("\t-uf, --updatefile\tUpdates the experiment definition file with new information.");
        System.out.println("\t---local\tStores data locally rather than in system directories.");
    }
    
    /**
     * Interprets command-line arguments.
     * @param args 
     */
    public static void interpretArgs(String args[])
    {
        for(int i=0;i<args.length;i++)
        {
            String s=args[i];
            
            if(s.equalsIgnoreCase("-h")||s.equalsIgnoreCase("--help"))
            {
                printHelp();
                System.exit(1);
            }
            
            else if(s.equalsIgnoreCase("--local"))
            {
                Settings.runningLocally();
                runningLocally=true;
            }
            
            else if(s.equalsIgnoreCase("--genconfig"))
            {
                Settings.promptSettingsFile();
                System.exit(1);
            }
            
            else if(s.equalsIgnoreCase("--defconfig"))
            {
                if((i+1)>=args.length)
                {
                    System.err.println("ERROR! New configuration file not specified!");
                    System.exit(1);
                }
                
                else
                {
                    Settings.readSettingsFile(new File(args[i+1]));
                    i++;
                }
            }
            
            else if(s.equalsIgnoreCase("--defaults"))
            {
                loadConfigFile=false;
            }
            
            else if(s.equalsIgnoreCase("--useconf"))
            {
                if((i+1)>=args.length)
                {
                    System.out.println("ERROR: Config file not specified!\n");
                    System.exit(1);
                }
                
                else
                {
                    defaultConfigFile=args[i+1];
                    i++;
                }
            }
            
            else if(s.equalsIgnoreCase("-lf")||s.equalsIgnoreCase("--logfile"))
            {
                if((i+1)>=args.length)
                {
                    System.err.println("ERROR: Log file not specififed!");
                    System.exit(1);
                }
                
                else
                {
                    messageFile=args[i+1];
                    i++;
                }
            }
            
            else if(s.equalsIgnoreCase("-ef")||s.equalsIgnoreCase("--errfile"))
            {
                if((i+1)>=args.length)
                {
                    System.err.println("ERROR: Error log file not specified!");
                    System.exit(1);
                }
                
                else
                {
                    errorFile=args[i+1];
                    i++;
                }
            }
        }
    }
    
    /**
     * Inits the ESS.
     */
    public static void init()
    {
        if(runningLocally)
        {
            defaultConfigFile="config.conf";
            Settings.runningLocally();
        }
        //First, get the logger ready:
        if(errorFile!=null||messageFile!=null)
        {
            if(errorFile!=null&&messageFile!=null)
            {
                try
                {
                    l=new FileLogger(new File(messageFile), new File(errorFile));
                } catch(FileNotFoundException e)
                {
                    System.err.println("ERROR: Can't log to requested files. Using stdin and stderr.");
                    l=new DefaultStreamLogger();
                }
                
            }
            
            else if(messageFile!=null)
            {
                try
                {
                    l=new FileLogger(new File(messageFile));
                } catch(FileNotFoundException e)
                {
                    System.err.println("ERROR: Can't log to requested file. Using stdin and stderr.");
                    l=new DefaultStreamLogger();
                }
            }
        }
        
        else
        {
            l=new DefaultStreamLogger();
        }
        
        //Now try to load a configuration file:
        if(loadConfigFile&&!runningLocally)
        {
            if(Settings.readSettingsFile(new File(defaultConfigFile)))
            {
                l.logMsg("Init", "Loaded configuration file: "+defaultConfigFile);
            }
        }
        
        else
        {
            //Do nothing, all defaults should be present.
            if(runningLocally)
            {
                l.logMsg("Init", "Note: configuration ignored; running locally.");
            }
        }
        
        //With our new configuration, try to build or validate a directory structure:
        checkMakeDirs();
        
        //Read all experiments:
        l.logMsg("Init", "Loading experiments.");
        
        if(loadExperiments(new File(Settings.experimentDir)))
        {
            l.logMsg("Init", "Successfully loaded "+experimentList.size()+" experiment(s).");
        }
        
        else
        {
            if(experimentList.isEmpty())
            {
                l.logErr("Init", "Error: loaded no experiments from experiment directory!");
                System.exit(2);
            }
            
            else
            {
                l.logErr("Init", "Error: couldn't load all experiments.");
            }
        }
        
        //Now start the actual server service:
        l.logMsg("Init", "Spawning data server thread...\n");
        dataServer=new DataServer(l,  experimentList, Settings.nuggetServerPort);
        try
        {
            dataServer.start();
        } catch(IOException e)
        {
            l.logErr("Init", "Failed to spawn dataServer! This is very bad.");
            System.exit(3);
        }
        
        //aaaand the results service as well:
        l.logMsg("Init", "Spawning results server thread...\n");
    }
    
    //Shuts down all services and saves anything else. 
    public static void deInit()
    {
        if(dataServer.isRunning())
        {
            dataServer.stop();
        }
    }
    
    /**
     * Checks if a directory exists, and if it doesn't, makes it.
     * @param dirName 
     */
    private static void checkMake(String dirName)
    {
        File f=new File(dirName);
        
        if(!f.exists())
        {
            l.logErr("Init", "Directory "+dirName+" doesn't exist. Creating...");
            f.mkdirs();
        }
    }
    
    /**
     * Attempts to check for the directories necessary and make them as appropriate.
     */
    public static void checkMakeDirs()
    {
        checkMake(Settings.configDir);
        checkMake(Settings.dbDir);
        checkMake(Settings.experimentDir);
        checkMake(Settings.respDir);
        checkMake(Settings.tmpDir);
    }
    
    public static boolean loadExperiments(File experimentDir)
    {
        experimentList=new ArrayList<Experiment>();
        File files[]=experimentDir.listFiles();
        
        if(files==null)
        {
            l.logErr("Init", "Experiment directory "+experimentDir+" doesn't exist. Exiting...");
            System.exit(1);
        }
        
        else if(files.length==0)
        {
            l.logErr("Init", "No experiments were found in "+experimentDir+". Exiting...");
            System.exit(1);
        }
        
        for(File f:files)
        {
            //Read only RSSE files:
            if(f.getName().contains(".rsse"))
            {
                try
                {
                    Experiment e=new Experiment(f, l);
                    experimentList.add(e);
                } catch(IOException ex)
                {
                    l.logErr("ExperimentLoader", "Warning: Can't load experiment file: "+f.getAbsolutePath());
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public static void printConsoleHelp()
    {
        PrintStream o=System.out;
        
        o.println("Command List: help");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        interpretArgs(args);
        init();
        
        Scanner s=new Scanner(System.in);
        String tmp;
        System.out.println("Press enter to stop the ESS or type \"help\" for more options.");
        System.out.print(">");
        tmp=s.nextLine();
        
        while(!tmp.equals(""))
        {
            if(tmp.equalsIgnoreCase("help"))
            {
                printConsoleHelp();
            }
            tmp=s.nextLine();
            System.out.print(">");
        }
        s.nextLine();
        
        deInit();
    }
    
}
