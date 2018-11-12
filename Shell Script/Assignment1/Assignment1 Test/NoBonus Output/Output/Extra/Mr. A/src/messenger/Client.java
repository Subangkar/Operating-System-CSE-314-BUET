/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.util.Scanner;
import util.ConnectionUtillities;
import util.Reader;
import util.Writer;

/**
 *
 * @author uesr
 */
public class Client {
    
    public static void main(String[] args) {
        ConnectionUtillities connection=new ConnectionUtillities("127.0.0.1",22222);
        
        System.out.println("Enter your username : ");
        
        Scanner in = new Scanner(System.in);
        String username=in.nextLine();                
        connection.write(username);        
        
        new Thread(new Reader(connection)).start();
        new Thread(new Writer(connection)).start();
        
        while(true);
    }
}
