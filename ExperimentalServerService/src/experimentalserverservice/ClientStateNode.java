/*
 * This is just a wrapper for the client state object so that it may be put into a binary search tree.
 * Alternatively, each client ID can be the index in an ArrayList, though that likely wouldn't scale very well.
 */

package experimentalserverservice;

/**
 *
 * @author mgohde
 */
public class ClientStateNode
{
    public ClientStateNode left, right;
    public ClientState s;
    
    public ClientStateNode(ClientStateNode newLeft, ClientStateNode newRight, ClientState newState)
    {
        left=newLeft;
        right=newRight;
        s=newState;
    }
}
