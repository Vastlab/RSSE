/*
 * This object encapsulates all of the data necessary for the CMTerminal to send management
 * commands and such to the Cache Module.
 *
 * It is also used to return the results of a command to the CMTerminal using CMTerminal commands.
 */

package cachemodule;

import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ManagementQuery
{
    public String command;
    public ArrayList<String> args;
    
    public ManagementQuery()
    {
        command="";
        args=new ArrayList<String>();
    }
    
    /**
     * This method converts the ManagementQuery into a single string.
     * @return the ManagementQuery as a string.
     */
    @Override
    public String toString()
    {
        String s="";
        
        //The command is always the first bit of data sent.
        s+=command;
        
        for(String str:args)
        {
            s+=" "+str;
        }
        
        return s;
    }
    
    /**
     * Generates a ManagementQuery from the given string.
     * @param s
     * @return 
     */
    public ManagementQuery fromString(String s)
    {
        Scanner strScanner=new Scanner(s);
        
        command=strScanner.next();
        
        while(strScanner.hasNext())
        {
            args.add(strScanner.next());
        }
        
        return this;
    }
}
