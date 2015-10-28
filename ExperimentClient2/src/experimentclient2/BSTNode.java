/*
 * Node for the BST.
 */

package experimentclient2;

/**
 *
 * @author mgohde
 * @param <T>
 */
public class BSTNode<T>
{
    public int index;
    public T val;
    public BSTNode<T> left;
    public BSTNode<T> right;
    
    public BSTNode(BSTNode<T> newLeft, BSTNode<T> newRight, T newVal, int newIndex)
    {
        left=newLeft;
        right=newRight;
        val=newVal;
        index=newIndex;
    }
}
