/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentalserverservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 *
 * @author mgohde
 */
public class ClientDBSaver
{
    private ClientDB internalDB;
    private File internalFile;
    private Logger l;
    private long sleepInterval;
    private Thread dbSaverThread;
    
    public ClientDBSaver(ClientDB newDB, File fileToUse, Logger newLogger, long newSleepInterval)
    {
        internalDB=newDB;
        internalFile=fileToUse;
        l=newLogger;
        sleepInterval=newSleepInterval;
    }
    
    public void start()
    {
        dbSaverThread=new Thread(new DBSaverRunnable());
        dbSaverThread.start();
    }
    
    public void stop()
    {
        dbSaverThread.interrupt();
    }
    
    public class DBSaverRunnable implements Runnable
    {
        private static final String DBSAVER_TAG="DBSaver";
        public void save()
        {
            if(internalFile.exists())
            try
            {
                PrintWriter outWriter=new PrintWriter(internalFile);
                
                internalDB.dump(outWriter);
                outWriter.flush();
                outWriter.close();
            } catch(FileNotFoundException e)
            {
                l.logErr(DBSAVER_TAG, "Couldn't save database to: "+internalFile);
            }
        }
        
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    Thread.sleep(sleepInterval);
                    save();
                } catch(InterruptedException e)
                {
                    l.logMsg(DBSAVER_TAG, "Got signal to exit. Stopping...");
                    return;
                }
            }
        }
    }
}
