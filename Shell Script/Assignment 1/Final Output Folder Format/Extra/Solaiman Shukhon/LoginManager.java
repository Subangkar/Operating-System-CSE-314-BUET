import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;

/**
 * Created by Mrinmoy Roddur on 9/20/2017.
 */
public class LoginManager {

    class UserInfo{
        InetAddress addr;
        int port;
        SocketChannel channel;

        UserInfo(InetAddress a, int p, SocketChannel sc){
            addr=a;
            port=p;
            channel=sc;
        }
    }

    private static LoginManager instance = new LoginManager();

    private Hashtable<String, UserInfo> users_online = new Hashtable<>();

    public static LoginManager getInstance() {
        return instance;
    }

    boolean login(String roll, InetAddress addr, int port, SocketChannel s) {
        if (!users_online.containsKey(roll)) {
            users_online.put(roll, new UserInfo(addr,port,s));
            return true;
        } else return (users_online.get(roll).equals(new UserInfo(addr,port,s)));
    }

    boolean isOnline(String roll) {
        return users_online.containsKey(roll);
    }

    void remove(SocketChannel s){
        users_online.remove(new UserInfo(s.socket().getInetAddress(), s.socket().getLocalPort(), s));
    }

    SocketChannel returnChannel(String roll){
        return users_online.get(roll).channel;
    }
}

class FileManager {

    class FileTask {
        String sender;
        String receiver;
        String fileName;
        int fileSize;
        int maxSize;
        int transmittedChunk;
        int chunkNum;
        Path chunks;

