/*
 * This is a caching module for use with the RSSE software stack.
 */

package cachemodule;

/**
 *
 * @author mgohde
 */
public class CacheModule
{
    public static Logger l;
    public static CMConfig cfg;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        l=new Logger();
        cfg=new CMConfig();
    }
    
}
