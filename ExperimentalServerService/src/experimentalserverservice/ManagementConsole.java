/*
 * This is a generic client class for the ManagementServer implemented here.
 */

package experimentalserverservice;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ManagementConsole
{
    Connection connectionToUse;
    
    public ManagementConsole(String addr, int port)
    {
        try
        {
            connectionToUse=new Connection(new Socket(addr, port));
        } catch(IOException e)
        {
            System.err.println("Can't connect!");
        }
    }
    
    public ManagementConsole()
    {
        connectionToUse=null;
    }
    
    //This blocks the thread it's executed on.
    public void run()
    {
        Scanner userLineScanner;
        Scanner stringScanner;
        String userLine;
        
        userLineScanner=new Scanner(System.in);
        
        //Check if the caller has provided connection info:
        if(connectionToUse==null)
        {
            System.out.println("Enter server name: ");
            System.out.println("Enter server management port: ");
        }
        
        while(userLineScanner.hasNextLine())
        {
            
        }
    }
}
