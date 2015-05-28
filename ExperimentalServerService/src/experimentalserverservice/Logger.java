/*
 * This is the logging class used by RSSE.
 * By putting logging functions here, there should be greater flexibility in terms of 
 * what customizations and other such stuff can be added as portions of RSSE shift to becoming
 * background services.
 */

package experimentalserverservice;

/**
 *
 * @author mgohde
 */
public class Logger
{
    /**
     * Logs an error to whichever stream is currently selected.
     * @param tag
     * @param message 
     */
    public void logErr(String tag, String message)
    {
        //For now it just prints to stderr:
        System.err.println("["+tag+"]\t"+message);
    }
    
    public void logMsg(String tag, String message)
    {
        //See logErr.
        System.out.println("["+tag+"]\t"+message);
    }
    
    public Logger()
    {
        //No nothing.
    }
}
