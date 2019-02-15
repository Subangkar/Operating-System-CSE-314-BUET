package util;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Nahiyan on 26/09/2017.
 */
public class NetworkUtil {
    public Socket socket ;
    private ObjectInputStream ois ;
    private ObjectOutputStream oos ;

    public NetworkUtil(Socket s)
    {
        //System.out.println("in networkUtil constructor");
        try {
            //System.out.println("nu0");
            this.socket = s;
            //System.out.println("nu1");
            oos = new ObjectOutputStream(socket.getOutputStream());
            //System.out.println("nu2");
            ois = new ObjectInputStream(socket.getInputStream());



            //System.out.println("nu3");
        }catch(Exception e) {
            System.out.println("Error in NetworkUtil constructor");
        }

        //System.out.println("out networkUtil constructor");
    }


    public boolean isConnected()
    {
        return socket.isConnected();
    }

    public boolean isClosed()
    {
        return socket.isClosed();
    }


    public Object read()
    {
        Object object = null ;
        try
        {
            object = ois.readObject();


        }catch(Exception e)
        {
            //System.out.println("Error in NetworkUtil read method "+e);
            return -1682057678 ;
        }

        return object ;
    }

    public int read(byte[] bArray)
    {
        int object = 0 ;
        try
        {
            object = ois.read(bArray);


        }catch(Exception e){ System.out.println("Error in NetworkUtil read method "+e); return -1682057678 ;}

        return object ;
    }



    public int read(byte[] bArray, int off , int  len)
    {
        int object = 0 ;
        try
        {
            object = ois.read(bArray,off,len);

        }catch(Exception e){ System.out.println("Error in NetworkUtil read method "+e); return -1682057678 ;}

        return object ;
    }



    public Object write(Object object)
    {

        try
        {
            oos.writeObject(object);

        }catch(Exception e){ System.out.println("Error in NetworkUtil write method "); return false ;}

        return true ;
    }


    public Object write(byte[] bArray)
    {

        try
        {
            oos.write(bArray);

        }catch(Exception e){ System.out.println("Error in NetworkUtil write method "); return false ;}

        return true ;
    }



    public Object write(byte[] bFile , int off,int len)
    {

        try
        {
            oos.write(bFile,off,len);

        }catch(Exception e){ System.out.println("Error in NetworkUtil write method "); return false ;}

        return true ;
    }

    public void fileSend(File file , int chunkSize)
    {
        int packetsize=chunkSize;
        double nosofpackets=Math.ceil(((int) file.length())/packetsize);
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            for (double i = 0; i < nosofpackets + 1; i++) {
                byte[] myByteArray = new byte[packetsize];
                bis.read(myByteArray, 0, myByteArray.length);
                System.out.println("Packet:" + (i + 1));
                OutputStream os = socket.getOutputStream();
                os.write(myByteArray, 0, myByteArray.length);
                os.flush();
            }
        }
        catch(Exception e){}

    }

    public void fileRecieve(File file , int chunkSize , long fSize)
    {
        int packetsize = chunkSize;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            double nosofpackets = Math.ceil((int) fSize / packetsize);
            for (double i = 0; i < nosofpackets + 1; i++) {
                InputStream is = socket.getInputStream();
                byte[] mybytearray = new byte[packetsize];
                int bytesRead = is.read(mybytearray, 0, mybytearray.length);
                System.out.println("Packet:" + (i + 1));
                bos.write(mybytearray, 0, mybytearray.length);
            }
            socket.close();
            bos.close();
        }
        catch(Exception e){}
    }



    public void closeConnection()
    {
        try
        {
            ois.close();
            oos.close();
        }catch(Exception e){ System.out.println("Error in NetworkUtil close connection method");}

    }

    public InetAddress getINetAddress()
    {
        //System.out.println(socket.getRemoteSocketAddress());
        //System.out.println(socket.getInetAddress());
        //System.out.println(socket.getLocalAddress());
        //System.out.println(socket.getLocalPort());
        //System.out.println(socket.getChannel());



        return socket.getInetAddress();
    }


}
