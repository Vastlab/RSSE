/*
 * This class contains the methods and other such stuff necessary to add a CLI to the 
 * cache module if the user runs it either in interactive mode or remotely. 
 *
 * Some comments on the design of this CLI:
 *      1. It is designed to be minimally dependent on the CM's code in and of itself.
 *      2. It uses only interfaces that have been exposed in some networking protocol.
 *      3. It is specific to this implementation of the RSSE Cache Module.
 *
 * The CMTerminal can actually execute some of its own commands. Those are:
 *      helplocal -- prints help for local commands.
 *      ls      -- lists the current directory locally.
 *      cd      -- changes the local directory.
 *      send    -- Sends the file specified to the remote server.
 *      setsrvr -- Changes the server to be connected to.
 *      setport -- Changes the port to connect to.
 *      connect -- Verifies that the server specified can be connected to.
 *
 * There is also another set of commands that the CM can return to the CMTerminal:
 *      PRINT   -- Prints all of the data to the screen.
 * 
 */

package cachemodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class CMTerminal
{
    private String remoteHost;
    private int remotePort;
    private File curPath;
    
    public CMTerminal(String newRemoteHost, int newPort, String newPath)
    {
        remoteHost=newRemoteHost;
        remotePort=newPort;
        curPath=new File(newPath);
    }
    
    public CMTerminal()
    {
        remoteHost=null;
        remotePort=9000;
        curPath=new File("."); //Just about every OS supports this path.
    }
    
    public CMTerminal(String host)
    {
        remoteHost=host;
        remotePort=9000;
        curPath=new File(".");
    }
    
    private ManagementQuery makeConnection(ManagementQuery q) throws IOException
    {
        ManagementQuery retQ;
        Socket s=new Socket(remoteHost, remotePort);
        System.out.println("Starting MakeConnection...");
        System.out.println(s.isBound());
        
        PrintWriter out=new PrintWriter(s.getOutputStream(), true);
        BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()));
        
        //Print out the request:
        out.println(q.toString());
        System.out.println("Printed data. Waiting on response...");
        //Get the server's response:
        retQ=new ManagementQuery().fromString(in.readLine());
        
        out.close();
        in.close();
        s.close();
        
        return retQ; //That was easy!
    }
    
    private void runHelp(ManagementQuery q)
    {
        CMTerminalHelpEngine e=new CMTerminalHelpEngine();
        
        if(q.args.isEmpty())
        {
            e.printHelp("none");
        }
        
        else
        {
            e.printHelp(q.args.get(0));
        }
    }
    
    /**
     * This executes any query returned from the CM.
     * @param q 
     */
    private void execManagementQuery(ManagementQuery q)
    {
        //Handle all commands that can be returned from a server.
        if(q.command.equals("PRINT"))
        {
            System.out.println("Output from "+remoteHost);
            
            for(String s:q.args)
            {
                System.out.println(s);
            }
        }
        
        else if(q.command.equals("PRINTERR"))
        {
            System.err.println(remoteHost+" error:");
            
            for(String s:q.args)
            {
                System.err.println(s);
            }
        }
        
        //Now handle all of the user commands:
        
        if(q.command.equalsIgnoreCase("cd"))
        {
            File temp=new File(q.args.get(0));
            
            if(temp.isDirectory())
            {
                curPath=temp;
            }
            
            else
            {
                System.err.println("ERROR: Specified directory doesn't exist!");
            }
        }
        
        else if(q.command.equalsIgnoreCase("help"))
        {
            runHelp(q);
        }
        
        else if(q.command.equalsIgnoreCase("pwd"))
        {
            System.out.println(curPath);
        }
        
        else if(q.command.equalsIgnoreCase("ls"))
        {
            File fileArr[]=curPath.listFiles();
            
            System.out.println("Directory listing for "+curPath);
            
            for(File f:fileArr)
            {
                System.out.println(f);
            }
        }
        
        else if(q.command.equalsIgnoreCase("setsrvr"))
        {
            /* This code path should be enabled if such checking is necessary.
            try
            {
                Socket s=new Socket(q.args.get(0), remotePort);
                s.close();
                
                remoteHost=q.args.get(0);
            } catch(IOException e)
            {
                System.err.println("ERROR: Specified server doesn't exist!");
            }*/
            
            remoteHost=q.args.get(0);
        }
        
        else if(q.command.equalsIgnoreCase("setport"))
        {
            //See the note on alternative code paths above.
            
            remotePort=Integer.parseInt(q.args.get(0));
        }
        
        else if(q.command.equals("")||q.command==null)
        {
            //Break the recursion that may occur in these circumstances.
        }
        
        else //These will be shipped off to the server.
        {
            //Yes, this is some pseudo-recursion:
            try
            {
                System.out.println("Sending command to server...");
                execManagementQuery(makeConnection(q));
            } catch(IOException e)
            {
                System.err.println("ERROR: Couldn't connect to server!");
            }
        }
    }
    
    private String prompt()
    {
        Scanner userScanner=new Scanner(System.in);
        
        if(remoteHost==null)
        {
            System.out.print("(no server)");
        }
        
        else
        {
            System.out.print(remoteHost);
        }
        
        System.out.print(" -> ");
        
        return userScanner.nextLine();
    }
    
    private String prompt(String prmpt)
    {
        Scanner userScanner=new Scanner(System.in);
        
        System.out.print(prmpt);
        
        return userScanner.nextLine();
    }
    
    public void runInterpreter()
    {
        String userBuffer;
        String userCommand;
        
        System.out.println("Cache Module CLI Interface Program");
        System.out.println("Version 1");
        
        while(true)
        {
            userBuffer=prompt();
            System.out.println(userBuffer);
            
            if(userBuffer.equalsIgnoreCase("exit"))
            {
                return;
            }
            
            ManagementQuery q=new ManagementQuery().fromString(userBuffer); //User buffer conveniently has the same formatting as this.
            
            execManagementQuery(q);
        }
    }
}
