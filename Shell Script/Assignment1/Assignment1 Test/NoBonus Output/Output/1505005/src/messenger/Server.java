/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.*;
import javax.print.attribute.HashAttributeSet;
import util.ConnectionUtillities;

/**
 *
 * @author uesr
 */
public class Server {
    
    public ServerSocket servSocket;
    HashMap<String,Information> clientList;
    
    public Server(int port){
        
        clientList=new HashMap<String, Information>();
        
        try {
            servSocket=new ServerSocket(port);
            
            while(true){
                printHashMap();
                Socket clientSocket=servSocket.accept();
                ConnectionUtillities connection=new ConnectionUtillities(clientSocket);
                new Thread(new CreateClientConnection(clientList,connection)).start();
            }
            
             
        } catch (Exception ex) {
            System.out.println("Exception in server :" + ex.toString());
            //ex.printStackTrace();
        }
        
        
    }
    
    public void printHashMap(){
        
        Set set = clientList.entrySet();
      
        Iterator i = set.iterator();      
        System.out.println("Current User--");
        while(i.hasNext()) {
           Map.Entry me = (Map.Entry)i.next();
           System.out.println(me.getKey() + " : ");                   
        }
    
        
    }
    
    
    public static void main(String[] args) {
        new Server(22222);
    }
}
