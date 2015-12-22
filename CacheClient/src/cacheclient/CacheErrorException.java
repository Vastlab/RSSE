/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cacheclient;

/**
 *
 * @author mgohde
 */
public class CacheErrorException extends Exception
{
    private int internalCode;
    
    public CacheErrorException(int errCode)
    {
        internalCode=errCode;
    }
    
    public int getErr()
    {
        return internalCode;
    }
    
    @Override
    public String toString()
    {
        return "CacheError: "+internalCode;
    }
}
