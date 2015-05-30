/*
 * This contains the server side of all response handling.
 * It is its own server because the infastructure implemented for the snippet protocol is already
 * fairly complex on its own. Also, the Response Protocol requires its own distinct set of configuration and other such
 * handling, so it is logically distinct.
 */

package experimentalserverservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ResponseProtocolServer
{
    private static final String RESPROTSRV_TAG="ResponseProtocolServer";
    private Logger l;
    private String responseDir;
    
    public ResponseProtocolServer(Logger newLogger)
    {
        
    }
    
    /**
     * Reads until it encounters a </nugget> tag.
     * @param r
     * @return 
     */
    private String readAllXML(BufferedReader r) throws IOException
    {
        String s="";
        String temp;
        
        do
        {
            temp=r.readLine();
            s+=temp;
        } while(!temp.equals("</nugget>"));
        
        return s;
    }
    
    private File getCurFile(String userId)
    {
        int curVal;
        File counterDir=new File(responseDir+"/+userId");
        File counterFile=new File(responseDir+"/"+userId+"/counter.file");
        File returnFile=null;
        
        if(!counterDir.exists())
        {
            counterDir.mkdir();
        }
        
        if(!counterFile.exists())
        {
            try
            {
                counterFile.createNewFile();
                
                PrintWriter out=new PrintWriter(counterFile);
                out.print("0");
            } catch(IOException e)
            {
                l.logErr(RESPROTSRV_TAG, "Couldn't save ");
            }
        }
        
        try
        {
            Scanner s=new Scanner(counterFile);
            curVal=Integer.parseInt(s.next());
            
            
            returnFile=new File(responseDir+"/"+userId+"/"+curVal+".xml");
            returnFile.createNewFile();
            
            s.close();
            counterFile.delete();
            counterFile.createNewFile();
            
            PrintWriter out=new PrintWriter(counterFile);
            curVal++;
            out.print(curVal);
            
            out.flush();
            out.close();
        } catch(FileNotFoundException e)
        {
            //This should probably never happen.
        } catch(IOException e)
        {
            
        }
        
        return returnFile;
    }
    
    public void serve(PrintWriter out, BufferedReader in)
    {
        PrintWriter fileOutput;
        String response;
        String userId;
        File outFile;
        
        try
        {
            //There is only one connection type: RESPOND. 
            in.readLine();
            userId=in.readLine(); //This is all that's necessary.
            
            //Read the XML response nugget:
            response=readAllXML(in);
            
            //Now record the response somewhere.
            outFile=getCurFile(userId);
            
            fileOutput=new PrintWriter(outFile);
            fileOutput.println(response);
        } catch(IOException e)
        {
            l.logErr(RESPROTSRV_TAG, "IOException while recording response!");
        }
    }
}
