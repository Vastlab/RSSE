/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentalserverservice;

import java.net.ServerSocket;

/**
 *
 * @author mgohde
 */
public class ResponseServer
{
    private ServerSocket sock;
    private Logger l;
    
    private Thread listenerThread;
    
    public ResponseServer(int portToUse, String respDir)
    {
        
    }
    
    private class ListenerRunnable implements Runnable
    {
        @Override
        public void run()
        {
            
        }
    }
}
