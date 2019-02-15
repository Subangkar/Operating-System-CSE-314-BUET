package Utilities;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Created by Mushfiq on 25/May/2016.
 */
public class ConnectionUtility {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private InputStream is;
    private OutputStream os;
    private boolean alive;

    public ConnectionUtility(Socket socket) {
        try {
            alive = true;
            this.socket = socket;
            os = socket.getOutputStream();
            is = socket.getInputStream();
            oos = new ObjectOutputStream(os);
            ois = new ObjectInputStream(is);
        } catch (IOException e) {
            System.out.println("Error opening streams!");
        }
    }

    public ConnectionUtility(String host, int port) {
        try {
            alive = true;
            socket = new Socket(host, port);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            ois = new ObjectInputStream(is);
            oos = new ObjectOutputStream(os);
        } catch (IOException e) {
            //System.out.println("Error opening connection!");
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setSocketTimeOutOn(int timeOut) {
        try {
            socket.setSoTimeout(timeOut);
        } catch (SocketException e) {
            System.out.println("Socket Exception: " + e.toString());
        }
    }

    public Object read() {
        Object obj;
        try {
            obj = ois.readObject();
        } catch (SocketTimeoutException ste) {
            //System.out.println("I timed out!");
            return -2;
        } catch (IOException e) {
            System.out.println("Error reading object!" + e);
            return -1;
        } catch (ClassNotFoundException e) {
            System.out.println("Error! Class not found!");
            return -1;
        }

        return obj;
    }

    public void write(Object o) {
        try {
            oos.writeObject(o);
            oos.reset();
        } catch (IOException e) {
            System.out.println("Error writing object!");
        }
    }


    public void closeConnection() {
        try {
            alive = false;
            ois.close();
            oos.close();
        } catch (Exception e) {
            System.out.println("Closing Error in network : " + e.toString());
        }
    }
}
