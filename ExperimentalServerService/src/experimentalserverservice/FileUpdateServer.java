/*
 * This server implements a very simple protocol (simple enough that it won't have corresponding client and server
 * protocol classes) intended to allow new experiments to be merged into the client database.
 * This class also contains the functions necessary to merge the data itself so that 
 */

package experimentalserverservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author mgohde
 */
public class FileUpdateServer
{
    private static final String UPDSRV_TAG="FileUpdateServer";
    private Logger l;
    private File secret;
    private String secretStr;
    private ClientDB db;
    private Thread updateThread;
    private ServerSocket updateThreadSocket;
    int i;
    
    public FileUpdateServer(Logger newL, ClientDB newdb)
    {
        l=newL;
        db=newdb;
        
        secret=new File("secret.file");
        
        if(!secret.exists())
        {
            Random rand=new Random();
            secretStr="";
            
            try
            {
                PrintWriter pw=new PrintWriter(secret);
                
                //Generate a random "password" string.
                //This is intended to ensure that random people on the Internet can't just
                //change someone's experiments. No serious security is ensured or expected.
                for(i=0;i<10;i++)
                {
                    secretStr+=rand.nextInt(9);
                }
                
                pw.println(secretStr);
            } catch(FileNotFoundException e)
            {
                //Fill the random password string but print an error to the user:
                
                for(i=0;i<10;i++)
                {
                    secretStr+=rand.nextInt(9);
                }
                
                l.logErr(UPDSRV_TAG, "Warning: wouldn't save secret file. Password for this session="+secretStr);
            }
        }
        
        updateThread=null;
    }
    
    /**
     * Starts the FileUpdate service.
     */
    public void start()
    {
        if(updateThread!=null)
        {
            //Only do something if it's thoroughly dead:
            if(!updateThread.isAlive())
            {
                updateThread=new Thread(new FileUpdateRunnable(Settings.fileUpdateServerPort));
                updateThread.start();
            }
        }
    }
    
    /**
     * Stops the FileUpdate service.
     */
    public void stop()
    {
        try
        {
            updateThreadSocket.close(); //This should probably get the thread's attention.
            updateThread.interrupt();
        } catch(IOException e)
        {
            //Once again, this shouldn't really be a problem.
        }
    }
    
    /**
     * This function generates a backup filename so that each set of changes can be backed up.
     * 
     * @return 
     */
    public File getBackupFile(File sourceFile)
    {
        File backupFile;
        int bakNum=1;
        int i;
        
        backupFile=new File(sourceFile.getName()+".bak"+bakNum);
        
        while(backupFile.exists())
        {
            bakNum++;
            backupFile=new File(sourceFile.getName()+".bak"+bakNum);
        }
        
        return backupFile;
    }
    
    /**
     * This function merges a new experiment definition file into the existing set.
     * Each connection to the FileUpdateServer involves the transfer of a new RSSE experiment definitions file.
     * This file can be merged into the existing set using this function.
     * @param dump
     * @param existing 
     */
    public void merge(File dump, File existing)//, File listFile)
    {
        Parser p=new Parser(l);
        FileDeltaTool tool=new FileDeltaTool(l);
        ArrayList<Experiment> dumpList;
        ArrayList<Experiment> existingList;
        ArrayList<Delta> deltaList;
        
        
        
        dumpList=p.parseExperimentFile(dump);
        existingList=p.parseExperimentFile(existing);
        
        for(Experiment e:dumpList)
        {
            for(Experiment ex:existingList)
            {
                if(ex.name.equals(e.name))
                {
                    deltaList=tool.compareExperiments(e, ex);
                    tool.mergeWithClientDb(ex, deltaList, db);
                }
            }
        }
    }
    
    //This runnable manages the File Update server thread.
    private class FileUpdateRunnable implements Runnable
    {
        public ServerSocket internalSock;
        
        public void run()
        {
            while(!internalSock.isClosed())
            {
                try
                {
                    Socket s=internalSock.accept();
                    
                    //Handle the connection:
                    BufferedReader r=new BufferedReader(new InputStreamReader(s.getInputStream()));
                    PrintWriter pw=new PrintWriter(s.getOutputStream(), true);
                    
                    //Do some password checking.
                    String comparisonSecret=r.readLine(); 
                    
                    if(!comparisonSecret.equals(secretStr))
                    {
                        pw.println("NOAUTH");
                    }
                    
                    //Dump the sent file to disk...
                    else
                    {
                        String temp;
                        
                        pw.println("AUTH");
                        temp=r.readLine();
                        
                        File outFile=new File(temp);
                        File oldFile=null;
                        
                        if(outFile.exists())
                        {
                            outFile.renameTo(getBackupFile(outFile));
                            oldFile=outFile;
                            outFile=new File(temp);
                        }
                        
                        PrintWriter fileWriter=new PrintWriter(outFile);
                        
                        temp=r.readLine();
                        
                        while(!temp.equals("ENDFILE"))
                        {
                            fileWriter.println(temp);
                            temp=r.readLine();
                        }
                        
                        fileWriter.flush();
                        fileWriter.close();
                        
                        //Now that the file writer is closed, merge changes:
                        merge(outFile, oldFile);
                        
                    }
                    
                    r.close();
                    pw.close();
                    
                    s.close();
                } catch(IOException e)
                {
                    l.logMsg(UPDSRV_TAG, "Stopping File Update Server...");
                    break;
                }
            }
        }
        
        public FileUpdateRunnable(int port)
        {
            try
            {
                internalSock=new ServerSocket(port);
                updateThreadSocket=internalSock;
            } catch(IOException e)
            {
                l.logErr(UPDSRV_TAG, "Couldn't bind socket: "+port);
            }
        }
    }
}
