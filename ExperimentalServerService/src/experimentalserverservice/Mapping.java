/*
 * This is a container class that contains mappings in indexes between two values.
 */

package experimentalserverservice;

/**
 *
 * @author mgohde
 */
public class Mapping
{
    public int from;
    public int to;
    
    public Mapping()
    {
        from=0;
        to=0;
    }
    
    public Mapping(int newFrom, int newTo)
    {
        from=newFrom;
        to=newTo;
    }
}
