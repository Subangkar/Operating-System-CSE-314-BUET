/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author uesr
 */
public class CreateClientConnection implements Runnable{
    public HashMap<String,Information> clientList;
    public ConnectionUtillities connection;
    public Vector<Chunk> serverStorage;
    public CreateClientConnection(HashMap<String,Information> list, ConnectionUtillities con,Vector<Chunk> serverStorage){
        clientList=list;
        connection=con;
        this.serverStorage=serverStorage;
    }
    
    @Override
    public void run() {
        Object o= null;
        try {
            o = connection.read();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }
        String username=o.toString();
        if(clientList.containsKey(username))
        {
            connection.write("log in is Denied. User already logged in.");
        }
        else
        {
            clientList.put(username, new Information(connection, username));
            connection.write("Log in Successful");
            new Thread(new ServerReaderWriter(username,connection, clientList,serverStorage)).start();
        }

    }
    
    
    
}
