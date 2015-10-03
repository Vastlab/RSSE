/*
 * This class manages and maintains the ExperimentalServerService internally.
 * As such, its functionality takes over from the main method of the ExperimentalServerService class.
 *
 * Its implementation will be far cleaner than the sloppy remote CLI implemented in the CacheModule.
 * As such, it will internally accept and queue ManagementRequests that are formatted and interpreted by some
 * remote bit of code.
 */

package experimentalserverservice;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ManagementServer
{
    public static final String MANAGEMENT_SERVER_TAG="ManagementServer";
    private Thread executorThread; //This thread serially executes connections.
    private Thread connectionAcceptThread; //This thread queues connections for the executor.
    private ArrayList<Connection> connectionQueue;
    private ServerSocket srvrSock;
    private Logger l;
    
    private boolean runThreads;
    
    public ManagementServer(int port, Logger newL) throws IOException
    {
        l=newL;
        connectionQueue=new ArrayList<Connection>();
        srvrSock=new ServerSocket(port);
        
        executorThread=new Thread(new ConnectionExecutor());
        connectionAcceptThread=new Thread(new ConnectionListener());
        runThreads=true;
    }
    
    public void start()
    {
        l.logMsg(MANAGEMENT_SERVER_TAG, "Starting management server...");
        executorThread.start();
        connectionAcceptThread.start();
    }
    
    public void stop()
    {
        l.logMsg(MANAGEMENT_SERVER_TAG, "Stopping management server...");
    }
    
    //This function wraps insert() and remove() such that no threads accessing the queue may step on each other.
    private synchronized Connection alterQueue(Connection c)
    {
        //This is the remove operation
        if(c==null)
        {
            if(connectionQueue.isEmpty())
            {
                return null;
            }
            
            else
            {
                return connectionQueue.remove(0);
            }
        }
        
        //Insert op.
        else
        {
            connectionQueue.add(c);
            return null;
        }
    }
    
    public boolean init(String args) //Does initialization (used to re-implement the ExperimentalServerService.main() method).
    {
        if(ExperimentalServerService.initialized)
        {
            return false;
        }

        else
        {
            //Init the client-facing servers:
            ExperimentalServerService.respSrv=new ResponseServer(Settings.responseServerPort, Settings.respDir, l);
            ExperimentalServerService.experimentFile=new File(Settings.experimentFile);

            //Check if there is actually an experiment definition file somewhere
            if(ExperimentalServerService.experimentFile.exists())
            {
                //Now try to read or generate digests:
                File digestList=new File(ExperimentalServerService.experimentFile.getName()+".list");
                FileDeltaTool tool=new FileDeltaTool(l);
                Parser p=new Parser(l);

                if(digestList.exists())
                {
                    ExperimentalServerService.experimentList=tool.loadFromDigests(digestList);
                }

                else
                {
                    System.out.printf("Experiment file %s doesn't have a digest list. Generating...\n", ExperimentalServerService.experimentFile.getName());
                    ExperimentalServerService.experimentList=p.parseExperimentFile(ExperimentalServerService.experimentFile);

                    //Generate ids for all of the data.
                    for(Experiment e:ExperimentalServerService.experimentList)
                    {
                        e.id();
                    }

                    tool.saveDigests(digestList, ExperimentalServerService.experimentList);
                }

                if(ExperimentalServerService.experimentList.isEmpty())
                {
                    l.logErr(ExperimentalServerService.ESS_TAG, "Error: No experiments defined!");
                }
            }

            else
            {
                l.logErr(ExperimentalServerService.ESS_TAG, "Error: Experiment definition file not found!");
                System.exit(1);
            }

            ExperimentalServerService.dbSnapshotFile=new File(Settings.dbDir+"/snapshot.file");
            ExperimentalServerService.loadDb(ExperimentalServerService.dbSnapshotFile);
            ExperimentalServerService.snippetSrv=new SnippetServer(Settings.nuggetServerPort, ExperimentalServerService.db, ExperimentalServerService.experimentList, l);

            ExperimentalServerService.saver=new ClientDBSaver(ExperimentalServerService.db, ExperimentalServerService.dbSnapshotFile, l, ExperimentalServerService.DB_SAVE_INTERVAL);
            ExperimentalServerService.fileUpdSrv=new FileUpdateServer(l, ExperimentalServerService.db);

            //Now that everything is ready, start all services:
            ExperimentalServerService.saver.start();
            ExperimentalServerService.respSrv.startServer();
            ExperimentalServerService.snippetSrv.startServer();
            ExperimentalServerService.fileUpdSrv.start();

            return true;
        }
    }
    
    public class ConnectionExecutor implements Runnable
    {
        public boolean mergeNew(String args)
        {
            Scanner argScanner=new Scanner(args);
            FileDeltaTool tool=new FileDeltaTool(l);
            ArrayList<Experiment> newExpList;
            ArrayList<Delta> deltaList;
            Experiment expToMerge, oldExp;
            String expName;
            File newFile;
            Parser p;
            
            newFile=new File(argScanner.next());
            
            expName="";
            
            while(argScanner.hasNext())
            {
                expName+=argScanner.next();
            }
            
            if(!newFile.exists()||expName.equals(""))
            {
                return false;
            }
            
            p=new Parser(l);
            
            newExpList=p.parseExperimentFile(newFile);
            
            //Get our experiment from the list:
            expToMerge=null;
            for(Experiment e:newExpList)
            {
                if(e.name.equals(expName))
                {
                    expToMerge=e;
                    break;
                }
            }
            
            if(expToMerge==null)
            {
                return false;
            }
            
            oldExp=null;
            for(Experiment e:ExperimentalServerService.experimentList)
            {
                if(e.name.equals(expName))
                {
                    oldExp=e;
                    break;
                }
            }
            
            if(oldExp==null)
            {
                return false;
            }
            
            deltaList=tool.compareExperiments(oldExp, expToMerge);
            
            tool.mergeWithClientDb(oldExp, deltaList, ExperimentalServerService.db);
            
            return true;
        }
        
        public boolean reloadConf(String args)
        {
            if(args.equals(""))
            {
                //...let's just not implement this for now.
            }
            
            return false;
        }
        
        public boolean standby(String args)
        {
            ExperimentalServerService.snippetSrv.stopServer();
            ExperimentalServerService.respSrv.stopServer();
            ExperimentalServerService.saver.stop();
            
            return true;
        }
        
        public void stop(String args)
        {
            ExperimentalServerService.snippetSrv.stopServer();
            ExperimentalServerService.respSrv.stopServer();
            ExperimentalServerService.fileUpdSrv.stop();
            ExperimentalServerService.saver.stop();
        }
        
        public boolean restart(String args)
        {
            ExperimentalServerService.saver.start();
            ExperimentalServerService.respSrv.startServer();
            ExperimentalServerService.snippetSrv.startServer();
            
            return true;
        }
        
        
        
        public void exec(ManagementRequest mreq, Connection c)
        {
            boolean result;
            
            switch(mreq.reqType)
            {
                case ManagementRequest.INIT:
                    result=init(mreq.reqArgs);
                    break;
                    
                case ManagementRequest.MERGE_NEW:
                    result=mergeNew(mreq.reqArgs);
                    break;
                    
                case ManagementRequest.RELOAD_CONF:
                    result=reloadConf(mreq.reqArgs);
                    break;
                    
                case ManagementRequest.STANDBY:
                    result=standby(mreq.reqArgs);
                    break;
                    
                case ManagementRequest.STOP: //Stop is a special case.
                    stop(mreq.reqArgs);
                    result=false;
                    System.exit(0);
                    break;
                    
                case ManagementRequest.RESTART:
                    result=restart(mreq.reqArgs);
                    break;
                    
                default: //Error condition.
                    result=false;
            }
            
            if(result)
            {
                c.out.println("OK");
            }
            
            else
            {
                c.out.println("ERR");
            }
        }
        
        public void run()
        {
            Connection localConnection;
            
            while(runThreads)
            {
                localConnection=alterQueue(null);
                
                if(localConnection!=null)
                {
                    ManagementRequest mReq;
                    mReq=new ManagementRequest();
                    
                    try
                    {
                        mReq.parse(localConnection.in);
                        exec(mReq, localConnection);
                    } catch(IOException e)
                    {
                        l.logErr(MANAGEMENT_SERVER_TAG, "Error! Couldn't parse management request from stream!");
                    }
                }
            }
        }
    }
    
    private class ConnectionListener implements Runnable
    {
        public void run()
        {
            Socket s;
            
            try
            {
                while((s=srvrSock.accept())!=null&&runThreads)
                {
                    alterQueue(new Connection(s));
                }
            } catch(IOException e)
            {
                return;
            }
        }
        
        public ConnectionListener()
        {
            
        }
    }
}
