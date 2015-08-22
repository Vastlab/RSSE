/*
 * This object is used by the ManagementServer to encapsulate the guts of a network
 * connection.
 */

package experimentalserverservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author mgohde
 */
public class Connection
{
    public PrintWriter out;
    public BufferedReader in;
    public Socket sock;
    
    public Connection(Socket s) throws IOException
    {
        out=new PrintWriter(s.getOutputStream(), true);
        in=new BufferedReader(new InputStreamReader(s.getInputStream()));
        sock=s;
    }
    
    public void close()
    {
        try
        {
            out.flush();
            out.close();
            in.close();
            sock.close();
        } catch(IOException e)
        {
            //Do nothing.
        }
    }
}
