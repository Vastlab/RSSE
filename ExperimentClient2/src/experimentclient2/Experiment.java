/*
 * This is a smarter, cleaner implementation of experiments as defined in the original ESS
 * It provides a complete set of information for an experiment and internally handles all
 * updates. 
 */

package experimentclient2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author mgohde
 */
public class Experiment
{
    public String experimentName;
    //private ArrayList<Integer> clientList;
    public ArrayList<DataElement> dataList;
    private ClientBST clientStructure;
    private boolean initialized;
    private String responseServer;
    private int responseServerPort;
    private Logger l;
    
    /**
     * Default constructor. Should be used with readFromDigest().
     */
    public Experiment(Logger l)
    {
        clientStructure=new ClientBST();
        initialized=false;
        experimentName=null;
        dataList=new ArrayList<DataElement>();
    }
    
    /**
     * Constructor for importing new files. 
     * @param newFile 
     */
    public Experiment(File newFile, Logger l) throws IOException
    {
        this.l=l;
        
        try
        {
            Parser p=new Parser(newFile, l);
            Experiment tmp=p.getData();
            
            this.experimentName=tmp.experimentName;
            this.responseServer=tmp.responseServer;
            this.responseServerPort=tmp.responseServerPort;
            
            this.dataList=tmp.dataList;
        } catch(SAXException e)
        {
            //Do nothing
        } catch(ParserConfigurationException e)
        {
            //Aaaand do nothing.
        }
        clientStructure=new ClientBST();
        initialized=true;
    }
    
    /**
     * Adds a data element to the set.
     * @param de 
     */
    public void addData(DataElement de)
    {
        dataList.add(de);
    }
    
    /**
     * Removes a data element from the set.
     * @param de 
     */
    public void removeData(DataElement de)
    {
        
    }
    
    /**
     * Removes a data element from the set.
     * @param dataURL 
     */
    public void removeData(String dataURL)
    {
        for(int i=0;i<dataList.size();i++)
        {
            if(dataList.get(i).URL.equals(dataURL))
            {
                dataList.remove(i);
                return;
            }
        }
    }
    
    /**
     * This generates an XML copy of the experiment.
     * @param pw PrintWriter to print output to.
     */
    public void writeToXML(PrintWriter pw)
    {
        pw.println("<rsse>");
        pw.println("\t<experiment>");
        pw.println("\t\t<title>"+experimentName+"</title>");
        
        if(responseServer!=null)
        {
            pw.println("\t\t<resserver>"+responseServer+"</resserver>");
            pw.println("\t\t<resport>"+responseServerPort+"</resport>");
        }
        
        //Now add all data:
        for(DataElement e:dataList)
        {
            pw.println("\t\t<data>");
            pw.println("\t\t\t<url>"+e.URL+"</url>");
            pw.println("\t\t</data>");
        }
        
        pw.println("\t</experiment>");
        pw.println("</rsse>");
    }
    
    /**
     * 
     * @param f
     * @throws FileNotFoundException 
     */
    public void writeToDigest(File f) throws FileNotFoundException
    {
        PrintWriter fileWriter=new PrintWriter(f);
        
        dumpToStream(fileWriter);
        
        fileWriter.flush();
        fileWriter.close();
    }
    
    public void readFromFile(File f) throws FileNotFoundException, IOException
    {
        InputStream is=new FileInputStream(f);
        
        if(!readFromStream(new BufferedReader(new InputStreamReader(is))))
        {
            throw new FileNotFoundException();
        }
        
        is.close();
    }
    
    /**
     * Reads the contents of an experiment from a stream.
     * The input should be formatted as an RSSE Digest file.
     * @param is 
     * @return whether or not the data could be read.
     */
    public boolean readFromStream(BufferedReader is)
    {
        //Scanner s=new Scanner(is);
        Scanner lineScanner;
        String buf;
        String line;
        
        try
        {
            buf=is.readLine();

            if(!buf.equalsIgnoreCase("RSSE DIGEST"))
            {
                return false;
            }

            while(true)//s.hasNextLine())
            {
                line=is.readLine();
                //System.out.println(line);
                
                //This means that the connection has been terminated.
                if(line==null)
                {
                    break;
                }
                
                lineScanner=new Scanner(line);

                buf=lineScanner.next();
                System.out.println("Buffer contents: "+buf);

                if(buf.equals("title:"))
                {
                    this.experimentName=lineScanner.next();
                }

                else if(buf.equals("shouldrespond:"))
                {
                    //Ignore it.
                }

                else if(buf.equals("responseserver:"))
                {
                    responseServer=lineScanner.next();
                }

                else if(buf.equals("responseport: "))
                {
                    responseServerPort=Integer.parseInt(lineScanner.next());
                }

                else if(buf.equals("numelements:"))
                {
                    //Ignore this as well.
                }

                else if(buf.equals("url:"))
                {
                    DataElement elem=new DataElement();
                    elem.URL=lineScanner.next();
                    dataList.add(elem);
                }

                else if(buf.equals("END"))
                {
                    break;
                }
            }
        
            return true;
        } catch(IOException e)
        {
            return false;
        }
    }
    
    /**
     * Writes the contents of the experiment to a stream.
     * The output is formatted as an RSSE Digest file.
     * @param pw 
     */
    public void dumpToStream(PrintWriter pw)
    {
        pw.println("RSSE DIGEST");
        pw.print("title: ");
        pw.println(experimentName);
        pw.println("shouldrespond: "+responseServer!=null);
        
        if(responseServer!=null)
        {
            pw.println("responseserver: "+responseServer);
            pw.println("responseport: "+responseServerPort);
        }
        
        pw.println("numelements: "+dataList.size());
        
        for(DataElement e:dataList)
        {
            pw.println("url: "+e.URL);
        }
        
        pw.println("END RSSE DIGEST");
    }
    
    @Override
    public Experiment clone()
    {
        try
        {
            return (Experiment) super.clone();
        } catch(CloneNotSupportedException e)
        {
            return null;
        }
    }
}
