/*
 * This class provides the tools necessary to modify experiments given a delta string.
 */

package experimentclient2;

/**
 *
 * @author mgohde
 */
public class DeltaResolver
{
    private String deltaString;
    private DeltaTool t;
    
    public DeltaResolver(String deltaString)
    {
        this.deltaString=deltaString;
        
        t=new DeltaTool();
    }
    
    
}
