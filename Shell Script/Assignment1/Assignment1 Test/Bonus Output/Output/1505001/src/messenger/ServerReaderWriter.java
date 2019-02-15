/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package messenger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import util.ConnectionUtillities;
import util.FileInfo;

/**
 *
 * @author uesr
 */
public class ServerReaderWriter implements Runnable{

    public HashMap<String,Information> clientList;
    public ConnectionUtillities connection;
    private String user;
    private FileInfo fileInfo;
    private long maxBufferSize = 10485760L;
    private long remainingBufferSize;
    private long receivedFileSize;
    private byte checkSumByte;
    private int[] acknowledgeStatus;
    
    public ServerReaderWriter(String username, ConnectionUtillities con, HashMap<String, Information> list){
        connection=con;
        clientList=list;
        user=username;
        remainingBufferSize = maxBufferSize;
        acknowledgeStatus = new int[8];
        for(int i = 0; i < 8; i++)  {
            acknowledgeStatus[i] = 0;
        }
    }
    
    @Override
    public void run() {
        while(true){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Object o=connection.read();
            String data = "";
            if(o instanceof String) {
                data=o.toString();
                System.out.println(data);
            }

            if(data.equalsIgnoreCase("no") || data.equalsIgnoreCase("yes")) {
                connection.write(data);
                remainingBufferSize += receivedFileSize;
            }
            else  if(data.contains(":")){
                String msg[]=data.split(":",2);
                String username=msg[0];
                String msgInfo=msg[1];

                if(clientList.containsKey(username)){
                    fileInfo = new FileInfo(msgInfo, new File(msgInfo).length());

                    if(fileInfo.getFileSize() > maxBufferSize)   {
                        connection.write("File size exceeds maximum buffer size. File can not be transmitted.");
                        System.out.println("File size exceeds maximum buffer size. File can not be transmitted.");
                    }

                    else    {

                        Random rand = new Random();
                        int fileID;
                        fileID = rand.nextInt(100);

                        fileInfo.setFileID(fileID);
                        int chunkSize = rand.nextInt(5555);
                        fileInfo.setChunkSize(chunkSize);
                        int add = 0;
                        if(fileInfo.getFileSize()%chunkSize != 0){ add = 1;}
                        fileInfo.setNoOfChunks((int)(fileInfo.getFileSize()/chunkSize + add));
                        fileInfo.setLastChunkSize();
                        System.out.println("FileName : " + fileInfo.getFileName() +" fileSize : " + fileInfo.getFileSize() +
                                " fileId : " + fileInfo.getFileID() + " chunkSize : " + fileInfo.getChunkSize() + " noOfChunks : "
                                + fileInfo.getNoOfChunks() + " lastChunkSize : " + fileInfo.getLastChunkSize());

                        connection.write("Chunk is set");
                        connection.write(fileInfo);
                        connection.write("Start sending the file now");

                        receivedFileSize = 0L;
                        byte[] payload;
                        byte seqByte, ackByte;
                        seqByte = 0x00;
                        for(int control = 0; 8 * control <= fileInfo.getNoOfChunks(); control++)
                        {
                            while(true)
                            {
                                byte[] buffer = (byte[]) connection.read();

                                if((int) buffer[2] == (int)seqByte+1) {
                                    System.out.println("Sequence number has matched, it is = " + buffer[2]);
                                }   else    {
                                    System.out.println("Sequence number of received farme is : " + buffer[2]);
                                }

                                if((int) buffer[0] == 126 && (int) buffer[buffer.length -1] == 126)   {
                                    System.out.println("Header and Trailer are matched");
                                    if((int) buffer[1] == -1)    {
                                        System.out.println("Frame contains data");
                                        if ((int) buffer[3] == 0) {
                                            System.out.println("Acknowledgement byte matched");

                                            checkSumByte = buffer[buffer.length - 2];
                                            String stuffedData = "", receivedFrame = "";
                                            for(byte b : buffer)    {
                                                stuffedData += String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                                            }
                                            System.out.println("Full data from buffer is :\n" + stuffedData);
                                            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                                            byteArray.write(buffer, 4, buffer.length - 6);
                                            payload = byteArray.toByteArray();

                                            for(byte b : payload)    {
                                                receivedFrame += String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                                            }
                                            System.out.println("Stuffed payload in receiver side is :\n" + receivedFrame);
                                            System.out.println("Stuffed payload is " + payload.length + " bytes");
                                            payload = deStuffBit(payload);
                                            receivedFrame = "";
                                            for(byte b : payload)    {
                                                receivedFrame += String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                                            }
                                            System.out.println("After destuffing payload is :\n" + receivedFrame);
                                            System.out.println("After destuffing payload is " + payload.length + " bytes");
                                            if(verifyCheckSum(payload, checkSumByte))   {
                                                connection.write("Acknowledged");
                                                System.out.println("Acknowledged frame index " + (buffer[2] -1)%8);
                                                seqByte = (byte) ((int)seqByte + 1);
                                                acknowledgeStatus[(buffer[2] -1)%8] += 1;
                                                connection.write(makeFrame(seqByte, acknowledgeStatus[seqByte - 1] %8));
                                                //ekhane frame ta pathate hobe
                                                connection.write("Checksum byte has matched, frame " + (int) seqByte + " is accepted.");
                                                byteArrayOutputStream.write(payload, 0, payload.length);

                                                if((int)seqByte < fileInfo.getNoOfChunks()) {
                                                    remainingBufferSize -= buffer.length;
                                                    receivedFileSize += buffer.length;
                                                }
                                                else    {
                                                    remainingBufferSize -= fileInfo.getLastChunkSize();
                                                    receivedFileSize += fileInfo.getLastChunkSize();
                                                    System.out.println("Remaining Buffer after this iteration : " + remainingBufferSize);
                                                    System.out.println("Received file size is : " + receivedFileSize);
                                                    System.out.println("fileInfo says , file size is : " + fileInfo.getFileSize());
                                                    connection.write("We are done");
                                                    break;
                                                /*    if(receivedFileSize == fileInfo.getFileSize()) {

                                                    }   */
                                                }
                                                if((int)seqByte % 8 == 0)   {
                                                    for(int i = 0; i < 8; i++)  {
                                                        acknowledgeStatus[i] = 0;
                                                    }
                                                    System.out.println("next iteration can be started");

                                                    break;
                                                }
                                            }
                                            else    {
                                                connection.write("Checksum byte has errors, frame " + ((int) seqByte + 1) + " is discarded.");
                                            }
                                        }
                                        else    {
                                            System.out.println("Acknowledgement byte mismatch");
                                        }
                                    }
                                    else    {
                                        System.out.println("Data frame code error detected");
                                    }
                                }
                                else    {
                                    System.out.println("Frame delimeter mismatch problem");
                                }
                            }
                        }
                        Information info=clientList.get(username);
                        info.connection.write(user+" - "+msgInfo);
                        info.connection.write("Do you want to receive " + fileInfo.getFileName() + " from client " + user
                                + " of size " + fileInfo.getFileSize() + " bytes?");
                        info.connection.write(fileInfo);
                        byte[] buffer = byteArrayOutputStream.toByteArray();
                        info.connection.write(buffer);
                        remainingBufferSize += receivedFileSize;
                    }

                } else  {
                    connection.write(username+" is currently offline.");
                }
            }
        }
    }

