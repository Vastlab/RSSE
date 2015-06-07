/*
 * This class provides a means by which the Experimental Server Service can parse XML documents.
 * It provides interfaces for both the overall experiment file and any experiment-nuggets that are sent over the network.
 * Functionality for these experiment nuggets will be baked into the client service.
 */

package experimentalserverservice;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
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
            
            System.out.println("Experiment list length: "+list.getLength());
            
            for(int i=0;i<list.getLength();i++)
            {
                Experiment e=new Experiment();
                
                //Pick out the nodes:
                Node n=list.item(i);
                NodeList experimentContents=n.getChildNodes();
                
                System.out.println("Experiment contents length: "+experimentContents.getLength());
                
                for(int j=0;j<experimentContents.getLength();j++)
                {
                    Node m=experimentContents.item(j);
                
                    if(m.getNodeName().equals("description"))
                    {
                        e.description=m.getTextContent();
                    }

                    else if(m.getNodeName().equals("title"))
                    {
                        e.name=m.getTextContent();
                    }

                    else if(m.getNodeName().equals("report"))
                    {
                        e.report=Boolean.parseBoolean(m.getNodeValue());
                    }

                    else if(m.getNodeName().equals("resserver"))
                    {
                        e.resServer=m.getNodeValue();
                    }

                    else if(m.getNodeName().equals("resport"))
                    {
                        try
                        {
                            e.resPort=Integer.parseInt(m.getNodeValue());
                        } catch(NumberFormatException exc)
                        {
                            l.logErr(PARSER_TAG, "Error in response port tag! Not a valid integer.");
                        }
                        
                    }

                    else if(m.getNodeName().equals("data"))
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
                                    de.setClass(dataNode.getAttributes().getNamedItem("class").getTextContent());
                                }
                                
                                if(dataNode.getAttributes().getNamedItem("label")!=null)
                                {
                                    de.setLabel(Integer.parseInt((dataNode.getAttributes().getNamedItem("label").getTextContent())));
                                }
                                
                                de.setUrl(dataNode.getTextContent());
                                
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
    
    private Experiment doNuggetParsing(Document doc) throws NoContentException
    {
        Experiment e=new Experiment();
        NodeList list=doc.getChildNodes();
        DataElement element=new DataElement();

        if(list.getLength()==0)
        {
            throw new NoContentException();
        }

        for(int i=0;i<list.getLength();i++)
        {
            Node node=list.item(i);

            if(node.getNodeName().equals("title"))
            {
                e.name=(node.getTextContent());
            }

            else if(node.getNodeName().equals("description"))
            {
                e.description=(node.getTextContent());
            }

            else if(node.getNodeName().equals("class"))
            {
                element.setClass(node.getTextContent());
            }

            else if(node.getNodeName().equals("url"))
            {
                element.setUrl(node.getTextContent());
            }

            else if(node.getNodeName().equals("label"))
            {
                element.setLabel(Integer.parseInt(node.getTextContent()));
            }

            else if(node.getNodeName().equals("resserver"))
            {
                e.resServer=(node.getTextContent());
            }

            else if(node.getNodeName().equals("resport"))
            {
                e.resPort=(Integer.parseInt(node.getTextContent()));
            }
        }
        
        e.urlList.add(element);
        
        return e;
    }
    
    public Experiment parseNuggetStringForInformation(String nuggetData) throws NoContentException
    {
        Experiment e=new Experiment();
        DocumentBuilderFactory docBuildFactory=DocumentBuilderFactory.newInstance();
        
        try
        {
            DocumentBuilder builder=docBuildFactory.newDocumentBuilder();
            Document doc=builder.parse(new InputSource(new StringReader(nuggetData)));
            
            e=doNuggetParsing(doc);
        } catch(ParserConfigurationException ex)
        {
            l.logErr(PARSER_TAG, "Couldn't parse nugget due to misconfiguration.");
        } catch(SAXException ex)
        {
            l.logErr(PARSER_TAG, "SAXException while parsing XML nugget.");
        } catch(IOException ex)
        {
            l.logErr(PARSER_TAG, "Couldn't read temporary file containing RSSE nugget!");
        }
        
        return e;
    }
    
    /**
     * Returns an ArrayList containing all data elements in the specified XML nugget.
     * Please note that it returns an ArrayList in order to provide API compatibility for future expansion.
     * @param f
     * @return 
     */
    public Experiment parseNuggetFileForInformation(File f) throws NoContentException
    {
        Experiment e=new Experiment();
        DocumentBuilderFactory docBuildFactory=DocumentBuilderFactory.newInstance();
        
        try
        {
            DocumentBuilder builder=docBuildFactory.newDocumentBuilder();
            Document doc=builder.parse(f);
            
            e=doNuggetParsing(doc);
        } catch(ParserConfigurationException ex)
        {
            l.logErr(PARSER_TAG, "Couldn't parse nugget due to misconfiguration.");
        } catch(SAXException ex)
        {
            l.logErr(PARSER_TAG, "SAXException while parsing XML nugget.");
        } catch(IOException ex)
        {
            l.logErr(PARSER_TAG, "Couldn't read temporary file containing RSSE nugget!");
        }
        
        return e;
    }
    
    //Implement some lightweight parsers for things that really don't require a full-blown XML parser:
    
    /**
     * Returns all of the experiment names contained within the specified string.
     * It might also work for all URLs within a specified string as well.
     * @param s The string to parse.
     * @return An ArrayList of experiment names.
     */
    public ArrayList<String> parseExperimentsFromString(String s)
    {
        ArrayList<String> returnList=new ArrayList<String>();
        
        DocumentBuilderFactory docFactory=DocumentBuilderFactory.newInstance();
        
        try
        {
            DocumentBuilder builder=docFactory.newDocumentBuilder();
            Document doc=builder.parse(new InputSource(new StringReader(s)));
            
            //Get all of the experiments in the file:
            NodeList list=doc.getElementsByTagName("nugget");//doc.getChildNodes();//doc.getElementsByTagName("nugget");
            //list=list.item(0).getChildNodes();
            System.out.println(list.getLength());
            
            for(int i=0;i<list.getLength();i++)
            {
                //Pick out the nodes:
                Node n=list.item(i);
                NodeList experimentContents=n.getChildNodes();
                
                for(int j=0;j<experimentContents.getLength();j++)
                {
                    Node m=list.item(j);
                
                    if(m.getNodeName().equals("url")||m.getNodeName().equals("title"))
                    {
                        returnList.add(n.getTextContent());
                    }
                }
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
    
    public long parseIdNugget(String s)
    {
        long lng=-1;
        
        DocumentBuilderFactory docFactory=DocumentBuilderFactory.newInstance();
        
        try
        {
            DocumentBuilder builder=docFactory.newDocumentBuilder();
            Document doc=builder.parse(new InputSource(new StringReader(s)));
            
            //Get all of the experiments in the file:
            NodeList list=doc.getChildNodes();//doc.getElementsByTagName("nugget");
            list=list.item(0).getChildNodes();
            System.out.println(list.getLength());
            
            for(int i=0;i<list.getLength();i++)
            {
                //Pick out the nodes:
                Node n=list.item(i);
                NodeList experimentContents=n.getChildNodes();
                
                for(int j=0;j<experimentContents.getLength();j++)
                {
                    Node m=list.item(j);
                
                    if(m.getNodeName().equals("id"))
                    {
                        lng=Long.parseLong(m.getTextContent());
                        return lng;
                    }
                }
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
        
        return lng;
    }
    
    public DataElement parseForDataElement(String s)
    {
        DataElement returnElement=new DataElement();
        
        DocumentBuilderFactory docFactory=DocumentBuilderFactory.newInstance();
        
        try
        {
            DocumentBuilder builder=docFactory.newDocumentBuilder();
            Document doc=builder.parse(new InputSource(new StringReader(s)));
            
            //Get all of the experiments in the file:
            NodeList list=doc.getElementsByTagName("nugget");//doc.getChildNodes();//doc.getElementsByTagName("nugget");
            //list=list.item(0).getChildNodes();
            System.out.println(list.getLength());
            
            for(int i=0;i<list.getLength();i++)
            {
                //Pick out the nodes:
                Node n=list.item(i);
                NodeList experimentContents=n.getChildNodes();
                
                for(int j=0;j<experimentContents.getLength();j++)
                {
                    Node m=list.item(j);
                
                    if(m.getNodeName().equals("url"))
                    {
                        returnElement.setUrl(m.getTextContent());
                    }
                    
                    else if(m.getNodeName().equals("class"))
                    {
                        returnElement.setClass(m.getTextContent());
                    }
                    
                    else if(m.getNodeName().equals("label"))
                    {
                        try
                        {
                            returnElement.setLabel(Integer.parseInt(m.getTextContent()));
                        } catch(NumberFormatException e)
                        {
                            l.logErr(PARSER_TAG, "Couldn't parse label from \""+m.getTextContent()+"\"");
                        }
                    }
                }
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
        
        return returnElement;
    }
}
