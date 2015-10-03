/*
 * This logger implementatoin allows the RSSE clients and servers to log their messages either to the same file or different files.
 */

package experimentalserverservice2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 *
 * @author mgohde
 */
public class FileLogger implements Logger
{
    private PrintWriter out, err;
    
    /**
     * Logs an error to whichever stream is currently selected.
     * @param tag
     * @param message 
     */
    @Override
    public void logErr(String tag, String message)
    {
        //For now it just prints to stderr:
        err.println("["+tag+"]\t"+message);
        err.flush();
    }
    
    @Override
    public void logMsg(String tag, String message)
    {
        //See logErr.
        out.println("["+tag+"]\t"+message);
        out.flush();
    }
    
    public FileLogger(File f) throws FileNotFoundException
    {
        out=new PrintWriter(f);
        err=out;
    }
    
    public FileLogger(File out, File err) throws FileNotFoundException
    {
        this.out=new PrintWriter(out);
        this.err=new PrintWriter(err);
    }
    
    @Override
    public void finalize() throws Throwable
    {
        super.finalize();
        
        if(out==err)
        {
            out.close();
        }
        
        else
        {
            out.close();
            err.close();
        }
    }
}
