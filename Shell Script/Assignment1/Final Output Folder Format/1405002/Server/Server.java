package Server;

import Utilities.ConnectionUtility;
import Utilities.FileInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    static long bufferSize = 1024L * 1024 * 1024 * 2;
    static long bufferUsed = 0;
    private static Random random = new Random();
    private static int fileId = 0;
    static ConcurrentHashMap<String, ConnectionUtility> senders;
    static ConcurrentHashMap<String, ConnectionUtility> receivers;
    static ConcurrentHashMap<String, FileInfo> files;

    private Server(int port) {
        //Scanner in = new Scanner(System.in);
        //bufferSize = in.nextLong();
        senders = new ConcurrentHashMap<>();
        receivers = new ConcurrentHashMap<>();
        files = new ConcurrentHashMap<>();
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            new Thread(new AddStudent(serverSocket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server(5000);
    }

    static String getFileId() {
        return "Mushfiq_" + Integer.toString(++fileId);
    }

    static int getChunkSize(int fileSize) {
        int kiloByte = 1024;
        int megaByte = 1024 * 1024;
        int x = Math.abs(random.nextInt()) % 1024;
        int y = Math.abs(random.nextInt()) % 10240;
        if (fileSize < 100) {
            return 5 + x / 200;
        } else if (fileSize < kiloByte) {
            return fileSize / 50 + x / 10;
        } else if (fileSize < 100 * kiloByte) {
            return fileSize / 100 + x;
        } else if (fileSize < megaByte) {
            return fileSize / 100 + y;
        } else if (fileSize < 200 * megaByte) {
            return fileSize / 100 + y * 10;
        } else {
            return 10 * megaByte + y * 10;
        }
    }
}
