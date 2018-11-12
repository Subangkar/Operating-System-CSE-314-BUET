package util;

import java.io.*;
import java.util.Random;

import static java.lang.Thread.sleep;

public class FileSender implements Runnable {
    public ConnectionUtillities connectionUtillities;
    public FileInfo fileInfo;
    public int expiredSession = 10000;
    private int[] acknowledgeStatus = new int[8];

    public int getAcknowledgeStatus(int i) {
        return acknowledgeStatus[i];
    }

    public void setAcknowledgeStatus(int i) {
        this.acknowledgeStatus[i] = 1;
    }

    public ConnectionUtillities getConnectionUtillities() {
        return connectionUtillities;
    }

    public void setConnectionUtillities(ConnectionUtillities connectionUtillities) {
        this.connectionUtillities = connectionUtillities;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public FileSender(ConnectionUtillities connectionUtillities, FileInfo fileInfo) {

        this.connectionUtillities = connectionUtillities;
        this.fileInfo = fileInfo;
        for(int i = 0; i < 8; i++)  {
            acknowledgeStatus[i] = 0;
        }
    }

    public void sendFile() throws Exception {
        FileInputStream fis;
        fis = new FileInputStream(new File(fileInfo.getFileName()));
        int dummySet = -1;

        try {
            byte[][] bufferArray = new byte[8][fileInfo.getChunkSize()];
            // remaining is the number of bytes to read to fill the buffer

            int[] remaining = new int[8];
            int[] read = new int[8];
            for(int i = 0; i < 8; i++)  {
                remaining[i] = bufferArray[i].length;
                read[i] = 0;
            }
            System.out.println("byte[] buffer has a size of " + fileInfo.getChunkSize() + " bytes.");

            System.out.println("FileName : " + fileInfo.getFileName() + " fileSize : " + fileInfo.getFileSize() +
                    " fileID : " + fileInfo.getFileID() + " ChunkSize : " + fileInfo.getChunkSize() +
                    " noOfChunks : " + fileInfo.getNoOfChunks() + " lastChunkSize :" + fileInfo.getLastChunkSize());

            // block number is incremented each time a block of ChunkSize is read and written

            int blockNumber = 1;
            for(int control = 0; 8*control <= fileInfo.getNoOfChunks(); control++){
                for(int i = 0; i < 8; i++)  {
                    if(blockNumber > fileInfo.getNoOfChunks())  break;

                    if(dummySet != -1 ) {
                        i = dummySet;
                        System.out.println("Frame " + blockNumber + " will be sent now");
                        sleep(1000);
                    }
                    if(dummySet == -1)  {
                        try {
                            read[i] = fis.read(bufferArray[i], bufferArray[i].length - remaining[i], remaining[i]);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        System.out.println("Buffer has a size of " + bufferArray[i].length + " bytes.");
                    }
                    if (read[i] >= 0) {                                 // some bytes were read
                        if(dummySet == -1)  {
                            remaining[i] -= read[i];
                        }
                        if (remaining[i] == 0) {                        // the buffer is full
                            if(!iRandomLostFrame())    {
                                doAllStuff((8*control + i+ 1), bufferArray[i]);
                            }   else    {
                                System.out.println("Frame " + 8*control + i+ 1 + " is lost by the RandomLost function");
                            }

                        //    remaining[i] = bufferArray[i].length;
                        }
                    } else {
                        System.out.println("End of file is reached");
                        if (remaining[i] < bufferArray[i].length) {                    // they are written to the last output file
                            doAllStuff((8*control + i+ 1), bufferArray[i]);

                        //    remaining[i] = bufferArray[i].length;
                        }
                        System.out.println("Will break the loop");
                        break;
                    }
                }
                if(blockNumber <= fileInfo.getNoOfChunks())  {

                    int elapsedTime = 0;
                    long startTime = System.currentTimeMillis();
                    while (elapsedTime < expiredSession)    {
                        int temp = 1;
                        for(int i = 0; i < 8; i++)   {
                            System.out.println("acknowledgeStatus[" + i + "] = " + acknowledgeStatus[i]);
                            if(8*control + i < fileInfo.getNoOfChunks())    {
                                System.out.print("temp = temp * acknowledgeStatus[i]  " + temp + " * " + acknowledgeStatus[i]);
                                temp = temp * acknowledgeStatus[i];
                                System.out.println(" = " + temp);
                            }
                        }
                        if(temp == 1)   {
                            System.out.println("Temp is 1, all okay");
                         //   blockNumber += 8;
                            dummySet = -1;
                            break;
                        }
                        elapsedTime = (int) (System.currentTimeMillis()- startTime);
                        sleep(1200);
                    }
/*
                    int p = dummySet < 0 ? 0 : dummySet;
                    for(    ; p < 8; p++)  {
                        if(acknowledgeStatus[p] == 0)   {
                            break;
                        }
                        else{
                            blockNumber ++;
                        }
                    }
*/

                    if(elapsedTime < expiredSession)    {
                        for(int i = 0; i < 8; i++)  {
                            if(acknowledgeStatus[i] == 1)   blockNumber++;
                            acknowledgeStatus[i] = 0;
                            remaining[i] = bufferArray[i].length;
                            read[i] =0;
                        }
                        dummySet = -1;
                        System.out.println("All frames in this iteration are received successfully");
                    }
                    else    {
                        int check = dummySet < 0 ? 0 : dummySet;
                        for(int i = check; i < 8; i++)  {
                            if(acknowledgeStatus[i] == 0)   {
                                dummySet = i;
                                control--;
                                break;
                            }
                            else    {
                                remaining[i] = bufferArray[i].length;
                                read[i] = 0;
                                blockNumber++;
                            }
                        }
                    }
                    System.out.println("by far " + (blockNumber -1) + " frames are accepted");
                }
                System.out.println(blockNumber-1 + " frames are received successfully, frame " + blockNumber + " will be sent now");
            }
            System.out.println("Block Number is : " + blockNumber + " That's one more than noOfChunks");
            System.out.println("We are done with creating chunks");
        } finally {
            fis.close();
        }
    }

    private void doAllStuff(int blockNumber, byte[] buffer)   {
        System.out.println("Main data : ");
        for(byte b : buffer)    {
            String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            System.out.print(s1);
        }
        byte[] bitStuffed = stuffBit(buffer);
        System.out.println("After bit stuffing :");
        for(byte b : bitStuffed)    {
            String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            System.out.print(s1);
        }
        System.out.println();
        System.out.println("After bit stuffing size of buffer is ; " + bitStuffed.length + " bytes");
        byte checkSum = calculateCheckSum(buffer);
        System.out.println("Checksum string is : " + String.format("%8s", Integer.toBinaryString(checkSum & 0xFF)).replace(' ', '0'));

        byte[] finalData = makeFrame((byte) blockNumber, bitStuffed);
        System.out.println("Frame " + blockNumber + " we want to send is : ");
        for(byte b : finalData)
            System.out.print(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        System.out.println();

        connectionUtillities.write(finalData);
    }

    private boolean iRandomLostFrame()  {
        int a, b, c;
        boolean result = true;
        Random random = new Random();
        a = random.nextInt(1000);
        b = random.nextInt(1000);
        c = random.nextInt(1000);
        if(a*c > b)   result = false;
        return result;
    }

    private byte[] makeFrame(byte seqNo, byte[] data)   {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte frameByte = 0x7e;
        byteArrayOutputStream.write(new byte[]{frameByte}, 0, 1);   //header of frame
        frameByte = (byte) 0xff;
        byteArrayOutputStream.write(new byte[]{frameByte}, 0, 1);   //marking frame as data frame
        byteArrayOutputStream.write(new byte[]{seqNo}, 0, 1);       //putting seqNo
        frameByte = 0x00;
        byteArrayOutputStream.write(new byte[]{frameByte}, 0, 1);   //acknowlegement is 0
        byteArrayOutputStream.write(data, 0, data.length);              //stuffed payload
        frameByte = calculateCheckSum(data);
        byteArrayOutputStream.write(new byte[]{frameByte}, 0, 1);   //checksum
        frameByte = 0x7e;
        byteArrayOutputStream.write(new byte[]{frameByte}, 0, 1);   //trailer
        byte[] b = byteArrayOutputStream.toByteArray();
        return  b;
    }

    private byte[] stuffBit(byte[] buffer) {
        System.out.println();
        int count = 0;
        int bitCount = 0;
        byte result = 0x00;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        for(int i = 0; i < buffer.length; i++)  {
            byte b = buffer[i];
            byte mask = (byte) 0x80;
            for(int j = 0; j < 8; j++)  {
            //    System.out.println("Mask value is now : " + (int) mask + " Byte we are reading is : " + (int) b + " Result is now : " + result);
                if(bitCount == 8)   {
                    byteArrayOutputStream.write(new byte[]{result}, 0, 1);
                    bitCount = 0;
                    result = 0x00;
                }
                boolean value = (b & mask) != 0;
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
                    result <<= 1;
                    count = 0;
                    bitCount++;
                }
            }
        }
        byteArrayOutputStream.write(new byte[]{result}, 0, 1);
        byte b[] = byteArrayOutputStream.toByteArray();
        return b;
    }

    private byte calculateCheckSum(byte[] buffer) {
        byte result = 0x00;
        for(int i = 0; i < buffer.length; i++)  {
            byte b = buffer[i];
            byte mask = (byte) 0x80;
            for(int j = 0; j < 8; j++)  {
                boolean value = (b & mask) != 0;
                if(value)   {
                    result = (byte) (result ^ (1 << (7 - j)));
                //    System.out.println("Checksum string is : " + String.format("%8s", Integer.toBinaryString(result & 0xFF)).replace(' ', '0'));
                }
                mask >>= 1;
                mask = (byte) (mask & 0x7f);
            }
        }
        return result;
    }

    @Override
    public void run() {
        try {
            this.sendFile();
        } catch (Exception e) {
          //  System.out.println("Exception in fileSender : " + e.toString());
        }
    }
}
