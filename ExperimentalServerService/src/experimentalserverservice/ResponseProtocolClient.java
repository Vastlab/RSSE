/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentalserverservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author mgohde
 */
public class ResponseProtocolClient
{
    private static final String RESPROTCLIENT_TAG="ResponseProtocolClient";
    private Logger l;
    
    public ResponseProtocolClient(Logger newLogger)
    {
        l=newLogger;
    }
    
    private String generateXML(DataElement e, String result)
    {
        String s="<nugget>\n";
        
        s+="\t<url>"+e.getUrl()+"</url>\n";
        s+="\t<class>"+e.getCls()+"</class>\n";
        s+="\t<label>"+e.getLabel()+"</label>\n";
        s+="\t<result>"+result+"</result>\n";
        
        s+="</nugget>";
        
        return s;
    }
    
    public void respond(String server, int port, DataElement element, String result, long userId)
    {
        PrintWriter out;
        //BufferedReader in;
        Socket s;
        
        try
        {
            s=new Socket(server, port);
            
            out=new PrintWriter(s.getOutputStream(), true);
            //in=new BufferedReader(new InputStreamReader(s.getInputStream()));
            
            out.println("RESPOND");
            out.println(userId);
            
            out.println(generateXML(element, result));
            
            out.flush();
            out.close();
            //in.close();
        } catch(UnknownHostException e)
        {
            l.logErr(RESPROTCLIENT_TAG, "Unknown host: "+server);
        } catch(IOException e)
        {
            l.logErr(RESPROTCLIENT_TAG, "IOException when sending results to "+server);
        }
    }
}
