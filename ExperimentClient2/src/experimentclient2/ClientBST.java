/*
 * This is a wrapper of the BST class that manages a set of clients.
 */

package experimentclient2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author mgohde
 */
public class ClientBST
{
    private BST<Client> internalBST;
    
    public ClientBST()
    {
        internalBST=new BST<Client>();
    }
    
    //Now forward all of the BST's default stuff:
    public void insert(int index, Client c)
    {
        internalBST.insert(index, c);
    }
    
    public Client lookup(int index)
    {
        return internalBST.lookup(index);
    }
    
    public void dump(File f) throws IOException
    {
        internalBST.dump(f);
    }
    
    public void restore(File f) throws FileNotFoundException
    {
        Scanner fileScanner;
        int tmpIndex;
        Client c;
        
        fileScanner=new Scanner(f);
        
        while(fileScanner.hasNextLine())
        {
            c=new Client();
            c.fromString(fileScanner.nextLine());
            tmpIndex=Integer.parseInt(fileScanner.nextLine());
            
            insert(tmpIndex, c);
        }
    }
}