        FileTask(String s, String r, String f, int fs) {
            sender = s;
            receiver = r;
            fileName = f;
            fileSize = fs;
            Random rand = new Random();
            maxSize = 32;
            transmittedChunk = 0;
            chunkNum = fileSize / maxSize;
            chunkNum += (fileSize % maxSize != 0) ? 1 : 0;
            //chunks = new byte[chunkNum][];
            chunks=Paths.get("C:\\Users\\mrinm\\Desktop\\Server\\"+f);
            try {
                Files.createFile(chunks);
            } catch (FileAlreadyExistsException e) {
                try {
                    Files.delete(chunks);
                    Files.createFile(chunks);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static FileManager instance = new FileManager();

    private Hashtable<Integer, FileTask> tasks = new Hashtable<>();

    int fileProcessed = 0;
    static final int BUFFER_SIZE =10*1024*1024;
    int usedBuffer=0;

    public static FileManager getInstance() {
        return instance;
    }

    byte[] addFile(String s, String r, String f, int fs) {
        if(usedBuffer+fs<BUFFER_SIZE) {
            usedBuffer+=fs;
            FileTask ft = new FileTask(s, r, f, fs);
            tasks.put(fileProcessed, ft);
            String temp = Integer.toString(ft.maxSize);
            String temp2 = Integer.toString(fileProcessed++);
            byte[] data = new byte[5 + temp.length() + temp2.length()];
            System.arraycopy((temp + temp2).getBytes(), 0, data, 5, data.length - 5);
            data[0] = Server.FILE_INFO;
            data[1] = (byte) temp.length();
            data[2] = (byte) temp2.length();
            return data;
        } else {
            byte[] error={Server.FILE_ERROR, 0};
            return error;
        }
    }

    boolean addChunk(int fileId, byte[] chunk) throws IOException {
        FileTask s = tasks.get(fileId);
        s.transmittedChunk++;
        chunk=Arrays.copyOfRange(chunk,0,41);
        System.out.println("Frame "+s.transmittedChunk+" : (Stuffed)");
        StuffingModule.printBitArray(chunk);
        chunk=StuffingModule.destuffBit(chunk);
        //if (s.transmittedChunk == s.chunkNum){
        //    chunk=Arrays.copyOfRange(chunk, 0, s.fileSize % s.maxSize);
        //} else chunk=Arrays.copyOfRange(chunk, 0, s.maxSize);
        System.out.println("Frame "+s.transmittedChunk+" : (Destuffed)");
        StuffingModule.printBitArray(chunk);
        System.out.println("Sequence Number :" +chunk[1]);
        byte[] payload=new byte[chunk.length-2];
        System.arraycopy(chunk,2,payload,0,payload.length);
        System.out.println("Checksum Error result : "+StuffingModule.hasChecksumError(payload));
        if(StuffingModule.hasChecksumError(payload)==true){
            s.transmittedChunk--;
            throw new IOException("hasChecksumError");
        }
        payload=Arrays.copyOfRange(payload,0,payload.length-1);
        System.out.println("Frame "+ s.transmittedChunk+" : (Original Payload)");
        StuffingModule.printBitArray(payload);
        try {
            Files.write(s.chunks ,payload, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("Acknowledgement frame");

        if (s.transmittedChunk == s.chunkNum) return true;
        return false;
    }

    void sendFile(int fileId, SocketChannel receiverChannel){
        FileTask s=tasks.get(fileId);
        try {
            FileChannel fchannel = FileChannel.open(s.chunks);
            fchannel.transferTo(0, s.fileSize,receiverChannel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean checkFile(int fileId){
        FileTask s = tasks.get(fileId);
        try {
            if(Files.size(s.chunks)==s.fileSize) return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    void deleteFile(int fileId){
        FileTask s=tasks.get(fileId);
        try {
            Files.delete(s.chunks);
        } catch (IOException e) {
            e.printStackTrace();
        }
        usedBuffer-=s.fileSize;
        tasks.remove(fileId);
    }



    String getReceiver(int fileId){
        return tasks.get(fileId).receiver;
    }

    String getFileName(int fileId){
        return tasks.get(fileId).fileName;
    }

    int getFileSize(int fileId){
        return tasks.get(fileId).fileSize;
    }

    String getSender(int fileId){ return tasks.get(fileId).sender; }




}

/*byte[] addChunk(int fileId, byte[] chunk) {
        FileTask s = tasks.get(fileId);
        chunk=Arrays.copyOfRange(chunk, 0, s.maxSize);
        s.chunks[s.transmittedChunk++] = chunk;
        if (s.transmittedChunk == s.chunks.length) {
            byte[] arr = new byte[s.fileSize % s.maxSize];
            System.arraycopy(s.chunks[s.transmittedChunk - 1], 0, arr, 0, s.fileSize % s.maxSize);
            int k = 0;
            s.chunks[s.transmittedChunk - 1] = arr;
            arr = new byte[s.fileSize];
            for (int i = 0; i < s.chunks.length; i++) {
                for (int j = 0; j < s.chunks[i].length; j++)
                    arr[k++] = s.chunks[i][j];
            }
            //byte[] temp=new byte[5+arr.length];
            //System.arraycopy(arr,0,temp,5,arr.length);
            //temp[0]=Server.REQUEST;

            return arr;

        }
        return new byte[0];
    }*/


class StuffingModule {

    private static int getBit(int n, byte[] message) {
        return ((message[n / 8] & 1 << (7 - n % 8)) == 0) ? 0 : 1;
    }

    private static void setBit(int n, int bit, byte[] message) {
        switch (bit) {
            case 1: {
                message[n / 8] |= 1 << (7 - n % 8);
                break;
            }
            case 0: {
                message[n / 8] &= ~(1 << (7 - n % 8));
                break;
            }
        }
    }

    private static void setByte(int firstBit, byte b, byte[] message) {
        for (int i = 0; i < 8; i++)
            setBit(firstBit + i, ((b & (1 << (7 - i))) == 0) ? 0 : 1, message);
    }

    public static byte[] bitStuff(byte[] message) {
        int counter = 0;
        byte[] stuffedMsg = new byte[message.length + ((message.length * 8) / 5 +1) / 8 + 3];
        stuffedMsg[0]='~';
        int j=8;
        for (int i = 0; i < message.length * 8; i++, j++) {
            int bit = getBit(i, message);
            setBit(j, bit, stuffedMsg);
            if (bit == 1) {
                counter++;
            } else counter = 0;
            if (counter == 5) {
                setBit(++j, 0, stuffedMsg);
                counter = 0;
            }
        }
        setByte(j,(byte)'~',stuffedMsg);
        return stuffedMsg;

    }

    public static byte[] destuffBit(byte[] message) {
        int counter = 0,  j = 0;
        byte[] stuffedMsg = new byte[message.length-2];
        if(message[0]!='~') return null;
        for (int i = 8; i < message.length * 8; i++, j++) {
            int bit = getBit(i, message);
            setBit(j, bit, stuffedMsg);
            if (bit == 1) {
                counter++;
            } else counter = 0;
            if (counter == 5) {
                if(getBit(++i,message)==1) break;
            }
        }
        byte[] destuffed=new byte[j/8];
        System.arraycopy(stuffedMsg,0,destuffed,0,destuffed.length);
        return destuffed;
    }

    public static void printBit(byte n) {
        for (byte i = (byte) 0x80; i != 0; i = (byte) ((i & 0xff) >>> 1)) {
            System.out.print((((n & i) == 0) ? 0 : 1) + " ");
        }
        System.out.print(" ## ");
    }

    public static void printBitArray(byte[] n){
        for(byte i:n)printBit(i);
        System.out.println();
    }

    public static int checksum(byte[] payload) {
        int counter = 0;
        for (int i = 0; i < payload.length * 8; i++) {
            if (getBit(i, payload) == 1) counter++;
        }
        return counter;
    }

    public static boolean hasChecksumError(byte[] payload) {
        int counter = 0;
        for (int i = 0; i < (payload.length * 8 - 8); i++) {
            if (getBit(i, payload) == 1) counter++;
        }
        return counter!=(payload[payload.length-1] & 0xFF);
    }

    static byte[] makeFrame(byte datatype, byte freshness, byte[] payload){
        byte[] msg=new byte[payload.length+3];
        System.arraycopy(payload,0,msg,2,payload.length);
        msg[0]=datatype;
        msg[1]=freshness;
        msg[msg.length-1]=(byte)checksum(payload);
        return bitStuff(msg);
    }
}
