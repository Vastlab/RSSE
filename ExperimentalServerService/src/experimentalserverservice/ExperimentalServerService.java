/*
 * This component of RSSE is intended to provide a low level means by which all of the functions
 * required of the Experimental Server are implemented. 
 * The ESS will contain the following threads and other such features:
 *  1. 
 */

package experimentalserverservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ExperimentalServerService
{
    
    private static final String ESS_TAG="ExperimentalServerService";
    private static final long DB_SAVE_INTERVAL=10000; //This should work for now.
    private static final String NAME="java -jar ExperimentalServerService.jar";
    private static boolean loadConfig=true;
    private static String altConfigFile=null;
    
    public static boolean runningOnWindows;
    public static Logger l=null;
    public static ResponseServer respSrv=null;
    public static SnippetServer snippetSrv=null;
    public static ClientDB db=null;
    public static ClientDBSaver saver=null;
    public static File dbSnapshotFile=null;
    public static File experimentFile=null;
    public static ArrayList<Experiment> experimentList;
    
    /**
     * Check if this program is running on Windows so that it's a bit less user-hostile.
     * @return 
     */
    public static boolean checkIfWindows()
    {
        String os;
        
        os=System.getProperty("os.name");
        
        return os.startsWith("Windows");
    }
    
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
    
    public static void loadDb(File f)
    {
        if(f.exists())
        {
            db=new ClientDB(false);
            
            try
            {
                System.out.println("Reading database from "+f);
                db.restore(f);
            } catch(FileNotFoundException e)
            {
                //This really should never happen.
            }
        }
        
        else
        {
            db=new ClientDB(true);
        }
    }
    
    public static void testSnippetProtocol()
    {
        SnippetProtocolClient c=new SnippetProtocolClient(l);
        
        ArrayList<String> expList=c.getExperimentList("localhost", Settings.nuggetServerPort, 0);
        
        for(String s:expList)
        {
            System.out.println(s);
        }
        
        System.exit(5);
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
                    l.logMsg(ESS_TAG, "Default config written to ./ess.conf");
                    System.exit(0);
                }
                
                else
                {
                    l.logErr(ESS_TAG, "ERROR: Unable to write default configuration file!");
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
                    l.logErr(ESS_TAG, "ERROR: Configuration file not specified!");
                    System.exit(1);
                }
                
                else
                {
                    altConfigFile=args[i+1];
                }
            }
            
            else if(args[i].equals("--testSnippetProtocol"))
            {
                testSnippetProtocol();
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException
    {
        File settingsFile;
        Parser p;
        
        //Check for OS before anything else:
        runningOnWindows=checkIfWindows();
        Settings.updateForOS();
        
        l=new DefaultStreamLogger();
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownRunnable()));
        
        interpretArgs(args);
        
        if(altConfigFile!=null)
        {
            if(Settings.readSettingsFile(new File(altConfigFile)))
            {
                l.logMsg(ESS_TAG,"Read configuration from "+altConfigFile);
            }
            
            else
            {
                l.logErr(ESS_TAG,"Specified configuration file doesn't exist!");
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
                l.logErr(ESS_TAG, "Couldn't read either default or local settings file!");
                System.exit(1);
            }
            
            else
            {
                if(Settings.readSettingsFile(settingsFile))
                {
                    l.logMsg(ESS_TAG, "Read configuration from "+altConfigFile);
                }
                
                else
                {
                    l.logErr(ESS_TAG, "No settings file can be found! Please run "+NAME+" --genconfig");
                }
            }
        }
        
        respSrv=new ResponseServer(Settings.responseServerPort, Settings.respDir, l);
        experimentFile=new File(Settings.experimentFile);
        p=new Parser(l);
        
        experimentList=p.parseExperimentFile(experimentFile);
        
        if(experimentList.size()==0)
        {
            l.logErr(ESS_TAG, "No experiments defined in experiment list!");
            System.exit(1);
        }
        
        dbSnapshotFile=new File(Settings.dbDir+"/snapshot.file");
        loadDb(dbSnapshotFile);
        snippetSrv=new SnippetServer(Settings.nuggetServerPort, db, experimentList, l);
        
        saver=new ClientDBSaver(db, dbSnapshotFile, l, DB_SAVE_INTERVAL);
        
        //Now that everything is ready, start all services:
        saver.start();
        respSrv.startServer();
        snippetSrv.startServer();
        
        //In the future, this portion will start a management console.
        l.logMsg(ESS_TAG, "Ready. Type \"stop\" to stop the server.");
        
        Scanner userScanner=new Scanner(System.in);
        
        while(true)
        {
            if(userScanner.nextLine().equals("stop"))
            {
                System.exit(0);
            }
        }
    }
    
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
                    if(!dbSnapshotFile.exists())
                    {
                        try
                        {
                            dbSnapshotFile.createNewFile();
                        } catch(IOException e)
                        {
                        }
                    }
                    
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
