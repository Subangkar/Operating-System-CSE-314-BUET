import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Mrinmoy Roddur on 9/20/2017.
 */

public class Server {

    public static final int SENDER = 1;
    public static final int RECEIVER = 2;
    public static final int FILE_INFO = 3;
    public static final int FILE = 4;
    public static final int ACKNOWLEDGEMENT = 5;
    public static final int COMPLETION = 6;
    public static final int REQUEST=7;
    public static final int FILE_ERROR=8;
    public static final int FILE_RECEIVER=9;
    public static final int SUCCESS=10;

    Selector selector;
    ServerSocketChannel serverChannel;
    ServerWriter sw;

    Server() {
        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(6789));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            sw = new ServerWriter(this);
        } catch (IOException e) {
            System.out.println("Server couldn't start. Process terminating....");
            System.exit(0);
        }
    }

    void start() throws IOException {
        while (true) {
            selector.select();
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel client = server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);


                } else if (key.isReadable()) {
                    SocketChannel currentSocket = (SocketChannel) key.channel();
                    ByteBuffer headb = ByteBuffer.allocate(5);
                    ByteBuffer bodyb = ByteBuffer.allocate(1024);
                    ByteBuffer[] buf = {headb, bodyb};
                    try{
                        currentSocket.read(buf);
                        processData(headb.array(), bodyb.array(), key);
                    } catch(IOException e){
                        key.cancel();
                    }

                } else if (key.isWritable()) {
                    byte[] temp = sw.get((SocketChannel) key.channel());
                    ByteBuffer buf = ByteBuffer.allocate(temp.length);
                    buf.put(temp);
                    buf.flip();
                    try {
                        ((SocketChannel) key.channel()).write(buf);
                    } catch(IOException e){
                        LoginManager.getInstance().remove((SocketChannel)key.channel());
                    }
                    key.interestOps(SelectionKey.OP_READ);

                }

            }
        }
    }

    void processData(byte[] header, byte[] data, SelectionKey key) {
        if (header[0] == Server.SENDER) {
            byte[] roll = new byte[header[1]];
            System.arraycopy(data, 0, roll, 0, header[1]);
            InetAddress addr = ((SocketChannel)key.channel()).socket().getLocalAddress();
            int port=((SocketChannel)key.channel()).socket().getPort();


            byte[] a = new byte[1];
            a[0] = (byte) ((LoginManager.getInstance().login(new String(roll), addr, port,
                    (SocketChannel)key.channel())) ? 1 : 0);
            sw.add((SocketChannel) key.channel(), a);
            if(a[0]==1)System.out.println(new String(roll)+" has been connected to the server");

        } else if (header[0] == Server.RECEIVER) {
            byte[] roll = new byte[header[1]];
            System.arraycopy(data, 0, roll, 0, header[1]);
            byte[] receiver = new byte[header[2]];
            System.arraycopy(data, roll.length, receiver, 0, receiver.length);
            byte[] fileName = new byte[header[3]];
            System.arraycopy(data, roll.length + receiver.length, fileName, 0, fileName.length);
            byte[] fileSize = new byte[header[4]];
            System.arraycopy(data, header[1] + header[2] + header[3], fileSize, 0, fileSize.length);

            if(!LoginManager.getInstance().isOnline(new String(receiver))){
                byte[] err={Server.FILE_INFO,0};
                sw.add((SocketChannel) key.channel(), err);
            } else {
                sw.add((SocketChannel) key.channel(), FileManager.getInstance().addFile(new String(roll),
                        new String(receiver), new String(fileName), Integer.parseInt(new String(fileSize))));
                System.out.println(new String(roll)+" wants to send file "+new String(fileName)+
                   " to "+new String(receiver)+" | File size = "+new String(fileSize)+" bytes | " +
                        "Chunk size = 32 bytes");

            }

        } else if (header[0] == Server.FILE) {
            byte[] temp = new byte[4];
            System.arraycopy(header, 1, temp, 0, 4);
            int s = ByteBuffer.wrap(temp).order(ByteOrder.LITTLE_ENDIAN).getInt();
            try {
                if (!FileManager.getInstance().addChunk(s, data)) {
                    byte[] aa = new byte[5];
                    aa[0] = Server.ACKNOWLEDGEMENT;
                    sw.add((SocketChannel) key.channel(), aa);
                    System.out.println("Acknowledgement sent");
                } else {
                    byte[] aa = new byte[5];
                    aa[0] = Server.COMPLETION;
                    sw.add((SocketChannel) key.channel(), aa);
                    System.out.println("Acknowledgement sent");
                    byte[] b = new byte[5];
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    b[0] = Server.SUCCESS;
                    b[1] = (byte) ((FileManager.getInstance().checkFile(s)) ? 1 : 0);
                    sw.add((SocketChannel) key.channel(), b);
                    if (FileManager.getInstance().checkFile(s)) {
                        String fileIdb = Integer.toString(s);
                        String filename = FileManager.getInstance().getFileName(s);
                        String fileSize = Integer.toString(FileManager.getInstance().getFileSize(s));
                        String sender = FileManager.getInstance().getSender(s);
                        byte[] a = new byte[5 + fileIdb.length() + filename.length() + fileSize.length() + sender.length()];
                        a[0] = Server.REQUEST;
                        a[1] = (byte) fileIdb.length();
                        a[2] = (byte) filename.length();
                        a[3] = (byte) fileSize.length();
                        a[4] = (byte) sender.length();
                        System.arraycopy(fileIdb.getBytes(), 0, a, 5, a[1]);
                        System.arraycopy(filename.getBytes(), 0, a, 5 + a[1], a[2]);
                        System.arraycopy(fileSize.getBytes(), 0, a, 5 + a[1] + a[2], a[3]);
                        System.arraycopy(sender.getBytes(), 0, a, 5 + a[1] + a[2] + a[3], a[4]);
                        sw.add(LoginManager.getInstance().returnChannel(FileManager.getInstance().getReceiver(s)), a);
                    } else {
                        FileManager.getInstance().deleteFile(s);
                    }
                }
            } catch (IOException e){
                System.out.println("Has checksum error. No acknowledgement sent");

            }
        } else if(header[0]==Server.FILE_RECEIVER){
            byte[] fileIds=new byte[header[1]];
            System.arraycopy(data,0,fileIds,0,header[1]);
            int fileId=Integer.parseInt(new String(fileIds));
            SocketChannel channel=(SocketChannel) key.channel();
            FileManager.getInstance().sendFile(fileId,channel);
            FileManager.getInstance().deleteFile(fileId);
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}