package Clients;

import Utilities.ConnectionUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class ClientFileReceiver implements Runnable {
    private final int chunkSize = 1024 * 1024 * 5;
    private String studentId;
    private ConnectionUtility utility;
    private Scanner sc;
    private File file;
    private FileOutputStream outputStream;
    private long fileSize;
    private boolean isComplete;

    ClientFileReceiver(String studentId, ConnectionUtility utility) {
        this.studentId = studentId;
        this.utility = utility;
        sc = new Scanner(System.in);
    }

    @Override
    public void run() {
        boolean flag = true;
        String outToServer = "c1000" + studentId;
        utility.write(outToServer);
        while (flag) {
            String inFromServer = utility.read().toString();
            String code = inFromServer.substring(0, 5);
            String msg = inFromServer.substring(5);
            switch (code) {
                case "s1000":
                    String data[] = msg.split("%#%");
                    System.out.println("User " +
                            data[1] + " has sent a file.\nFile name: " +
                            data[2] + "\nFile size: " +
                            data[3] + "\nWant to receive it???(Y/n)");
                    fileSize = Long.parseLong(data[3]);
                    String reply = sc.next();
                    if (reply.startsWith("y") || reply.startsWith("y")) {
                        outToServer = "c1002" + "Yes";   //willing to receive file
                        System.out.println("OK, File Transmission Starting!!!");
                        utility.write(outToServer);
                    } else {
                        outToServer = "c1001" + "No";   //do not want to receive file
                        System.out.println("OK, File Transmission Aborted!!!");
                        utility.write(outToServer);
                    }
                    break;
                case "s1001":
                    while (true) {
                        System.out.println("Enter the path you wish to save the file:");
                        reply = sc.next();
                        file = new File(reply);
                        try {
                            if (!file.exists()) {
                                file.createNewFile();
                                outputStream = new FileOutputStream(file, true);
                                break;
                            } else {
                                System.out.println("File already exists.:(");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    outToServer = "c1003"; //starting the transmission
                    utility.write(outToServer);
                    break;
                case "s1002":
                    Object obj = utility.read();
                    byte[] buffer = (byte[]) obj;
                    try {
                        outputStream.write(buffer);
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (buffer.length == chunkSize) {
                        outToServer = "c1003";     //acknowledgement
                        utility.write(outToServer);
                    }
                    break;
                case "s1003":
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (file.length() != fileSize) {
                        file.delete();
                        isComplete = false;
                        utility.write("c1004" + "File not completely received.");
                        System.out.println("File not completely received");
                    } else {
                        isComplete = true;
                        utility.write("c1004" + "File completely received.");
                        System.out.println("File completely received");
                    }

                    file = null;
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    outputStream = null;

                    break;
                case "s1004":   //log out command from server reader
                    cleanUp();
                    flag = false;
                    break;

            }
            //System.out.println(msg);
        }

    }

    private void cleanUp() {
        if (!isComplete && file != null) {
            file.delete();
            file = null;
        }
        if (!isComplete && outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }
        if (utility.isAlive()) {
            utility.write("c1005" + "quit");
            utility.closeConnection();
        }
    }
}
