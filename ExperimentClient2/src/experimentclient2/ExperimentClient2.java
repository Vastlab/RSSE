/*
 * Due to the changes implemented in the new Experimental Server Service,
 * a new Experiment client needed to be written to not only handle delta support but to be able
 * to report results back to the server.
 */

package experimentclient2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ExperimentClient2
{
    public static String configFileName=null;
    public static String serverName=null;
    public static int serverPort=9002;
    public static String experimentName=null;
    
    public static final int FETCHALL=1;
    public static final int FETCHNEXT=2;
    public static final int DELTA=3;
    public static final int LISTEXPERIMENTS=4;
    public static final int REWIND=5;
    public static int actionType;
    public static boolean writeConfig=false;
    
    public static Logger l;
    
    public static boolean checkLength(int v, int l)
    {
        return v<l;
    }
    
    public static void printUsage()
    {
        PrintStream o=System.out;
        
        o.println("USAGE: java -jar ExperimentClient2.jar [configfile] [args]");
        o.println("Connect to and fetch data lists from an experiment server.");
        o.println("\tconfigfile\tA file containing the server, port, and experiment to work with.");
        o.println("\t-h, --help\tPrint this message");
        o.println("\t-s\tSpecifies the server to connect to.");
        o.println("\t-p\tSpecifies the port to connect to.");
        o.println("\t-e\tSpecifies the name of the experiment to work with.");
        o.println("\t-n, --next\tGets the next item from the set. Prints NONE if there is nothing left.");
        o.println("\t-o\tWrite the current configuration to a file.");
        o.println("\t-f\tFetch a list of (all) URLs.");
        o.println("\t-d\tCalculate the differences between a list of URLs and the current server.");
        o.println("\t-l\tList all experiments available on the server.");
        o.println("\t-r\tReset the current URL index to 0.");
    }
    
    public static void interpretArgs(String[] args)
    {
        writeConfig=false;
        
        for(int i=0;i<args.length;i++)
        {
            if(args[i].equals("-h")||args[i].equals("--help"))
            {
                printUsage();
                System.exit(1);
            }
            
            else if(args[i].equals("-s"))
            {
                if(checkLength(i+1, args.length))
                {
                    serverName=args[i+1];
                    
                    i++;
                }
                
                else
                {
                    System.err.println("ERROR: Didn't specify a server to connect to!");
                    System.exit(2);
                }
            }
            
            else if(args[i].equals("-p"))
            {
                if(checkLength(i+1, args.length))
                {
                    serverPort=Integer.parseInt(args[i+1]);
                    i++;
                }
                
                else
                {
                    System.err.println("ERROR: Didn't specify a port to connect to!");
                    System.exit(2);
                }
            }
            
            else if(args[i].equals("-e"))
            {
                if(checkLength(i+1, args.length))
                {
                    experimentName=args[i+1];
                    i++;
                }
                
                else
                {
                    System.err.println("ERROR: Didn't specify an experiment!");
                    System.exit(2);
                }
            }
            
            else if(args[i].equals("-n")||args[i].equals("--next"))
            {
                actionType=FETCHNEXT;
            }
            
            else if(args[i].equals("-o"))
            {
                if(checkLength(i+1, args.length))
                {
                    writeConfig=true;
                }
                
                else
                {
                    System.err.println("ERROR: Didn't specify a file to write to.");
                    System.exit(2);
                }
            }
            
            else if(args[i].equals("-f"))
            {
                actionType=FETCHALL;
            }
            
            else if(args[i].equals("-d"))
            {
                actionType=DELTA;
            }
            
            else if(args[i].equals("-l"))
            {
                actionType=LISTEXPERIMENTS;
            }
            
            else if(args[i].equals("-r"))
            {
                actionType=REWIND;
            }
            
            else //Assume that this is the experiment configuration file.
            {
                if(args[i].charAt(0)=='-')
                {
                    System.err.println("ERROR: Unrecognized command: "+args[i]);
                }
                
                else
                {
                    configFileName=args[i];
                }
            }
        }
    }
    
    public static void writeConfig()
    {
        if(configFileName==null)
        {
            printUsage();
            System.exit(1);
        }
        
        try
        {
            PrintWriter pw=new PrintWriter(new File(configFileName));
            
            pw.println("experimentName: "+experimentName);
            pw.println("serverName: "+serverName);
            pw.println("serverPort: "+serverPort);
            pw.println("accountingFile: "+experimentName+".digest");
            
            pw.flush();
            pw.close();
        } catch(IOException e)
        {
            System.err.println("ERROR: Couldn't write to configuration file!");
        }
    }
    
    public static void readConfig()
    {
        try
        {
            Scanner s=new Scanner(new File(configFileName));
            Scanner lineScanner;
            String buf;

            while(s.hasNextLine())
            {
                buf=s.nextLine();
                lineScanner=new Scanner(buf);

                switch(lineScanner.next())
                {
                    case "experimentName:":
                        experimentName=lineScanner.next();
                        break;
                    case "serverName:":
                        System.out.println("Loading experiment name.");
                        serverName=lineScanner.next();
                        break;
                    case "serverPort:":
                        serverPort=Integer.parseInt(lineScanner.next());
                        break;
                    case "accountingFile":
                        lineScanner.next();
                        break;
                }
            }
        } catch(FileNotFoundException e)
        {
            
        }
    }
    
    public static void getAccountingData(File f)
    {
        try
        {
            ESSProtocolClient c=new ESSProtocolClient(l);
            PrintWriter pw=new PrintWriter(f);
            Experiment e=c.getData(serverName, serverPort, experimentName);
            
            if(e==null)
            {
                System.err.println("ERROR: Unable to connect to server and get data list!");
                System.exit(3);
            }
            
            pw.println("curelement: 0");
            
            e.dumpToStream(pw);
            
            pw.flush();
            pw.close();
        } catch(IOException e)
        {
            System.err.println("ERROR: Unable to connect to server and get data list. IOExceptions are fun.");
        }
    }
    
    public static void doFetchAll(File f, File accountingFile)
    {
        //See if we can contact the server:
        try
        {
            ESSProtocolClient c=new ESSProtocolClient(l);
            PrintWriter pw=new PrintWriter(f);
            
            Experiment e=c.getData(serverName, serverPort, experimentName);
            
            //If we couldn't fetch anything over the network, use the cached experiment.
            if(e==null)
            {
                System.err.println("WARNING: Unable to contact the server. Using cached experiment.");
                
                FileInputStream rawIn=new FileInputStream(accountingFile);
                Scanner s=new Scanner(rawIn);
                s.nextLine(); //Read past the first line of the new file.
                
                e=new Experiment(l);
                e.readFromStream(new BufferedReader(new InputStreamReader(rawIn)));
                
                rawIn.close();
            }
            
            for(DataElement elem:e.dataList)
            {
                pw.println(elem.URL);
                //pw.println(elem);
            }
            
            pw.flush();
            pw.close();
            System.out.println("URL list written to "+f.getName());
            
        } catch(IOException e)
        {
            System.err.println("Couldn't generate URL file.");
        }
    }
    
    public static void doFetchNext(File accountingFile)
    {
        //System.out.println("Accounting file: "+accountingFile);
        try
        {
            int index;
            Experiment e;
            FileInputStream rawIn=new FileInputStream(accountingFile);
            BufferedReader lineReader=new BufferedReader(new InputStreamReader(rawIn));
            //Scanner s=new Scanner(rawIn);
            Scanner subScanner=new Scanner(lineReader.readLine()); //Read past the first line of the new file.
            
            subScanner.next(); //Remove the name of the part to read.
            index=subScanner.nextInt();

            e=new Experiment(l);
            e.readFromStream(lineReader);//(new BufferedReader(new InputStreamReader(rawIn)));
            
            rawIn.close();
            
            if(index>=e.dataList.size())
            {
                System.out.println("NONE");
            }
            
            else
            {
                System.out.println(e.dataList.get(index).URL);
                
                accountingFile.delete();
            
                PrintWriter pw=new PrintWriter(accountingFile);
                pw.println("curelement: "+(index+1));
                e.dumpToStream(pw);

                pw.flush();
                pw.close();
            }
        } catch(IOException e)
        {
            System.err.println("Couldn't generate URL file.");
        }
    }
    
    /**
     * Resets the current URL index in the accounting file.
     * @param accountingFile 
     */
    public static void doRewind(File accountingFile)
    {
        try
        {
            //Ensure that we don't crash:
            if(!accountingFile.exists())
            {
                return;
            }
            
            //First, we need to read the current file:
            ArrayList<String> fileContents=new ArrayList<String>();
            Scanner s=new Scanner(accountingFile);
            
            s.nextLine(); //Delete the index.
            
            while(s.hasNextLine())
            {
                fileContents.add(s.nextLine());
            }
            
            s.close();
            accountingFile.delete();
            
            PrintWriter pw=new PrintWriter(accountingFile);
            pw.println("curelement: 0");
            
            //Now write back the file:
            for(String str:fileContents)
            {
                pw.println(str);
            }
            
            pw.flush();
            pw.close();
        } catch(IOException e)
        {
            System.err.println("Couldn't generate URL file.");
        }
    }
    
    public static void doResolve(File accountingFile)
    {
        try
        {
            int index;
            Experiment e;
            FileInputStream rawIn=new FileInputStream(accountingFile);
            //Scanner s=new Scanner(rawIn);
            BufferedReader lineReader=new BufferedReader(new InputStreamReader(rawIn));
            Scanner subScanner=new Scanner(lineReader.readLine()); //Read past the first line of the new file.
            
            subScanner.next(); //Remove the name of the part to read.
            index=subScanner.nextInt();

            e=new Experiment(l);
            e.readFromStream(lineReader);//new BufferedReader(new InputStreamReader(rawIn)));
            
            System.out.println(e.dataList.get(index).URL);
            
            rawIn.close();
                
        } catch(IOException e)
        {
            System.err.println("Couldn't generate URL file.");
        }
    }
    
    public static void doListExperiments()
    {
        try
        {
            ESSProtocolClient c=new ESSProtocolClient(l);
            ArrayList<String> exps=c.getExperiments(serverName, serverPort);
            
            for(String s:exps)
            {
                System.out.println(s);
            }
            
        } catch(IOException e)
        {
            System.err.println("ERROR: Couldn't connect to experiment server to get experiments!");
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if(args.length==0)
        {
            printUsage();
            return;
        }
        
        l=new DefaultStreamLogger();
        actionType=-1; //All action types are positive.
        interpretArgs(args);
        
        System.out.println(configFileName);
        if(configFileName!=null)
        {
            File f=new File(configFileName);
            
            if(f.exists()&&!writeConfig)
            {
                readConfig();
            }
        }
        
        File accountingFile=new File(experimentName+".digest");
        
        //Filter out the experiment list action type now:
        if(actionType==LISTEXPERIMENTS)
        {
            doListExperiments();
            return;
        }
        
        if(writeConfig)
        {
            writeConfig();
        }
        
        if(!accountingFile.exists())
        {
            System.out.println("Getting accounting data from server...");
            getAccountingData(accountingFile);
        }
        
        switch(actionType)
        {
            case FETCHALL: //In this case, try to contact the server, then fall back:
                doFetchAll(new File(experimentName+".urls"), accountingFile);
                break;
            case FETCHNEXT:
                doFetchNext(accountingFile);
                break;
            case DELTA:
                doResolve(accountingFile);
                break;
            case REWIND:
                doRewind(accountingFile);
                break;
                
        }
    }
    
}
