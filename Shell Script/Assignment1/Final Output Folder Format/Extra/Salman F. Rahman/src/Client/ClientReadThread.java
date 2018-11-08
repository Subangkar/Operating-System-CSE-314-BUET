package Client;

import Server.FileBox;
import util.NetworkUtil;

import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

/**
 * Created by Nahiyan on 01/10/2017.
 */
public class ClientReadThread implements Runnable {
    public Thread t ;
    //Map<String, NetworkUtil> route;
    //Vector<FileBox> vf ;
    public NetworkUtil nu1,nu2 ;
    public String clientName="Client";
    public String fid;
    public long fSize;


    public ClientReadThread(String no ,NetworkUtil nu1,NetworkUtil nu2)
    {
        System.out.println("Inside ClientReadThread");
        clientName=clientName+"_"+no;
        this.nu1=nu1;
        this.nu2=nu2;
        t= new Thread(this);
        t.start();
    }


    public void run()
    {
        while(true)
        {
            boolean flag = true ;
            while(true)
            {
                Object o = nu1.read();
                if(o!=null)
                {
                    System.out.println(o.toString());
                    //String yes = new Scanner(System.in).nextLine().toUpperCase();

                    //if(yes.equals("Y"))
                    if(true)
                    {
                        nu1.write(1);
                        flag= true;
                    }
                    else
                    {
                        nu1.write(0);
                        flag=false ;
                    }
                    break;
                }
            }

            if(!flag) continue;


            while(true)
            {
                Object o =nu1.read();
                if(o!=null)
                {
                    fid=o.toString();
                    break;
                }
            }


            while(true)
            {
                Object o =nu1.read();
                if(o!=null)
                {
                    fSize=(long)o;
                    break;
                }
            }



            int packetsize=30;
            try {
                System.out.println("ei porjnto");
                File file = new File("./ClientFiles/"+clientName+"_"+fid);
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                double nosofpackets = Math.ceil((fSize) / packetsize);
                for (double i = 0; i < nosofpackets + 1; i++) {
                    InputStream is = nu2.socket.getInputStream();
                    byte[] mybytearray = new byte[packetsize];
                    int bytesRead = is.read(mybytearray, 0, mybytearray.length);
                    System.out.println("Packet:" + (i + 1));
                    bos.write(mybytearray, 0, mybytearray.length);
                }
                //sock.close();
                bos.close();
            }catch(Exception e){}


        }

    }
}
