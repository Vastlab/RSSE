/*
 * Contains the information necessary to connect to a cache module.
 */
package cacheclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class Settings 
{
    public static String cacheModuleServerName="localhost";
    public static int cacheModuleServerPort=9001;
 
    public static void readSettings(File f) throws FileNotFoundException
    {
        Scanner s=new Scanner(f);
        
        cacheModuleServerName=s.nextLine();
        
        try
        {
            cacheModuleServerPort=Integer.parseInt(s.nextLine());
        } catch(NumberFormatException e)
        {
            System.err.println("Configuration file is corrupted or incorrect.");
            System.exit(1);
        }
    }
    
    public static void writeSettings(File f) throws FileNotFoundException
    {
        PrintWriter pw=new PrintWriter(f);
        
        pw.println(cacheModuleServerName);
        pw.println(cacheModuleServerPort);
    }
}
