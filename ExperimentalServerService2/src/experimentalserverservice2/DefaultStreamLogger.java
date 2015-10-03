/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package experimentalserverservice2;

/**
 *
 * @author mgohde
 */
public class DefaultStreamLogger implements Logger
{
    /**
     * Logs an error to whichever stream is currently selected.
     * @param tag
     * @param message 
     */
    @Override
    public void logErr(String tag, String message)
    {
        //For now it just prints to stderr:
        System.err.println("["+tag+"]\t"+message);
    }
    
    @Override
    public void logMsg(String tag, String message)
    {
        //See logErr.
        System.out.println("["+tag+"]\t"+message);
    }
    
    public DefaultStreamLogger()
    {
        //No nothing.
    }
}
