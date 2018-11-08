package Clients;

import Utilities.ConnectionUtility;
import Utilities.FrameCreator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

import static java.lang.System.exit;

//this class is for reading messages from server
public class ClientFileSender implements Runnable {
    private ConnectionUtility utility;
    private Scanner sc;
    private String studentId;

    enum State {
        STARTED,
        INTERMEDIATE,
        WILLING_TO_SEND_FILE,
        IDLE,
        RECEIVER_CHOSEN,
        FILE_TRANSFER_STARTED,
        FILE_TRANSFER_ENDED
    }

    private State state = State.STARTED;
    private byte window[][];
    private int chunkSize;
    private FileInputStream fin;
    private boolean corruption;
    private byte kind;
    private byte seqNo;
    private byte txNo;
    private boolean lostFrame;


    ClientFileSender(String studentId, ConnectionUtility utility) {
        this.studentId = studentId;
        this.utility = utility;
        this.utility.setSocketTimeOutOn(1000);
        sc = new Scanner(System.in);
        window = new byte[8][];
        corruption = false;
        seqNo = 0;
        txNo = 1;
        kind = 1;
        corruption = true;
        lostFrame = true;
    }


    @Override
    public void run() {
        boolean isReceiverOpen = false;
        String outToServer = "c0000" + studentId;
        utility.write(outToServer);
        boolean flag = true;
        while (flag) {
            Object object = utility.read();
            if (object.getClass() == Integer.class) {
                int errorCode = (int) object;
                if (errorCode == -2) {
                    if (state == State.IDLE) {
                        continue;
                    }
                    System.out.println("Server timed out!");
                    utility.write("c0007" + "log out");
                    utility.read();
                    utility.closeConnection();
                    exit(0);
                } else if (errorCode == -1) {
                    System.out.println("Server down");
                    System.exit(1);
                }
            } else if (object.getClass() != String.class){
                continue;   //discarding the remaining unnecessary acknowledgement frames
            }
            String inFromServer = object.toString();

            String code = inFromServer.substring(0, 5);
            String msg = inFromServer.substring(5);
            switch (code) {
                case "s0000":    //login successful
                    state = State.INTERMEDIATE;
                    break;

                case "s0001":     //login failed
                    break;

                case "s0002":    //receiver available
                    state = State.RECEIVER_CHOSEN;
                    break;

                case "s0003":    //receiver unavailable
                    break;

                case "s0004":    //fileId and chunk size is received
                    String msgs[] = msg.split("#%X@X%#");
                    String fileId = msgs[0];
                    chunkSize = Integer.parseInt(msgs[1]);
                    for (int i = 0; i < 8; i++) {
                        window[i] = new byte[chunkSize];
                    }
                    System.out.println(fileId + " " + chunkSize);
                    state = State.FILE_TRANSFER_STARTED;
                    break;

                case "s0005":    //not enough memory
                    break;

                case "s0006":
                    break;

                case "s0007":
                    state = State.WILLING_TO_SEND_FILE;
                    break;

                case "s0008":
                    state = State.IDLE;
                    break;

                case "s0009":   //
                    state = State.INTERMEDIATE;
                    break;
                default:
                    System.out.println();
                    System.out.println("Unpredictable error occurred");
            }
            System.out.println(msg);


            switch (state) {
                case STARTED:
                    System.out.println("Please enter your student Id:");
                    studentId = sc.next();     //getting the student id
                    outToServer = "c0000" + studentId;
                    utility.write(outToServer);
                    break;

                case INTERMEDIATE:
                    if (!isReceiverOpen) {
                        ConnectionUtility utility2 = new ConnectionUtility("localhost", 5000);
                        new Thread(new ClientFileReceiver(studentId, utility2)).start();
                        isReceiverOpen = true;
                    }
                    System.out.println("Want to send file: Y/n?");
                    String will = sc.next();
                    if (will.equals("y") || will.equals("Y")) {
                        utility.write("c0005" + "Willing to send file.");
                    } else if (will.toLowerCase().equals("quit")) {
                        utility.write("c0007" + "log out");
                        utility.closeConnection();
                        flag = false;
                    } else {
                        utility.write("c0006" + "Don't want to send file now.");
                    }

                    //System.out.println("Want to introduce lost frame error?");
                    break;

                case WILLING_TO_SEND_FILE:
                    System.out.println("Please enter the recipient Id: ");
                    String rId = sc.next();  //getting the receiver's id
                    rId = "c0001" + rId;
                    utility.write(rId);
                    break;

                case IDLE:

                    break;

                case RECEIVER_CHOSEN:
                    String fileName;
                    Long fileSize;
                    while (true) {
                        System.out.println("Please enter full path to the file: ");
                        String path = sc.next();
                        fileName = Paths.get(path).getFileName().toString();
                        fileSize = Paths.get(path).toFile().length();
                        try {
                            fin = new FileInputStream(path);
                            break;
                        } catch (FileNotFoundException e) {
                            System.out.println("File does not exists. :(");
                        }
                    }
                    System.out.println(fileName + " " + fileSize);
                    msg = "c0002" + fileName + "#%X@X%#" + fileSize; //#%X@X%# is added to split file name and size
                    utility.write(msg);
                    break;

                case FILE_TRANSFER_STARTED:
                    utility.write("c0003" + "File transfer started");
                    boolean ftGoing = true;
                    FrameCreator fc = new FrameCreator();
                    int ackNo = 0;
                    seqNo = 0;
                    while (ftGoing) {
                        try {
                            int x, i;
                            for (i = 0; i < 8; i++) {
                                if (fin.available() < chunkSize) {
                                    window[i] = new byte[fin.available()];
                                }
                                x = fin.read(window[i]);
                                System.out.println("Read " + x + " Available " + fin.available());
                                if (fin.available() == 0) {
                                    state = State.FILE_TRANSFER_ENDED;
                                    i++;    //for the 2nd loop
                                    ftGoing = false;
                                    break;
                                }
                            }
                            int j = 0, k = 0;
                            txNo = 1;
                            Random random = new Random();
                            while (true) {
                                for (; j < i; j++) {
                                    fc.setPayload(window[j]);
                                    seqNo++;
                                    if (corruption) {
                                        if (Math.abs(random.nextInt()) % 60 == 13) {
                                            fc.setCorrupted(true);
                                            System.out.println("Corrupted frame " + seqNo);
                                        }
                                    }
                                    fc.makeFrame(kind, seqNo, txNo);
                                    if (lostFrame) {
                                        if (Math.abs(random.nextInt()) % 70 == 31) {
                                            System.out.println("Skipping frame " + seqNo);
                                            continue;
                                        }
                                    }
                                    utility.write(fc.getFrame());
                                    System.out.printf("Chunk %d is sent. Transmission No: %d\n", seqNo, txNo);
                                    //utility.write(window[j]);
                                }

                                System.out.println("Reading acknowledgements");
                                //reading acknowledgements
                                j = k;
                                for (; j < i; j++) {
                                    Object obj = utility.read();
                                    if (obj.getClass() == Integer.class) {
                                        if ((int) obj == -2) {
                                            j = ackNo % 8;
                                            k = j;
                                            txNo++;
                                            seqNo = (byte) (ackNo);
                                            System.out.printf("Timed Out!!! SeqNo: %d TxNo: %d\n", seqNo + 1, txNo - 1);
                                            break;
                                        }
                                    }
                                    fc.setFrame((byte[]) obj);
                                    fc.makePayload();
                                    int rackNo = fc.getSeqNo();
                                    if (ackNo + 1 != rackNo) {
                                        //TODO
                                        System.out.printf("Ack expected %d received %d\n", ackNo + 1, rackNo);
                                        j--;
                                    } else {
                                        ackNo++;
                                        System.out.println("Ack: " + rackNo + " TxNo: " + fc.getTxNo());
                                    }
                                    //System.out.println("Acknowledgement: " + utility.read().toString());
                                }
                                if (j == i) {
                                    break;
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //break;

                case FILE_TRANSFER_ENDED:
                    utility.write("c0004" + "Object sent completely!");
                    //state = State.INTERMEDIATE;
                    try {
                        fin.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("WIT!!!!");
            }
        }
    }
}
