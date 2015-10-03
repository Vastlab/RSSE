/*
 * This object is used by the ManagementServer to encapsulate the guts of a network
 * connection.
 */

package experimentalserverservice2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
    public InputStream rawIn;
    public Socket sock;
    private boolean isOpen;
    
    public Connection(Socket s) throws IOException
    {
        out=new PrintWriter(s.getOutputStream(), true);
        rawIn=s.getInputStream();
        in=new BufferedReader(new InputStreamReader(s.getInputStream()));
        sock=s;
        isOpen=true; //If we've made it to this point without returning for an exception.
    }
    
    public void close()
    {
        try
        {
            out.flush();
            out.close();
            in.close();
            sock.close();
            isOpen=false;
        } catch(IOException e)
        {
            //Do nothing.
        }
    }
    
    public boolean isOpen()
    {
        return isOpen;
    }
}
