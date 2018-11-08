
package Offline;

/**
 *
 * @author Rifat
 */
import java.io.*;
import java.net.*;

public class NameSocket {
    public String str;
    public Socket sckt;
    
    public NameSocket(String str,Socket sckt)
    {
        this.str=str;
        this.sckt=sckt;
    }
}
