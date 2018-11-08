package Server;

import Utilities.ConnectionUtility;
import Utilities.FileInfo;

import java.io.*;

public class ServerWriter implements Runnable {
    private final int chunkSize = 1024 * 1024 * 5;     //fixed chunk size
    private File file;      //the file
    private ConnectionUtility utility;      //bridge between server and client
    private InputStream fin;    //reading the file as byte array
    private byte buffer[];      //buffer to store the data read from the file
    private String fileId;
    private FileInfo fileInfo;

    ServerWriter(String fileId) {
        this.fileId = fileId;
        fileInfo = Server.files.get(fileId);
        //generating the directory name to get the file
        String createdFileName = "res/" + fileId;
        file = new File(createdFileName);
        buffer = new byte[chunkSize];
        utility = Server.receivers.get(fileInfo.getReceiverId());
    }


    @Override
    public void run() {
        if (utility != null && utility.isAlive()) {
            String x = "%#%";   //delimiter to tokenize the data
            String msg = x + fileInfo.getSenderId() + x + fileInfo.getFileName() + x + fileInfo.getFileSize();
            String outToClient = "s1000" + msg;
            utility.write(outToClient);
        }
        boolean flag = true;    //for terminating the loop from inside
        while (utility != null && utility.isAlive() && flag) {
            String inFromClient = utility.read().toString();
            String code = inFromClient.substring(0, 5);
            //String msg = inFromClient.substring(5);

            switch (code) {
                case "c1001":
                    file.delete();      //don't willing to receive file
                    Server.bufferUsed -= fileInfo.getFileSize();  //restoring the used buffer size of server
                    flag = false;
                    break;
                case "c1002":
                    try {
                        //willing to receive file; so opening the input stream
                        fin = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    //want to know the path where to save the file
                    String outToClient = "s1001" + "Path to save file";
                    utility.write(outToClient);
                    break;
                case "c1003":
                    try {
                        if (fin.available() < chunkSize) { //reading the last chunk
                            buffer = new byte[fin.available()];
                        }
                        int x = fin.read(buffer);
                        utility.write("s1002");  //writing the code and the chunk size
                        //String data = new String(buffer, 0, x);
                        utility.write(buffer);    //writing binary data

                        if (fin.available() == 0) {   //file exhausted
                            utility.write("s1003" + "File transfer completed");
                            break;

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "c1004":
                    try {
                        fin.close();
                        boolean x = file.delete();  //deleting the file after completing file transfer to the receiver
                        Server.bufferUsed -= fileInfo.getFileSize();  //restoring the servers buffer size
                        Server.files.remove(fileId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    flag = false;   //breaking out the loop; end of this thread
                    break;
                case "c1005":
                    try {
                        fin.close();
                        file.delete();
                        Server.bufferUsed -= fileInfo.getFileSize();  //restoring the servers buffer size
                        Server.files.remove(fileId);
                        utility.closeConnection();
                        flag = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

            }
        }
    }
}
