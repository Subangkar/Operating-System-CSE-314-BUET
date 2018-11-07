/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;
/**
 *
 * @author uesr
 */
public class Client {

    public static void main(String[] args) {
        ConnectionUtillities connection=new ConnectionUtillities("127.0.0.1",5000);
        System.out.println(connection.sc.getInetAddress()+"Enter your username : ");

        Scanner in = new Scanner(System.in);
        String username=in.nextLine();
        connection.write(username);
        Object o= null;
        try {
            o = connection.read();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }
        if(o.toString().equals("log in is Denied. User already logged in."))
        {
            System.out.println(o.toString());
        }
        else{
            new Thread(new Reader(connection)).start();
        }

        //new Thread(new Writer(connection)).start();

        while(true);
    }
}
