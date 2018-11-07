package Client;

//import Server.ReadThreadClient;
import sun.nio.ch.Net;
import util.NetworkUtil;

import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

import static java.net.InetAddress.*;

/**
 * Created by Nahiyan on 26/09/2017.
 */



public class Client
{
    private ReadThreadClient rtc ;
    private boolean flag = true ;
    private boolean flag2 = true , go = true ;
    private Check check , checkt;
    private Scanner sc = new Scanner(System.in) ;
    private String fileId;
    private File file ;
    private String no;

    byte[] stuffing(byte[] mybytearray,int seqNo ) // real code
    {

        String byteString="";

        byteString+="11111111"; // data or acknowledgemen
        byteString+=String.format( "%8s",Integer.toBinaryString((byte)seqNo & 0xFF)).replace(' ', '0');
        byteString+="00000000"; // ackNo dont care

        byte checksum=0;
        //System.out.println("in stuffing");
        //System.out.println("mybyte length :"+mybytearray.length);
        //System.out.println(byteString);

        for(int i =0 ; i<mybytearray.length;i++)
        {
            // convert to ints and xor
            int one = (int)checksum;
            int two = (int)mybytearray[i];
            int xor = one ^ two;

            // convert back to byte
            checksum = (byte)(0xff & xor);

            byteString+=String.format( "%8s",Integer.toBinaryString(mybytearray[i] & 0xFF)).replace(' ', '0');
            //System.out.println(byteString);

        }

        byteString+=String.format( "%8s",Integer.toBinaryString((checksum & 0xFF)).replace(' ', '0'));

        /*System.out.println("byte String1 :");

        for(int i =0 ; i+8<byteString.length() ; i+=8)
        {
            System.out.println(byteString.substring(i,i+8));
        }*/


        String modified="01111110";
        for(int i=0 ,count=0 ; i<byteString.length();i++)
        {
            modified+=byteString.charAt(i);
            if(byteString.charAt(i)=='1') count++;
            else count = 0;
            if(count==5)
            {
                modified+="0";
                count=0;
            }

        }
        int empty = 0;
        if(modified.length()%8!=0)
        {
            int x = modified.length()/8;
            empty = (x+1)*8 - modified.length();
        }
        //System.out.println(modified.length());

        while (empty--!=0) modified+="0";
        modified+="01111110";
        modified = modified.replace(' ','0');


        /*System.out.println("byteString :");
        for(int i =0 ; i<byteString.length() ; i+=8)
        {
            System.out.println(byteString.substring(i,i+8));
        }


        System.out.println("modified :");
        for(int i =0 ; i<modified.length() ; i+=8)
        {
            System.out.println(modified.substring(i,i+8));
        }*/

        //byte[] byterray = new BigInteger(modified, 2).toByteArray();
        byte sb[] = new byte[modified.length()/8];

        for (int i =0 ,j=0 ; i+7 < modified.length() ;j++, i+=8)
        {

            int val = Integer.parseInt(modified.substring(i,i+8), 2);
            sb[j] = (byte) val;
        }

        return sb;
    }

