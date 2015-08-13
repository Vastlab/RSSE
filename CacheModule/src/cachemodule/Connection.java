/*
 * This object is used by the ManagementServer to encapsulate the guts of a network
 * connection.
 */

package cachemodule;

import java.io.BufferedReader;
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
    
    public Connection(Socket s)
    {
        
    }
}
