/*
 * The cache database will be implemented as a binary search tree because doing so should allow for the simplified
 * removal of duplicate caching requests and fairly fast searches. 
 * 
 * In the future this will be replaced by some other means of managing a cache database, as I expect 
 *    it to fail on larger caches (IIRC, in previous implementations the JVM couldn't handle more than a few million elements.
 * 
 * This class is designed as a container with public data because it is unlikely to be used in any other parts of the code
 * than the database itself. 
 */

package cachemodule;

/**
 *
 * @author mgohde
 */
public class CacheNodeWrapper
{
    public CacheNodeWrapper left, right;
    public CacheNode data;
    
    public CacheNodeWrapper(CacheNodeWrapper newLeft, CacheNodeWrapper newRight, CacheNode newData)
    {
        left=newLeft;
        right=newRight;
        data=newData;
    }
}
