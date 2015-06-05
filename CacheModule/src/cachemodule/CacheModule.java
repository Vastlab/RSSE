/*
 * This is a caching module for use with the RSSE software stack.
 */

package cachemodule;

import java.io.File;

/**
 *
 * @author mgohde
 */
public class CacheModule
{
    public static boolean runningOnWindows;
    public static Logger l;
    public static CMConfig cfg;
    public static Database database;
    public static ConcurrentRequestManager reqMan;
    
    //Now for all of the servers:
    public static CacheRequestServer cacheSrvr=null;
    public static ManagementServer manageSrvr;
    
    public static void printHelp()
    {
        System.out.println("Usage: java -jar CacheModule.jar [args]");
        System.out.println("Start or manage the RSSE Caching Service.");
        System.exit(0);
    }
    
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
    
    public static void interpretArgs(String args[])
    {
        for(String s:args)
        {
            if(s.equals("--genconfig"))
            {
                System.out.println("Generating configuration file...");
                cfg=new CMConfig();
                cfg.saveSettings(new File("./cm.conf"));
                System.exit(0);
            }
            
            else if(s.equals("-h")||s.equals("--help"))
            {
                
            }
            
            else if(s.equals("-t")||s.equals("--terminal"))
            {
                CMTerminal t=new CMTerminal();
                t.runInterpreter();
                
                System.exit(0);
            }
            
            else if(s.equals("--headless"))
            {
                //Shouldn't matter yet.
            }
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        runningOnWindows=checkIfWindows();
        
        l=new Logger();
        cfg=new CMConfig();
        database=new Database();
        
        interpretArgs(args);
        
        cfg.loadSettings(cfg.getDefaultSettingsFile());
        
        /*
        //Let's try some requests:
        CacheRequestServer server=new CacheRequestServer(cfg, database, l);
        server.startServer();
        
        CacheRequestProtocolClient client=new CacheRequestProtocolClient("localhost", 9001, l, "/home/mgohde/rsse/clientstuff/");
        
        try
        {
            File f=client.connect("http://www.playtool.com/pages/agpcompat/agp.html");
            
            System.out.println("f="+f);
        } catch(CacheErrorException e)
        {
            System.err.println(e);
        }
        
        server.stopServer();*/
        
        manageSrvr=new ManagementServer(9998, true);
        manageSrvr.start();
        
        //cacheSrvr=new CacheRequestServer(CacheModule.cfg, database, l, new File(CacheModule.cfg.getSetting(CMConfig.SETTING_STORAGE_DIR)+"/db.snapshot"));
        //cacheSrvr.startServer();
        
        CMTerminal t=new CMTerminal("localhost");
        
        t.runInterpreter();
    }
    
}
