/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentalserverservice;

import java.io.File;

/**
 *
 * @author mgohde
 */
public class ClientDBSaver
{
    private ClientDB internalDB;
    private File internalFile;
    
    public ClientDBSaver(ClientDB newDB, File fileToUse)
    {
        internalDB=newDB;
        internalFile=fileToUse;
    }
    
    public class DBSaverRunnable implements Runnable
    {
        public void save()
        {
            
        }
        
        @Override
        public void run()
        {
            
        }
    }
}
