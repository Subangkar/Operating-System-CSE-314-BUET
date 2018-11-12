/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.util.ArrayList;
import java.util.HashMap;
import util.ConnectionUtillities;

/**
 *
 * @author uesr
 */
public class CreateClientConnection implements Runnable{
    public HashMap<String,Information> clientList;
    public ConnectionUtillities connection;

    public CreateClientConnection(HashMap<String, Information> list, ConnectionUtillities con){
        clientList=list;
        connection=con;
    }
    
    @Override
    public void run() {
        Object o;
        String username;
        while(true) {
            o=connection.read();
            username=o.toString();
            System.out.println(username + " is read");
            if(clientList.containsKey(username)) {
                connection.write(username + " is already logged in, try another username");
                connection.write("Enter your username : ");
            }
            else  { break;}
        }
        connection.write("Successfully logged in");
        clientList.put(username, new Information(connection, username));
        new Thread(new ServerReaderWriter(username,connection, clientList)).start();
    }
}
