/*
 * The experiment client consists mostly of existing codebases just with the added bonus of being able to communicate with
 * a caching server.
 */

package experimentclient;

/**
 *
 * @author mgohde
 */
public class ExperimentClient
{
    private static final String NAME="java -jar EC.jar";
    private static final String EC_TAG="ExperimentClient";
    
    public static void printHelp()
    {
        System.out.println("Usage: "+NAME+" [args]");
        System.out.println("Connect to Experiment Servers and fetch data.");
        System.out.println("\t-h, --help\tDisplay this message.");
        System.out.println("\t-i, --initial\tRegister with the Experiment Server.");
        System.out.println("\t--genconfig\tGenerate a configuration file for an experiment.");
        System.out.println("\t-s, --server\tUse the server specified rather than the one configured.");
        System.out.println("\t-p, --port\tUse the port specified rather than the one configured.");
        System.out.println("\t-l, --list\tList all experiments hosted by the Experiment Server.");
        System.out.println("\t-d, --usedir\tUse the specified directory instead of .");
        System.out.println("\t-n, --next\tFetch the next file from the server.");
        System.out.println("\t-a, --all\tFetch all files from the server.");
        System.out.println("\t-cp, --cacheprt\tConnect to the Cache Module on the port specified.");
        System.out.println("\t-cs, --cachesrv\tConnect to the Cache Module at the address specified.");
    }
    
    public static void writeConfig()
    {
        
    }
    
    public static void execArgs(String[] args)
    {
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        execArgs(args);
    }
    
}
