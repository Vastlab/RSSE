/*
 * This is an interface for the various databases that the program can use.
 * Overall, implementing this interface should ease the transition to smarter, working
 * database backends such as hashtables, etc.
 */
package cachemodule;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public interface Database 
{
    public boolean loadDatabase(File snapshot);
    public boolean saveDatabase(File snapshot);
    public boolean remove(String uri);
    public CacheNode find(String uri);
    public boolean add(CacheNode newNode);
    public ArrayList<String> generateReport();
}
