package Server;

import Utilities.ConnectionUtility;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AddStudent implements Runnable {
    private ServerSocket serverSocket;

    AddStudent(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ConnectionUtility connUtil = new ConnectionUtility(socket);
                new Thread(new ServerReader(connUtil)).start();
            } catch (IOException e) {
                System.out.println("Cannot accept client socket!!!");
            }
        }
    }
}
