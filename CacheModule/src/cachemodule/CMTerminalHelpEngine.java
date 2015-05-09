/*
 * This is a generic help engine for when users need interactive, online help.
 * In the future, this engine will load XML files contianing help messages so that 
 * the information contained there may be translated into different languages. 
 */

package cachemodule;

import java.io.File;

/**
 *
 * @author mgohde
 */
public class CMTerminalHelpEngine
{
    private String[] cmdNames={"none", "cd", "help", "pwd", "ls", "setsrvr", "setport"};
    private String[] cmdDescriptions={
            "Enter \"help [cmd]\" to get help for a command.",
            "Changes the current local directory.",
            "Runs the help system.",
            "Prints the current working directory.",
            "Prints the contents of the current local directory.",
            "Sets a new server to connect to and manage.",
            "Changes the port to connect to for management."
    };
    
    public CMTerminalHelpEngine()
    {
        
    }
    
    public void loadHelp(File f)
    {
        //TODO: Implement this in RSSE version 2.
    }
    
    public void printHelp(String key)
    {
        int i;
        
        for(i=0;i<cmdNames.length;i++)
        {
            if(cmdNames[i].equals(key))
            {
                System.out.println("Help for "+cmdNames[i]);
                System.out.println(cmdDescriptions[i]);
                return;
            }
        }
    }
}
