/*
 * This is a generic client class for the ManagementServer implemented here.
 */

package experimentalserverservice;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ManagementConsole
{
    private Connection connectionToUse;
    private String addr;
    private int port;
    
    public ManagementConsole(String addr, int port)
    {
        /*
        try
        {
            //connectionToUse=new Connection(new Socket(addr, port));
        } catch(IOException e)
        {
            System.err.println("Can't connect!");
        }*/
        
        this.addr=addr;
        this.port=port;
    }
    
    public ManagementConsole()
    {
        connectionToUse=null;
    }
    
    private void printHelp()
    {
        System.out.println();
        System.out.println("List of commands:");
        System.out.println("-----------------");
        System.out.println("stop\tStops the server");
        System.out.println("reload\tReloads server's configuration.");
        System.out.println("standby\tShuts down all services and waits.");
        System.out.println("mergenew [file]\tMerges new file into experiment list.");
        System.out.println("restart\tRestarts the ESS.");
        System.out.println("init\tInitializes the ESS (automatically done)");
        System.out.println("exit\tStops the console session.");
        System.out.println();
    }
    
    //This blocks the thread it's executed on.
    public void run()
    {
        Scanner userLineScanner;
        Scanner stringScanner;
        String userLine;
        String command;
        boolean runCmd;
        
        userLineScanner=new Scanner(System.in);
        
        //Check if the caller has provided connection info:
        if(connectionToUse==null)
        {
            System.out.println("Enter server name: ");
            System.out.println("Enter server management port: ");
        }
        
        printHelp();
        
        System.out.print(">");
        while(userLineScanner.hasNextLine())
        {
            runCmd=true;
            userLine=userLineScanner.nextLine();
            stringScanner=new Scanner(userLine);
            
            //Now scan the line sent in:
            command=stringScanner.next();
            ManagementRequest r=new ManagementRequest();
            
            if(command.equalsIgnoreCase("help"))
            {
                printHelp();
            }
            
            else if(command.equalsIgnoreCase("stop"))
            {
                r.reqArgs="";
                r.reqType=ManagementRequest.STOP;
            }
            
            else if(command.equalsIgnoreCase("reload"))
            {
               r.reqArgs="";
               r.reqType=ManagementRequest.RELOAD_CONF;
            }
            
            else if(command.equalsIgnoreCase("standby"))
            {
                r.reqArgs="";
                r.reqType=ManagementRequest.RESTART;
            }
            
            else if(command.equalsIgnoreCase("mergenew"))
            {
                System.out.print("New file to merge: ");
                r.reqArgs+=userLineScanner.nextLine();
                System.out.print("Name of experiment to merge: ");
                r.reqArgs+=userLineScanner.nextLine();
                r.reqType=ManagementRequest.MERGE_NEW;
            }
            
            else if(command.equalsIgnoreCase("restart"))
            {
                r.reqArgs="";
                r.reqType=ManagementRequest.RESTART;
            }
            
            else if(command.equalsIgnoreCase("init"))
            {
                r.reqArgs="";
                r.reqType=ManagementRequest.INIT;
            }
            
            else if(command.equalsIgnoreCase("exit"))
            {
                System.out.println("Ending console session...");
                return;
            }
            
            else
            {
                System.out.println("Invalid command. Run 'help' for help.");
                runCmd=false;
            }
            
            //Now send the request out:
            try
            {
                System.out.println("Opening connection to "+addr+":"+port);
                connectionToUse=new Connection(new Socket(addr, port));
                
                connectionToUse.out.println(r);
                if(connectionToUse.in.readLine().equalsIgnoreCase("err"))
                {
                    System.err.println("Error in command.");
                }
                
                else
                {
                    System.out.println("OK");
                }
            } catch(UnknownHostException e)
            {
                
            } catch(IOException ex)
            {
                
            }
            
            System.out.print(">");
        }
    }
}
