/*
 * This stores some configuration information to be loaded at startup.
 */

package cachemodule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class CMConfig
{
    private String storageDir;
    private String configDir;
    private String serverPort;
    private int configVersion;
    
    private static final String DEFAULT_STORAGE_DIR="./storage";
    private static final String DEFAULT_CONFIG_DIR="./";
    private static final String DEFAULT_SERVER_PORT="9001";
    
    public static final int SETTING_STORAGE_DIR=1;
    public static final int SETTING_CONFIG_DIR=2;
    public static final int SETTING_SERVER_PORT=3;
    public static final String SETTING_STORAGE_DIR_STR="storage_dir";
    public static final String SETTING_CONFIG_DIR_STR="config_dir";
    public static final String SETTING_SERVER_PORT_STR="server_port";
    public static final String CONFIG_VERSION_STR="config_version";
    
    
    public String getSetting(int setting)
    {
        switch(setting)
        {
            case SETTING_STORAGE_DIR:
                return storageDir;
            case SETTING_CONFIG_DIR:
                return configDir;
            case SETTING_SERVER_PORT:
                return serverPort;
        }
        
        return null;
    }
    
    public boolean loadSettings(File f)
    {
        try
        {
            String buffr;
            String str;
            Scanner s=new Scanner(f);
            
            //Read version information:
            buffr=s.nextLine();
            Scanner buffrScanner=new Scanner(buffr);
            buffrScanner.next();
            configVersion=buffrScanner.nextInt();
            
            if(configVersion==1)
            {
                while(s.hasNextLine())
                {
                    buffr=s.nextLine();
                    System.out.println(buffr);
                    buffrScanner=new Scanner(buffr);
                    str=buffrScanner.next();//str=s.next();
                    
                    //Maintain Java 6 compatibility:
                    if(str.equals(SETTING_STORAGE_DIR_STR))
                    {
                        storageDir=buffrScanner.next();
                    }
                    
                    else if(str.equals(SETTING_CONFIG_DIR_STR))
                    {
                        configDir=buffrScanner.next();
                    }
                    
                    else if(str.equals(SETTING_SERVER_PORT_STR))
                    {
                        serverPort=buffrScanner.next();
                    }
                }
            }
        } catch(FileNotFoundException e)
        {
            return false;
        }
        
        return true;
    }
    
    public boolean saveSettings(File f)
    {
        System.out.println("In saveSettings");
        try
        {
            if(!f.exists())
            {
                f.createNewFile();
            }
            
            PrintWriter pw=new PrintWriter(f);
            
            // Write all current settings to disk: 
            pw.println(CONFIG_VERSION_STR+" "+RSSEConstants.RSSE_VERSION_STRING);
            pw.println(SETTING_STORAGE_DIR_STR+" "+storageDir);
            pw.println(SETTING_CONFIG_DIR_STR+" "+configDir);
            pw.println(SETTING_SERVER_PORT_STR+" "+serverPort);
            
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e)
        {
            System.out.println("Can't find file.");
            return false;
        } catch (IOException e)
        {
            System.out.println("Can't write file.");
            return false;
        }
        
        return true;
    }
    
    public int getConfVer()
    {
        return configVersion;
    }
    
    public boolean upgradeConfFile(File f)
    {
        // Not to be implemented until version 2.
        return true; //Always "upgrades" the config.
    }
    
    public CMConfig()
    {
        storageDir=DEFAULT_STORAGE_DIR;
        configDir=DEFAULT_CONFIG_DIR;
        serverPort=DEFAULT_SERVER_PORT;
    }
    
    public CMConfig(File f)
    {
        
    }
    
    public File getDefaultSettingsFile()
    {
        return new File(DEFAULT_CONFIG_DIR+"cm.conf");
    }
}
