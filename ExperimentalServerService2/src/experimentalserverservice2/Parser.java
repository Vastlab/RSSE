/*
 * Re-implementation of the RSSE parser. 
 */

package experimentalserverservice2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author mgohde
 */
public class Parser
{
    private static final String PARSER_TAG="Parser";
    private Logger l;
    private Experiment e;
    
    private static Experiment internalparse(String s, Logger l) throws ParserConfigurationException, SAXException, IOException
    {
        Experiment e=new Experiment(l);
        
        //Does anyone else think that the amount of stuff necessary to get XML up and running in Java is 
        //just a little absurd?
        DocumentBuilderFactory docBuildFac=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder=docBuildFac.newDocumentBuilder();
        Document d=builder.parse(s);
        
        Element topElement=d.getDocumentElement(); //Get the top level element, in this case <rsse>
        
        if(!topElement.getNodeName().equals("rsse"))
        {
            l.logErr(PARSER_TAG, "RSSE opening tag missing or invalid!");
            return null;
        }
        
        Node n=topElement.getChildNodes().item(0);
        
        if(n==null)
        {
            l.logErr(PARSER_TAG, "Couldn't parse any experiments from the file.");
            return null;
        }
        
        NodeList subList=n.getChildNodes();
        
        String resServer=null;
        int resPort=0;
        
        //The "Experiment" tag is now obsolete as one file ay describe only one experiment.
        for(int i=0;i<subList.getLength();i++)
        {
            Node tmp=subList.item(i);
            
            if(tmp.getNodeName().equals("title"))
            {
                e.experimentName=tmp.getTextContent();
            }
            
            else if(tmp.getNodeName().equals("description"))
            {
                //Ignore it.
            }
            
            else if(tmp.getNodeName().equals("data"))
            {
                NodeList dataList=tmp.getChildNodes();
                
                for(int j=0;j<dataList.getLength();j++)
                {
                    Node data=dataList.item(j);
                    
                    if(data.getNodeName().equals("url"))
                    {
                        DataElement elem=new DataElement(data.getTextContent(), resServer==null, resServer, resPort);
                        
                        e.addData(elem);
                    }
                }
            }
            
            else if(tmp.getNodeName().equals("resserver"))
            {
                resServer=tmp.getNodeValue();
            }
            
            else if(tmp.getNodeName().equals("resport"))
            {
                resPort=Integer.parseInt(tmp.getNodeValue());
            }
        }
        
        return e;
    }
    
    private static String readFile(BufferedReader r) throws IOException
    {
        String retStr="";
        int v;
        
        while((v=r.read())!=-1)
        {
            retStr+=(char)v;
        }
        
        return retStr;
    }
    
    private static String readFile(File f) throws FileNotFoundException, IOException
    {
        BufferedReader r=new BufferedReader(new FileReader(f));
        
        return readFile(r);
    }
    
    public Experiment getData()
    {
        return e;
    }
    
    public Parser(File f, Logger l) throws IOException, ParserConfigurationException, SAXException
    {
        this.l=l;
        
        this.e=internalparse(readFile(f), l);
    }
    
    public Parser(BufferedReader r, Logger l) throws IOException, ParserConfigurationException, SAXException
    {
        this.l=l;
        
        this.e=internalparse(readFile(r), l);
    }
    
    public Parser(String s, Logger l)
    {
        this.l=l;
    }
    
    public void digest(File outputFile) throws FileNotFoundException
    {
        e.writeToDigest(outputFile);
    }
}
