/*
 * This implements the cache's database with a hash table. 
 */
package cachemodule;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author mgohde
 */
public class HashTableDatabase implements Database
{

    @Override
    public boolean loadDatabase(File snapshot) 
    {
        return false;
    }

    @Override
    public boolean saveDatabase(File snapshot) 
    {
        return false;
    }

    @Override
    public boolean remove(String uri) 
    {
        return false;
    }

    @Override
    public CacheNode find(String uri) 
    {
        return null;
    }

    @Override
    public boolean add(CacheNode newNode) 
    {
        return false;
    }

    @Override
    public ArrayList<String> generateReport() 
    {
        return null;
    }
}
