package Server;

import Utilities.ConnectionUtility;
import Utilities.FileInfo;
import Utilities.FrameCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static java.lang.Thread.sleep;

public class ServerReader implements Runnable {
    private ConnectionUtility utility;
    private String clientId;
    private FileOutputStream outputStream;
    private File file;
    private FileInfo fileInfo;
    private boolean isComplete = false;
    private FrameCreator fc;

    ServerReader(ConnectionUtility utility) {
        this.utility = utility;
        fileInfo = new FileInfo();
        fc = new FrameCreator();
    }

    @Override
    public void run() {
        boolean flag = true;
        while (utility.isAlive() && flag) {
            System.out.println("Waiting for msg");
            Object object = utility.read();
            if (object.getClass() == Integer.class) {
                if ((int) object == -1) {
                    cleanup();
                    break;
                }
            }
            String inFromUser = object.toString();
            String code = "";
            String msg = "";
            System.out.println(inFromUser);
            if (inFromUser.length() >= 5) {
                code = inFromUser.substring(0, 5);
                msg = inFromUser.substring(5);
            }
            switch (code) {
                case "c0000":    //login confirmation
                    clientId = msg;
                    if (Server.senders.containsKey(clientId)) {     //id already logged in
                        String ip = utility.getSocket().getRemoteSocketAddress().toString();//this addresss
                        ConnectionUtility utility2 = Server.senders.get(clientId);
                        String sIP = utility2.getSocket().getRemoteSocketAddress().toString();//already logged in users address
                        if (!isSameMachine(ip, sIP)) {
                            utility.write("s0001" + "Same student, different ip!!!");
                        } else {
                            //creating directory to store the files
                            /*String folder = "res/" + clientId;
                            Path path = Paths.get(folder);
                            try {
                                Files.createDirectories(path);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/
                            utility.write("s0000" + "Login confirmed");
                        }

                    } else {
                        //creating directory to store the files
                        /*String folder = "res/" + clientId;
                        Path path = Paths.get(folder);
                        try {
                            Files.createDirectories(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        Server.senders.put(clientId, utility);
                        utility.write("s0000" + "Login confirmed");
                    }
                    break;
                case "c0001":      //getting the receiver id
                    fileInfo.setReceiverId(msg);
                    if (Server.senders.containsKey(msg)) {
                        utility.write("s0002" + "Receiver available");
                    } else {
                        utility.write("s0003" + "Receiver unavailable");
                    }
                    break;
                case "c0002":     // getting the file name and size
                    String fileDetails[] = msg.split("#%X@X%#");
                    fileInfo.setSenderId(clientId);
                    fileInfo.setFileName(fileDetails[0]);
                    fileInfo.setFileSize(new Long(fileDetails[1]));
                    System.out.println(fileInfo.getFileName() + " " + fileInfo.getFileSize());
                    if (Server.bufferUsed + fileInfo.getFileSize() < Server.bufferSize) {
                        Server.bufferUsed += fileInfo.getFileSize();
                        //getting the fileId and chunkSize from server
                        String fileId = Server.getFileId();
                        int chunkSize = Server.getChunkSize((int) (fileInfo.getFileSize()));
                        fileInfo.setFileId(fileId);

                        String createdFileName = "res/" + fileInfo.getFileId();
                        file = new File(createdFileName);
                        try {
                            if (!file.createNewFile()) {
                                file.delete();
                                file.createNewFile();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream = new FileOutputStream(createdFileName, true);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        System.out.println("s0004" + fileId + "#%X@X%#" + chunkSize);
                        Server.files.put(fileId, fileInfo);
                        utility.write("s0004" + fileId + "#%X@X%#" + chunkSize);
                    } else {
                        utility.write("s0005" + "Not enough memory");
                    }

                    break;
                case "c0003":  //receiving file
                    int seqNo = 0;
                    byte kind = 2, txNo;
                    while (true) {
                        try {
                            Object obj = utility.read();
                            if (obj.getClass() == String.class) {
                                //file transfer complete message is received here
                                break;
                            }
                            byte[] buffer = (byte[]) obj;
                            fc.setFrame(buffer);
                            boolean b = fc.makePayload();
                            if (!b) {
                                System.out.printf("Corrupted frame %d. Checksum verification failed\n", fc.getSeqNo());
                                continue;
                            }
                            if (fc.getSeqNo() > seqNo + 1) {
                                System.out.printf("Lost frame %d; Discarding frame %d\n", seqNo + 1, fc.getSeqNo());
                                continue;
                            } else if (fc.getSeqNo() == seqNo + 1) {
                                System.out.println("Received frame " + fc.getSeqNo() + " TxNo: " + fc.getTxNo());
                                seqNo++;
                                buffer = fc.getPayload();
                                outputStream.write(buffer);
                                outputStream.flush();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Random random = new Random();
                        int x = Math.abs(random.nextInt(102));
                        try {
                            System.out.println("Sleeping for " + x / 100.0 + " seconds");
                            sleep(x * 10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        txNo = fc.getTxNo();
                        byte ackNo = fc.getSeqNo();
                        if (Math.abs(random.nextInt(19)) == 7) {
                            System.out.printf("Ack for %d; txNo: %d is lost\n", ackNo, txNo);
                            continue;
                        }
                        fc.setPayload(new byte[0]);
                        fc.makeFrame(kind, ackNo, txNo);
                        utility.write(fc.getFrame());
                        System.out.printf("Ack for %d; txNo: %d\n", ackNo, txNo);
                        //utility.write("s0006" + " Received chunk " + fc.getSeqNo());
                    }
                    //break;
                case "c0004":
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (file.length() != fileInfo.getFileSize()) {
                        file.delete();
                        Server.bufferUsed -= fileInfo.getFileSize();
                        Server.files.remove(fileInfo.getFileId());
                        utility.write("s0009" + "File is not completely received.");
                        System.out.println("File is not completely received");
                    } else {
                        isComplete = true;
                        utility.write("s0009" + "File is completely received.");
                        System.out.println("File is completely received");
                        Thread thread = new Thread(new ServerWriter(fileInfo.getFileId()));
                        thread.start();
                    }
                    break;
                case "c0005":
                    utility.write("s0007" + "Ok, you can send file!");
                    break;
                case "c0006":
                    utility.write("s0008" + "Ok, you can wait.");
                    break;
                case "c0007":
                    cleanup();
                    flag = false;
                    break;

                case "c1000":
                    if (!Server.receivers.containsKey(msg)) {
                        clientId = msg;
                        Server.receivers.put(clientId, utility);
                        flag = false;
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private boolean isSameMachine(String ip1, String ip2) {
        return ip1.equals(ip2);
    }

    private void cleanup() {
        if (fileInfo.getFileId() != null) {
            Server.bufferUsed -= fileInfo.getFileSize();
            Server.files.remove(fileInfo.getFileId());
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if (file != null && !isComplete) {
            file.delete();
            Server.files.remove(fileInfo.getFileId());
        }

        if (clientId != null) {
            Server.senders.remove(clientId);
            Server.receivers.get(clientId).write("s1004");

            Server.receivers.remove(clientId);
        }
        if (utility.isAlive()) {
            utility.closeConnection();
        }


        System.out.println("Client " + clientId + " is closing!");
    }
}
