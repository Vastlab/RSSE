/*
 * This is an individual URL/label/class combination to send to the client.
 * It is write-once, so it uses private attributes where possible.
 */

package experimentalserverservice;

import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class DataElement
{
    private String url;
    private String className;
    private int label;
    private boolean labeled;
    private long idNum; //Used for additions to experiment files.
    //private String title;
    //private String description;
    //private String respServer;
    //private int respPort;
    
    public DataElement()
    {
        labeled=false;
        url=null;
        className=null;
        label=-1000;
        idNum=0;
    }
    
    /**
     * Generates a DataElement from a string. 
     * This constructor is best suited for network transfers, as it is compatible with this class' toString method.
     * @param s 
     */
    public DataElement(String s)
    {
        Scanner strScanner=new Scanner(s);
        
        url=strScanner.next();
        className=strScanner.next();
        label=Integer.parseInt(strScanner.next());
        labeled=Boolean.parseBoolean(strScanner.next());
        //idNum=Long.parseLong(strScanner.next());
        //title=null;
        //description=null; //Neither the title nor description need to be transmitted or received.
    }
    
    /*
    public String getResServer()
    {
        return respServer;
    }
    
    public void setResServer(String server)
    {
        respServer=server;
    }
    
    public int getResPort()
    {
        return respPort;
    }
    
    public void setResPort(int portNum)
    {
        respPort=portNum;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public void setTitle(String newTitle)
    {
        title=newTitle;
    }
    
    public String getDescription()
    {
        return description;
    }
    
    public void setDescription(String newDescription)
    {
        description=newDescription;
    }*/
    
    public String getCls()
    {
        return className;
    }
    
    public void setClass(String newClass)
    {
        className=newClass;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public void setUrl(String newURL)
    {
        url=newURL;
    }
    
    public int getLabel()
    {
        return label;
    }
    
    public long getId()
    {
        return idNum;
    }
    
    public void setId(long newId)
    {
        idNum=newId;
    }
    
    public void setLabel(int newLabel)
    {
        label=newLabel;
        
        if(label!=-1000)
        {
            labeled=true;
        }
    }
    
    public boolean isLabeled()
    {
        return labeled;
    }
    
    @Override
    public String toString()
    {
        return url+" "+className+" "+label+" "+labeled+" "+idNum;
    }
    
    boolean equals(DataElement e)
    {
        return e.getUrl().equals(getUrl());
    }
}