    public Client()
    {


        Socket socket= null , socket2= null ,s1=null ,s2=null;

        try
        {
            socket = new Socket("localhost",33333);// creating connection
            System.out.println("client 1");
            socket2= new Socket("localhost",44444);
            System.out.println("client 2");
            s1= new Socket("localhost",22222);
            s2= new Socket("localhost",11111);
            System.out.println("client done");

        } catch (IOException e) {
            System.out.println("client Socket problem");
        }

        NetworkUtil nuw = new NetworkUtil(socket);                 // using network util
        NetworkUtil nuf = new NetworkUtil(socket2);

        NetworkUtil nu1 = new NetworkUtil(s1);
        NetworkUtil nu2 = new NetworkUtil(s2);




        //------------------------------------Clint Login---------------------------------------------------------------
        flag=true ;

        while(flag)
        {
            System.out.println("Enter your Student ID to Login in :");
            no=sc.nextLine();
            nuw.write(no);
            //sc.nextLine();
            String ac = nuw.read().toString();
            if (ac.equals("1"))
            {
                System.out.println("Login Successful");
                flag = false;
            }
            else System.out.println("Login Failed");

        }
        new ClientReadThread(no,nu1,nu2);
        //rtc = new ReadThreadClient(nur);

        //------------------------------------------------File Send-----------------------------------------------------

        while(flag2)
        {


            //-------------------------------------------Reciever's id--------------------------------------------------


            while (true)
            {
                System.out.println("Enter The Receiver's Id :");
                nuw.write(sc.nextLine());
                //sc.nextLine();

                if (nuw.read().equals(1))
                {
                    System.out.println("Receiver is Online");
                    flag = true ;
                    break;
                }
                else
                {
                    System.out.println("Receiver Offline. Would you Try again?(Y/N)");
                    if (((sc.nextLine()).toUpperCase()).equals("Y")) continue;
                    else
                    {
                        nuw.write(-1);
                        //flag = false;
                        //break;
                        nuw.closeConnection();
                        return;
                    }
                }

            }





            //-----------------------------------------File Select----------------------------------------------------


            boolean bfSize=true;
            while(bfSize) {
                while (true)
                {
                    System.out.println("Enter File URL:");
                    String filePath = sc.nextLine();
                    filePath.replace('\\', '/');
                    file = new File(filePath);

                    if (file.exists() == true) {
                        break;
                    } else {
                        System.out.println("File does not exist");
                    }

                }


                nuw.write(file.getName());
                nuw.write((long) file.length());


                while (true) // File confirmation
                {
                    Object o = nuw.read();

                    if (o != null)
                    {
                        if ((int) o == 1)
                        {
                            System.out.println("FileSize Okay");
                            bfSize=false;
                            break;
                        }
                        else
                        {
                            System.out.println("FileSize out of bound");
                            break;
                        }
                    }


                }
            }

            int chunkSize;

            while(true)   // chunk size
            {
                Object o = nuw.read();
                if(o != null)
                {
                    chunkSize = (int)o;
                    break;
                }
            }

            while(true)  // fileid
            {
                Object o = nuw.read();
                if(o != null)
                {
                    fileId = o.toString();
                    break;
                }
            }
            //-----------------------------File Send----------------------------------------
            System.out.println(chunkSize);

            boolean gese = true ;
            int packetsize=chunkSize;
            double nosofpackets=Math.ceil(((int) file.length())/packetsize);

            boolean[] readPckt = new boolean[(int)nosofpackets+5];
            for (int i =0 ; i < nosofpackets + 3; i++) readPckt[i+1]=false;

            try{
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                byte[] mybytearray = new byte[packetsize];



                for(double i=0;i<nosofpackets+1;i++)////////////////////////////////////////////////////////////////////
                {
                    try
                    {
                        int bread;
                        if(readPckt[(int)i]==false)
                        {
                            readPckt[(int)i]=true;
                            bread = bis.read(mybytearray, 0, mybytearray.length);
                        }
                        System.out.println("Packet:" + (i));
                        //--->>sc.nextLine();
                        OutputStream os = nuf.socket.getOutputStream();
                        boolean done=false;

                        nuw.write("take");

                        //--------------------------new code------------------------------------------

                        byte[] stuffed = stuffing(mybytearray,((int)i)%127);
                        os.write(stuffed, 0, stuffed.length);

                        //--------- new code for TIMEOUT----------------------------------------------------------------
                        nuf.socket.setSoTimeout(30*1000);
                        InputStream is = nuf.socket.getInputStream();
                        int readbyte;
                        byte[] ackByte = new byte[4];
                        try
                        {
                            while ((readbyte=is.read(ackByte,0,ackByte.length)) != -1)     // block reading data ...
                            //while ((readbyte=is.read(ackByte)) != -1)     // block reading data ...
                            {
                                System.out.println("ackNo n :"+i+" ");
                                // ;
                                byte header = Byte.parseByte("01111110",2);
                                byte tailer = Byte.parseByte("01111110",2);
                                int headerIndex =0;

                                while(true)
                                {
                                    // convert to ints and xor
                                    int one = (int)header;
                                    int two = (int)ackByte[headerIndex];
                                    int and = one ^ two;
                                    if(and==0) break;
                                    headerIndex++;
                                }
                                if ((int)ackByte[headerIndex+2]==i%127)
                                {
                                    System.out.println("ack milse");

                                    break;

                                }
                            }
                        }
                        catch (SocketTimeoutException e)                 // we didn't get any data within 30 seconds ...
                        {
                            //socket.close();                                // ... close the socket

                            System.out.println("Ack for packet "+(int)i+"not found");
                            System.out.println("resending packet :"+(int)i);
                            i--;

                        }

                    }
                    catch(Exception e){}
                }
                bis.close();


            }catch(Exception e){}

            if(gese==true)
            {
                nuw.write("File sending successful");
            }
        }
    }





    public static void main(String[] arg)
    {
        new Client();

    }
}
