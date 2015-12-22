/*
 * This is a dedicated client for the RSSE Cache Module.
 * It can either cache/fetch a list of URLs as represented in a file 
 * or stdin. This should allow greater flexibility, as the user can either point the Cache Client
 * at a full dump from the EC or just pipe the next URL into the Cache Module.
 */
package cacheclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class CacheClient 
{
    public static boolean fetchMode=false; //False = cache the file. True = cache and copy the file locally.
    public static Scanner source;
    public static Logger l;
    
    public static void printHelp()
    {
        PrintStream o=System.out;
        
        o.println("USAGE: java -jar CacheClient.jar [args] [filename]");
        o.print("Connect to and fetch data from an RSSE Cache Module");
        o.println("\t-h, --help\tPrint this message");
        o.println("\t-f,--fetch\tCaches and fetches data. The default is to cache only.");
        o.println("\t-s\tSpecifies Cache Module server address.");
        o.println("\t-p\tSpecifies Cache Module server port.");
        o.println("\t-sc [filename]\tSaves current Cache Module configuration to a file.");
        o.println("\t-lc [filename]\tUses a configuration file to provide remote server details.");
    }
    
    public static void interpretArgs(String[] args)
    {
        for(int i=0;i<args.length;i++)
        {
            String s=args[i];
            
            if(s.equals("-f")||s.equals("--fetch"))
            {
                fetchMode=true;
            }
            
            else if(s.equals("-h")||s.equals("--help"))
            {
                printHelp();
            }
            
            else if(s.equals("-s"))
            {
                i++;
                Settings.cacheModuleServerName=args[i];
            }
            
            else if(s.equals("-p"))
            {
                try
                {
                    i++;
                    Settings.cacheModuleServerPort=Integer.parseInt(args[i]);
                } catch(NumberFormatException e)
                {
                    System.err.println("ERROR: Cache module port missing or incorrectly specified.");
                    System.exit(1);
                }
            }
            
            else if(s.equals("-sc"))
            {
                i++;
                try
                {
                    Settings.writeSettings(new File(args[i]));
                } catch(FileNotFoundException e)
                {
                    System.err.println("Can't write configuration file "+args[i]+" for some reason.");
                    System.exit(1);
                }
            }
            
            else if(s.equals("-lc"))
            {
                i++;
                try
                {
                    Settings.readSettings(new File(args[i]));
                } catch(FileNotFoundException e)
                {
                    System.err.println("Can't find file: "+args[i]);
                    System.exit(1);
                }
            }
            
            else //Assume it's the input filename
            {
                try
                {
                    source=new Scanner(new File(args[i]));
                } catch(FileNotFoundException e)
                {
                    System.err.println("Input file not found or not correctly specified.");
                    System.err.println("Tried to load: "+args[i]);
                    
                    System.exit(1);
                }
            }
        }
    }
    
    public static void init(String[] args)
    {
        source=new Scanner(System.in);
        l=new DefaultStreamLogger();
        interpretArgs(args);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        init(args);
        
        while(source.hasNextLine())
        {
            String url=source.nextLine();
            
            CacheRequestProtocolClient c=new CacheRequestProtocolClient(Settings.cacheModuleServerName, 
                Settings.cacheModuleServerPort, l, "./");
            
            if(fetchMode)
            {
                try
                {
                    File f=c.connectExplicitRemote(url);
                    System.out.println("Fetched: "+f);
                } catch(CacheErrorException e)
                {
                    System.err.println("Couldn't fetch: "+url);
                }
            }
            
            else
            {
                try
                {
                    c.connectCache(url);
                    System.out.println("Cached: "+url);
                } catch(CacheErrorException e)
                {
                    System.err.println("Couldn't cache: "+url);
                }
            }
        }
    }
    
}
