package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.IntStream;

import sun.nio.ch.Net;
import util.NetworkUtil;

import static java.lang.System.in;
import static java.lang.System.setOut;

/**
 * Created by Nahiyan on 26/09/2017.
 */
public class Server {
    int count =0;
    long fileCount=0;
    private ServerSocket serverSocketRead ,serverSocketFile,serverSocket1,serverSocket2 ;
    private ServerReadThread srt ;
    private int afrina = 23 ;
    private Map<String,String> map ;
    private Map<String,NUBox> route;

    Vector<FileBox>vf ;
    FileCount fc ;

    public BufferSize bz ;




    public Server()
    {
        bz = new BufferSize() ;
        map = new HashMap<String , String>();
        route = new HashMap<String , NUBox>();
        vf = new Vector<>();
        fc = new FileCount();




        System.out.println("Enter maximum size of all chunks allowed in the buffer");

        bz.setMaxBufferSize(10000000);//(new Scanner(System.in)).nextInt());
        System.out.println("maximum size of all chunks allowed in the buffer is "+bz.getMaxBufferSize());
        //System.out.println("Server constructor");


        new ServerWriteThread(route,vf,bz);


        try
        {   //System.out.println("Server going to initialize serverSocket");
            serverSocketRead = new ServerSocket(33333);
            serverSocketFile = new ServerSocket(44444);

            serverSocket1 = new ServerSocket(22222);
            serverSocket2 = new ServerSocket(11111);




            while(true)
            {

                Socket connectionSocket = serverSocketRead.accept() ;
                System.out.println("server 1");
                Socket cf = serverSocketFile.accept();
                System.out.println("server 2");
                //System.out.println("got "+connectionSocket+" "+cf);
                Socket cf1 = serverSocket1.accept();
                Socket cf2 = serverSocket2.accept();


                NetworkUtil nu = new NetworkUtil(connectionSocket);
                NetworkUtil nuf = new NetworkUtil(cf);
                NetworkUtil nu1 = new NetworkUtil(cf1);
                NetworkUtil nu2 = new NetworkUtil(cf2);

                srt = new ServerReadThread( map ,route, ++count , nu ,nuf,nu1,nu2 ,bz,vf,fc);


            }






        } catch(Exception e){ System.out.println("Error in server socket :" +e ); }


    }

    public static void main(String[] arg){
        Server server = new Server() ;
        System.out.println("Server Terminated");



    }
}

class FileCount
{
    private long count;

    public void FileCount()
    {
        count=0;
    }

    public synchronized Object get()
    {
        return count;
    }
    public synchronized Object get2()
    {
        return ++count;
    }

    public synchronized void inc()
    {
        count++;
    }

}


class BufferSize
{
    private long maxBufferSize ;
    private long currentBufferSize = 0;

    public BufferSize()
    {
        currentBufferSize = 0 ;
    }

    public synchronized long getMaxBufferSize() { return maxBufferSize; }

    public synchronized long getCurrentBufferSize(){ return currentBufferSize; }

    public synchronized void setMaxBufferSize(long p) {maxBufferSize = p;}

    public synchronized void setCurrentBufferSize(long p) {
        System.out.println("currentBuffersize:" +p ); currentBufferSize = p;}



}


class NUBox
{
    public NetworkUtil nu1,nu2;
    public NUBox(NetworkUtil nu1,NetworkUtil nu2)
    {
        this.nu1=nu1;
        this.nu2=nu2;
    }
}