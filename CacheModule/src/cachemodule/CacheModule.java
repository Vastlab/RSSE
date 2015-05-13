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
    public static Logger l;
    public static CMConfig cfg;
    public static Database database;
    public static ConcurrentRequestManager reqMan;
    
    //Now for all of the servers:
    public static CacheRequestServer cacheSrvr;
    public static ManagementServer manageSrvr;
    
    public static void interpretArgs(String args[])
    {
        for(String s:args)
        {
            if(s.equals("--genconfig"))
            {
                System.out.println("Generating configuration file...");
                cfg=new CMConfig();
                cfg.saveSettings(new File("/home/mgohde/rsse/cm.conf"));
            }
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
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
        
        manageSrvr=new ManagementServer(9002, true);
        manageSrvr.start();
        
        CMTerminal t=new CMTerminal();
        
        t.runInterpreter();
    }
    
}
