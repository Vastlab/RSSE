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
public interface BSTElement
{
    public Object getValue();
    public void setValue();
    
    //Overriden to provide some way of dumping this data to disk.
    @Override
    public String toString();
    public void fromString(String s);
}
