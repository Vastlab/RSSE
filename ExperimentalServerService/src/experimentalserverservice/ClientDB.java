/*
 * This class is a container for all clients currently registered with the Experimental Server service.
 *
 * It implements client storage using a binary search tree for the following reasons:
 *  1. Adding users is cheaper than it would be for an Array or ArrayList
 *  2. Users can be added and removed out of order.
 *  3. While searching isn't O(1) like an ArrayList (User IDs could be indices in the array), 
 *      it should generally still be fast enough.
 * Drawbacks of using a BST:
 *  1. To make the tree worthwhile, user IDs have to be either out of order or randomly generated.
 *  2. Generally higher RAM usage vs an array (except when adding new elements)
 * 
 * Other, minimal considerations:
 *  1. If ID generation is properly implemented, a modern computer's branch predictor will be thoroughly put to the test.
 */

package experimentalserverservice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ClientDB
{
    private static final boolean DEBUG_MODE=false;
    private ClientStateNode root;
    private boolean first;          //This boolean changes how IDs are assigned.
    
    private ArrayList<ClientState> debugDb;
    private Random rand;
    
    public ClientDB(boolean isFirst)
    {
        root=null;
        first=isFirst;
        
        if(DEBUG_MODE)
        {
            debugDb=new ArrayList<ClientState>();
        }
        
        rand=new Random(System.currentTimeMillis());
    }
    
    public ArrayList<Long> getAllClientIds()
    {
        ArrayList<Long> idList=new ArrayList<Long>();
        
        if(DEBUG_MODE)
        {
            for(ClientState s:debugDb)
            {
                idList.add(s.clientId);
            }
        }
        
        else
        {
            rcsvGetAllClientIds(idList, root);
        }
        
        return idList;
    }
    
    public synchronized ClientState getClientById(long id)
    {
        ClientStateNode wrapper;
        
        if(DEBUG_MODE)
        {
            for(int i=0;i<debugDb.size();i++)
            {
                if(debugDb.get(i).clientId==id)
                {
                    return debugDb.get(i);
                }
            }
            
            return null;
        }
        
        else
        {
            wrapper=internalFindById(id, root);

            if(wrapper==null)
            {
                return null;
            }

            else
            {
                return wrapper.s;
            }
        }
    }
    
    private int debugFindById(long id)
    {
        int i;
        
        for(i=0;i<debugDb.size();i++)
        {
            if(debugDb.get(i).clientId==id)
            {
                return i;
            }
        }
        
        return -1;
    }
    
    public synchronized void removeClientState(ClientState s)
    {
        if(DEBUG_MODE)
        {
            int index;
            
            index=debugFindById(s.clientId);
            
            if(index!=-1)
            {
                debugDb.remove(index);
            }
            
            return;
        }
        
        ClientStateNode parent;
        ClientStateNode child;
        
        child=internalFindById(s.clientId, root);
        
        if(child!=null)
        {
            parent=internalFindByChild(child, root);
            
            if(parent.left==child)
            {
                parent.left=null;
            }
            
            else
            {
                parent.right=null;
            }
            
            //Re-insert the child's children.
            rcsvAdd(child.left);
            rcsvAdd(child.right);
        }
        
        else //Looks like we need to add it.
        {
            internalAdd(s, root);
        }
    }
    
    public synchronized boolean isFirst()
    {
        return first;
    }
    
    public synchronized long generateId()
    {
        long val;
        
        if(first)
        {
            first=false;
            return Long.MAX_VALUE/2; //Ensure that the tree is balanced.
        }
        
        val=rand.nextLong();
        
        while(internalFindById(val, root)!=null)
        {
            val=rand.nextLong();
        }
        
        return val;
    }
    
    public synchronized void updateClient(ClientState s)
    {
        ClientStateNode client;
        
        if(DEBUG_MODE)
        {
            int index;
            
            index=debugFindById(s.clientId);
            
            if(index==-1)
            {
                debugDb.add(s);
            }
            
            else
            {
                debugDb.set(index, s);
            }
            
            return;
        }
        
        client=internalFindById(s.clientId, root);
        
        if(client==null) //Add it.
        {
            System.out.println("Client null. Adding...");
            if(root==null)
            {
                root=new ClientStateNode(null, null, s);
            }
            
            else
            {
                internalAdd(s, root);
            }
        }
        
        else //Edit it.
        {
            System.out.println("\t\tUpdating client!");
            client.s=s;
        }
    }
    
    public void dump(PrintWriter o)
    {
        if(DEBUG_MODE)
        {
            int i;
            
            System.out.println("Dumping "+debugDb.size()+" elements to disk.");
            
            for(i=0;i<debugDb.size();i++)
            {
                o.println(debugDb.get(i).toString());
                o.println(debugDb.get(i).expName);
            }
            
            return;
        }
        
        if(o!=null&&root!=null)
        {
            rcsvDump(o, root);
        }
    }
    
    public void restore(File f) throws FileNotFoundException
    {
        String temp, temp1;
        Scanner s;
        
        s=new Scanner(f);
        
        while(s.hasNextLine())
        {
            temp=s.nextLine();
            temp1=s.nextLine();
            
            updateClient(new ClientState(temp, temp1));
        }
    }
    
    private void rcsvGetAllClientIds(ArrayList<Long> list, ClientStateNode n)
    {
        if(n!=null)
        {
            list.add(n.s.clientId);
            rcsvGetAllClientIds(list, n.left);
            rcsvGetAllClientIds(list, n.right);
        }
    }
    
    private void rcsvDump(PrintWriter o, ClientStateNode curNode)
    {
        //This is internally just going to be a recursive traversal with the current node first.
        
        if(curNode!=null)
        {
            o.println(curNode.s.toString());
            o.println(curNode.s.expName);
            rcsvDump(o, curNode.left);
            rcsvDump(o, curNode.right);
        }
    }
    
    private void rcsvAdd(ClientStateNode curNode)
    {
        if(curNode!=null)
        {
            rcsvAdd(curNode.left);
            internalAdd(curNode.s, root);
            rcsvAdd(curNode.right);
        }
    }
    
    private void internalAdd(ClientState newState, ClientStateNode curNode)
    {
        if(DEBUG_MODE)
        {
            debugDb.add(newState);
            return;
        }
        
        if(curNode!=null)
        {
            if(newState.clientId>curNode.s.clientId)
            {
                if(curNode.right==null)
                {
                    System.out.println("Instantiating new client node...");
                    curNode.right=new ClientStateNode(null, null, newState);
                }
                
                else
                {
                    internalAdd(newState, curNode.right);
                }
            }
            
            else
            {
                if(curNode.left==null)
                {
                    System.out.println("Instantiating new client node...");
                    curNode.left=new ClientStateNode(null, null, newState);
                }
                
                else
                {
                    internalAdd(newState, curNode.left);
                }
            }
        }
    }
    
    private ClientStateNode internalFindById(long idNum, ClientStateNode cur)
    {
        if(cur==null)
        {
            return null;
        }
        
        else
        {
            if(idNum>cur.s.clientId)
            {
                return internalFindById(idNum, cur.right);
            }
            
            else if(idNum<cur.s.clientId)
            {
                return internalFindById(idNum, cur.left);
            }
            
            else
            {
                return cur;
            }
        }
    }
    
    private ClientStateNode internalFindByChild(ClientStateNode child, ClientStateNode cur)
    {
        if(cur==null)
        {
            return null; //This really should hopefully never happen.
        }
        
        else
        {
            if(cur.left==child)
            {
                return cur;
            }
            
            else if(cur.right==child)
            {
                return cur;
            }
            
            else //Search the next level:
            {
                if(child.s.clientId>cur.s.clientId)
                {
                    return internalFindByChild(child, cur.right);
                }
                
                else //Let's just assume that the child can't be its own parent.
                {
                    return internalFindByChild(child, cur.left);
                }
            }
        }
    }
    
    
}
