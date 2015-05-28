/*
 * This class provides a means by which the Experimental Server Service can parse XML documents.
 * It provides interfaces for both the overall experiment file and any experiment-nuggets that are sent over the network.
 * Functionality for these experiment nuggets will be baked into the client service.
 */

package experimentalserverservice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author mgohde
 */
public class Parser
{
    private static final String PARSER_TAG="XML Parser";
    private Logger l;
    
    public Parser(Logger newLogger)
    {
        l=newLogger;
    }
    
    public ArrayList<Experiment> parseExperimentFile(File f)
    {
        ArrayList<Experiment> returnList=new ArrayList<Experiment>();
        
        DocumentBuilderFactory docFactory=DocumentBuilderFactory.newInstance();
        
        try
        {
            DocumentBuilder builder=docFactory.newDocumentBuilder();
            Document doc=builder.parse(f);
            
            //Get all of the experiments in the file:
            NodeList list=doc.getElementsByTagName("experiment");
            
            for(int i=0;i<list.getLength();i++)
            {
                Experiment e=new Experiment();
                
                //Pick out the nodes:
                Node n=list.item(i);
                NodeList experimentContents=n.getChildNodes();
                
                for(int j=0;j<experimentContents.getLength();j++)
                {
                    Node m=list.item(j);
                
                    if(n.getNodeName().equals("description"))
                    {
                        e.description=m.getNodeValue();
                    }

                    else if(n.getNodeName().equals("title"))
                    {
                        e.name=m.getNodeValue();
                    }

                    else if(n.getNodeName().equals("report"))
                    {
                        e.report=Boolean.parseBoolean(m.getNodeValue());
                    }

                    else if(n.getNodeName().equals("resserver"))
                    {
                        e.resServer=m.getNodeValue();
                    }

                    else if(n.getNodeName().equals("resport"))
                    {
                        e.resPort=Integer.parseInt(m.getNodeValue());
                    }

                    else if(n.getNodeName().equals("data"))
                    {
                        NodeList dataNodes=m.getChildNodes();
                        
                        for(int k=0;k<dataNodes.getLength();k++)
                        {
                            Node dataNode=dataNodes.item(k);
                            
                            if(dataNode.getNodeName().equals("url"))
                            {
                                DataElement de=new DataElement();
                                //Try to get class and label attributes:
                                if(dataNode.getAttributes().getNamedItem("class")!=null)
                                {
                                    de.setClass(dataNode.getAttributes().getNamedItem("class").getNodeValue());
                                }
                                
                                if(dataNode.getAttributes().getNamedItem("label")!=null)
                                {
                                    de.setLabel(Integer.parseInt((dataNode.getAttributes().getNamedItem("class").getNodeValue())));
                                }
                                
                                de.setUrl(dataNode.getNodeValue());
                                
                                e.urlList.add(de);
                            }
                        }
                    }
                }
                
                returnList.add(e);
            }
            
        } catch(ParserConfigurationException e)
        {
            l.logErr(PARSER_TAG, "Parser misconfigured! Can't read RSSE Experiment Definitions file!");
        } catch(SAXException ex)
        {
            l.logErr(PARSER_TAG, "SAXException! Can't read RSSE Experiment Definitions file!");
        } catch (IOException exc)
        {
            l.logErr(PARSER_TAG, "The RSSE Experiment Definitions file likely doesn't exist at the location specified.");
        }
        
        return returnList;
    }
    
    /**
     * Returns a command string for the Experimental Client's CLI.
     * This method is a little convoluted, so please bear with me. The general idea is that the XML nuggets parsed
     * are general enough that they can be reinterpreted as a series of commands for a lower-level interface built into
     * the Experimental Client.
     * This is naturally completely implementation dependent, as it is not expected that other implementers will implement
     * a complete CLI.
     */
    public String parseNuggetForCommands(File f)
    {
        String cmd="";
        
        return cmd;
    }
    
    /**
     * Returns an ArrayList containing all data elements in the specified XML nugget.
     * Please note that it returns an ArrayList in order to provide API compatibility for future expansion.
     * @param f
     * @return 
     */
    public ArrayList<DataElement> parseNuggetForInformation(File f)
    {
        ArrayList<DataElement> information=new ArrayList<DataElement>();
        
        DocumentBuilderFactory docBuildFactory=DocumentBuilderFactory.newInstance();
        
        try
        {
            DocumentBuilder builder=docBuildFactory.newDocumentBuilder();
            Document doc=builder.parse(f);
            
            NodeList list=doc.getChildNodes();
            
            DataElement element=new DataElement();
            
            for(int i=0;i<list.getLength();i++)
            {
                Node node=list.item(i);
                
                if(node.getNodeName().equals("title"))
                {
                    element.setTitle(node.getNodeValue());
                }
                
                else if(node.getNodeName().equals("description"))
                {
                    element.setDescription(node.getNodeValue());
                }
                
                else if(node.getNodeName().equals("class"))
                {
                    element.setClass(node.getNodeValue());
                }
                
                else if(node.getNodeName().equals("url"))
                {
                    element.setUrl(node.getNodeValue());
                }
                
                else if(node.getNodeName().equals("label"))
                {
                    element.setLabel(Integer.parseInt(node.getNodeValue()));
                }
                
                else if(node.getNodeName().equals("resserver"))
                {
                    element.setResServer(node.getNodeValue());
                }
                
                else if(node.getNodeName().equals("resport"))
                {
                    element.setResPort(Integer.parseInt(node.getNodeValue()));
                }
            }
        } catch(ParserConfigurationException e)
        {
            l.logErr(PARSER_TAG, "Couldn't parse nugget due to misconfiguration.");
        } catch(SAXException ex)
        {
            l.logErr(PARSER_TAG, "SAXException while parsing XML nugget.");
        } catch(IOException exc)
        {
            l.logErr(PARSER_TAG, "Couldn't read temporary file containing RSSE nugget!");
        }
        return information;
    }
}
