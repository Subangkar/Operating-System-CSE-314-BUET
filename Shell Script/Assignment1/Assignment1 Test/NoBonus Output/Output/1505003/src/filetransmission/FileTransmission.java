/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransmission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Abhishek
 */
public class FileTransmission {

    /**
     * @param args the command line arguments
     */
    public static Scanner scanner;
    public static int countFile  = 0;
    public static OutputStream outputStream = null;
    public static ServerSocket serverSocket = null;
    public static InputStream inputStream = null;
    public static Socket socket = null;
    public static void main(String[] args) {
        try {
            int portNo,a;
            scanner=new Scanner(System.in);
            System.out.println("Enter port number of machine(e.g. '2000') :");
            portNo=2000;
            ServerSocket serverSocket = new ServerSocket(portNo);
            System.out.println("Waiting for client...");
             scanner=new Scanner(System.in);
            System.out.println("Enter bufferSize :");
            a=scanner.nextInt();
            while(true)
            {
                socket = serverSocket.accept();
                Server server=new Server(socket);
            server.bufferSize=a;
                Thread t=new Thread(server);
                t.start();
                }
        } catch (IOException ex) {
            Logger.getLogger(FileTransmission.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
