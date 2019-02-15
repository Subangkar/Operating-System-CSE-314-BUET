/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.exit;

/**
 *
 * @author uesr
 */
public class ConnectionUtillities {
    public Socket sc;
    public ObjectInputStream ois;
    public ObjectOutputStream oos;
    
    public ConnectionUtillities(String host, int port){
        try {
            sc=new Socket(host,port);
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
        } 
        catch(Exception e)
        {
            System.out.println("Exception in connectionUtilities for Client : " + e.toString());
        }
        
    }
    
    public ConnectionUtillities(Socket socket){
        try {
            sc=socket;
            oos=new ObjectOutputStream(sc.getOutputStream());
            ois=new ObjectInputStream(sc.getInputStream());
        } 
        catch(Exception e)
        {
            System.out.println("Exception in connectionUtilities for Server: " + e.toString());
            //e.printStackTrace();
        }
    }
    
    public void write(Object o){
        try {
            oos.writeObject(o);
        } catch (IOException ex) {
         //   Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error in write function");
            System.out.println("Server has gone offline, connection will be terminated");
            try {
                sc.close();
            } catch (IOException e1) {
                System.out.println("Socket could not be closed properly");
            }
            exit(0);
        }
    }
    
    public Object read(){
        try {
            Object o=ois.readObject();
            return o;
        } catch (IOException ex) {
        //    Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Server has gone offline, connection is terminated");
            try {
                sc.close();
                exit(0);
            } catch (IOException e) {
                System.out.println("This socket could not be closed");
            }
        }

        catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionUtillities.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}
