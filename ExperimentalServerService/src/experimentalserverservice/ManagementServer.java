/*
 * This class manages and maintains the ExperimentalServerService internally.
 * As such, its functionality takes over from the main method of the ExperimentalServerService class.
 *
 * Its implementation will be far cleaner than the sloppy remote CLI implemented in the CacheModule.
 * As such, it will internally accept and queue ManagementRequests that are formatted and interpreted by some
 * remote bit of code.
 */

package experimentalserverservice;

/**
 *
 * @author mgohde
 */
public class ManagementServer
{
    private Thread executorThread; //This thread serially executes connections.
    private Thread connectionAcceptThread; //This thread queues connections for the executor.
    private ArrayList<Connection> connectionQueue;
}