    private byte[] makeFrame(byte seqNo, int ackNo)   {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte frameByte = 0x7e;
        byteArrayOutputStream.write(new byte[]{frameByte}, 0, 1);   //header of frame
        frameByte = (byte) 0x00;
        byteArrayOutputStream.write(new byte[]{frameByte}, 0, 1);   //marking frame as data frame
        byteArrayOutputStream.write(new byte[]{seqNo}, 0, 1);       //putting seqNo
        frameByte = (byte) ackNo;
        byteArrayOutputStream.write(new byte[]{frameByte}, 0, 1);   //acknowledgement is set

        frameByte = 0x7e;
        byteArrayOutputStream.write(new byte[]{frameByte}, 0, 1);   //trailer
        byte[] b = byteArrayOutputStream.toByteArray();
        return  b;
    }

    private boolean verifyCheckSum(byte[] buffer, byte checkSum) {
        byte result = 0x00;
        for(int i = 0; i < buffer.length; i++)  {
            byte b = buffer[i];
            byte mask = (byte) 0x80;
            for(int j = 0; j < 8; j++)  {
                boolean value = (b & mask) != 0;
                if(value)   {
                    result = (byte) (result ^ (1 << (7 - j)));
                }
                mask >>= 1;
                mask = (byte) (mask & 0x7f);
            }
        }
        boolean value = false;
        if((int) result == (int) checkSum)  value = true;
        return value;
    }

    private byte[] deStuffBit(byte[] buffer)
    {
        int count = 0;
        int bitCount = 0;
        byte result = 0x00;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        for(int i = 0; i < buffer.length; i++)  {
            byte mask = (byte) 0x80;
            for(int j = 0; j < 8; j++)  {
                //    System.out.println("Mask value is now : " + (int) mask + " Byte we are reading is : " + (int) b + " Result is now : " + result);
                if(bitCount == 8)   {
                    byteArrayOutputStream.write(new byte[]{result}, 0, 1);
                    bitCount = 0;
                    result = 0x00;
                }
                boolean value = (buffer[i] & mask) != 0;
                result <<= 1;
                if(value)   {
                    result |= 1;
                    count++;
                }
                else    {
                    count = 0;
                }
                bitCount++;
                //    System.out.println("Value is : " + value + " Result is : " + result + " bitCount is : " + bitCount + " count is : " +  count);
                mask >>= 1;
                mask = (byte) (mask & 0x7f);

                if(count == 5)  {
                    j++;
                    mask >>= 1;
                    mask = (byte) (mask & 0x7f);
                    count = 0;
                }
            }
        }
        if(bitCount == 8)
            byteArrayOutputStream.write(new byte[]{result}, 0, 1);
        return byteArrayOutputStream.toByteArray();
    }
}
