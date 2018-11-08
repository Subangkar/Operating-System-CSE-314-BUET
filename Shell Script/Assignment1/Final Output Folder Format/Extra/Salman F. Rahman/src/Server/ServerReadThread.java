package Server;

import sun.nio.ch.Net;
import util.NetworkUtil;

import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;


/**
 * Created by Nahiyan on 26/09/2017.
 */
public class ServerReadThread implements  Runnable {
    public String client = "Client";
    public Thread thread ;
    private NetworkUtil nu ,nuf ,nu1,nu2;
    private Map<String,String> map ;
    private Map<String,NUBox> route;
    private String sid ,rid ;
    private String address ;
    public BufferSize bz ;
    private boolean flag1,flag2,flag3 ,flag4 ;
    private Vector<FileBox> vf;
    private FileCount fc;
    String fName="" ;
    long fSize=0;
    String fid;
    File file;

    int chunkSize;

    Scanner sc = new Scanner(System.in);


    byte[] deStuffing(byte[] mybytearray  )
    {
        byte header = Byte.parseByte("01111110",2);
        byte tailer = Byte.parseByte("01111110",2);
        int headerIndex =0;
        while(true)
        {
            // convert to ints and xor
            int one = (int)header;
            int two = (int)mybytearray[headerIndex];
            int and = one ^ two;
            if(and==0) break;
            headerIndex++;
        }
        //System.out.println(headerIndex);

        String payload = "" ;

        for(int i = headerIndex+1 ; i<mybytearray.length;i++)
        {
            if(((int)mybytearray[i]^(int)tailer)==0)
            {
                break;
            }
            payload+=String.format( "%8s",Integer.toBinaryString(mybytearray[i] & 0xFF)).replace(' ', '0');
        }
        /*System.out.println("payload :");
        for(int i =0 ; i<payload.length() ; i+=8)
        {
            System.out.println(payload.substring(i,i+8));
        }*/
        int count = 0;
        String deStuffed="" ;
        for(int i = 0 ,j=0 ; i<payload.length();i++, j++)
        {

            deStuffed+=payload.charAt(i);

            if(payload.charAt(i)=='1') count++;
            else count= 0;

            if(count==5)
            {
                i++;
                count=0;
            }
        }

        /*System.out.println("deStuffed :");
        for(int i =0 ; i+8<deStuffed.length() ; i+=8)
        {
            System.out.println(deStuffed.substring(i,i+8));
        }*/

        String ds = deStuffed.substring(0,8*(chunkSize+4));
        //System.out.println("ds:"+ds);


        //byte[] byteframe = new BigInteger(ds, 2).toByteArray(); ;

        byte byteframe[] = new byte[chunkSize+4];

        for (int i =0 ,j=0 ; i+7 < ds.length() ;j++, i+=8)
        {

            int val = Integer.parseInt(ds.substring(i,i+8), 2);
            byteframe[j] = (byte) val;
        }
        return byteframe;
    }





    boolean check(byte[] destuffed)
    {
        byte checksum = (byte)0x0;

        for(int i =3 ; i<destuffed.length-1 ; i++)
        {
            int a =(int)checksum;
            int b =(int)destuffed[i];
            int c = a^b ;
            checksum = (byte)(c^0xFF) ;
        }

        if(checksum==destuffed[destuffed.length-1])
        {
            System.out.println("packet "+(int)destuffed[1]+" is good");
            return true ;
        }
        else
        {
            System.out.println("packet "+(int)destuffed[1]+" is corrupted");
            return false ;
        }


    }

    byte[] createAck(int ackNo)
    {
        byte ack[] = { (byte)0 , (byte)0 , (byte)ackNo , (byte)0};
        return ack;
    }

    public ServerReadThread(Map<String,String> map,Map<String,NUBox> route, int no, NetworkUtil nu , NetworkUtil nuf ,
                            NetworkUtil nu1 , NetworkUtil nu2 ,BufferSize bz,
                            Vector<FileBox> vf,FileCount fc)
    {
        client = client + no ;
        System.out.println("Server read cnstructor");
        this.map = map ;
        this.route=route;
        this.nu = nu ;
        this.nu1 = nu1 ;
        this.nu2 = nu2 ;
        this.bz=bz ;
        this.nuf = nuf;
        this.vf=vf;
        this.fc=fc;

        thread = new Thread(this);
        thread.start();

        System.out.println("Server read thread started");
    }

    public void run()
    {
        //bz.setMaxBufferSize(bz.getMaxBufferSize()+10);
        //----------------------------Client Authentication part ---------------------------------------
        flag1 = true;
        while(flag1)
        {
            sid=nu.read().toString();
            if(sid.equals("-1682057678"))
            {
                nu.closeConnection();

                break;
            }
            if(map.containsKey(sid))
            {
                nu.write(0);
            }
            else
            {
                map.put(sid,nu.getINetAddress().toString());
                route.put(sid,new NUBox(nu1,nu2));
                nu.write(1);
                flag1=false ;
            }

        }
        flag4=!flag1;
        while(flag4)
        {

            //----------------------------getting Reciever------------------------------------------------

            flag2 = !flag1;
            while (flag2) {
                rid = nu.read().toString();
                if (rid.equals("-1682057678") || rid.equals("-1") )
                {
                    map.remove(sid);
                    route.remove(sid);

                    nu.closeConnection();
                    flag4=false;
                    break;
                }
                if (!map.containsKey(rid)) {
                    nu.write(0);
                } else {
                    //map.put(id,nu.getINetAddress().toString());
                    nu.write(1);
                    flag2 = false;
                }
            }


            //--------------------------------getting File------------------------------------------------------------------

            flag3 = true;
            boolean bfSize=true;
            while(bfSize) {

                //String fName;
                //long fSize;

                while (true) { //getting fName
                    fName = nu.read().toString();
                    if(fName.equals("-1682057678"))
                    {
                        map.remove(sid);
                        route.remove(sid);
                        nu.closeConnection();
                        return ;
                    }
                    if (fName != null) {
                        break;
                    }
                }

                while (true) { // getting fSize

                    Object o = nu.read();
                    if(o.toString().equals("-1682057678"))
                    {
                        map.remove(sid);
                        route.remove(sid);
                        nu.closeConnection();
                        return ;
                    }
                    if (o != null) {
                        fSize = (long) o;

                        break;
                    }
                }

                System.out.println(fName + " " + fSize);

                if (bz.getCurrentBufferSize() + fSize < bz.getMaxBufferSize()) {
                    nu.write(1);
                    break;
                } else {
                    nu.write(0);
                }
            }

            //chunkSize =( (  ((int)(Math.random()*1000) )  %5+1)*10)  ;
            chunkSize = 50 ;
            nu.write(chunkSize);
            fid = ((Object) ((long) fc.get2())).toString();
            nu.write(fid);


            //----------------------------------getting Files-----------------------------------------------------------

            boolean done = true;
            boolean paisi = true;

            int packetsize=chunkSize;
            try {
                file = new File("./ServerFiles/"+fid+fName);
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                double nosofpackets = Math.ceil((fSize) / packetsize);

                //------------------------------new code------boolean---------------------------------------------------
                boolean[] sentAck = new boolean[(int)nosofpackets+5];
                for (int i =0 ; i < nosofpackets + 3; i++) sentAck[i+1]=false;


                boolean cFlag=false;
                int cPacket=0 , cByte=0;

                System.out.println("There are "+(int)nosofpackets+" packets each having "+chunkSize+" bytes.");
                System.out.println("Enter the packet number and byte number you want to corrupt");
                System.out.println("(Enter -1 if you don't want to corrupt any byte)");
                System.out.println("Packet no :");
                cPacket = sc.nextInt();
                if (cPacket!=-1 && cPacket < nosofpackets ) cByte   = sc.nextInt();



                for (double i = 0; i < nosofpackets + 1; i++)//////////////////////////////////////////////////////////
                {//////////////////////////////////////////////////////////////////////////////////////////////////////

                    boolean go = true ;
                    while(true)
                    {
                        //System.out.println("here");
                        Object o = nu.read();
                        if(o.toString().equals("-1682057678"))
                        {
                            map.remove(sid);
                            route.remove(sid);
                            //-----------------------------------------------------------------------------------------//
                            done=false;
                            //file.delete();
                            //nu.closeConnection();
                            //return ;
                            go=false;
                            bos.close();
                            file.delete();
                            return;
                            //break ;
                        }
                        if(o!=null)
                        {
                            //System.out.println("here1");
                            if((o.toString()).equals("terminate"))
                            {
                                //System.out.println("here2");
                                //file.delete();
                                go = false ;
                                done=false;
                                break;
                            }
                            go=true;
                            //System.out.println("here3");
                            break;
                        }
                    }
                    if(go== false) break;
                    InputStream is = nuf.socket.getInputStream();


                    //-------------- new code -------byte destuffing ---------------------------------------------------
                    byte[] mybytearray = new byte[(6+packetsize)*4];
                    int bytesRead = is.read(mybytearray);

                    System.out.println("Packet:" + (i));

                    byte[] deStuffed = deStuffing(mybytearray);
                    if (((int)i)%127==((int)deStuffed[1])-1)i--;
                    //-------------- new code------- byte manupulating -------------------------------------------------

                    if ((int)i==cPacket)
                    {
                        cPacket=-1;
                        String byteString=
                                String.format("%8s",Integer.toBinaryString(
                                        deStuffed[cByte] & 0xFF)).replace(' ', '0');
                        System.out.println("packet "+i+" byte "+cByte+" is:");
                        System.out.println(byteString);
                        System.out.println("Enter your altered Byte:");
                        byteString = sc.next();
                        byteString = byteString.replace(" ","");

                        int cval = Integer.parseInt(byteString, 2);
                        deStuffed[cByte] = (byte) cval;

                    }




                    //-------------- new code ------ byte checking -----------------------------------------------------

                    boolean status = check(deStuffed);
                    if(status==true)// && (i!=127 || cFlag==true ) )
                    {
                        //if(sentAck[(int)i]==false)
                        {
                            System.out.println("sending ack for packet "+(int)i);
                            byte[] ack = { (byte)0 , (byte)0 , deStuffed[1] ,(byte)0 };
                            OutputStream os = nuf.socket.getOutputStream();
                            os.write(ack, 0, ack.length);
                            System.out.println("ackNo :" + (i));
                            //sentAck[(int)i] = true;
                        }
                    }
                    else
                    {
                        //cFlag=true;

                        System.out.println("Packet "+(i)+" is corrupted");
                        i--;
                        continue;
                    }

                    if(sentAck[(int)i]==false)
                    {
                        sentAck[(int)i] = true;
                        bos.write(deStuffed, 3, chunkSize);
                    }

                }

                bos.close();
                if(done==false)
                {
                    System.out.println("deleting files");
                    //file = new File("./ServerFiles/"+fid+fName);
                    file.delete();
                    paisi=false;
                }

            }catch(Exception e){}

            if(paisi==true)
            {

                while(true)
                {
                    Object o = nu.read();
                    if(o.toString().equals("-1682057678"))
                    {
                        map.remove(sid);
                        route.remove(sid);

                        return ;
                    }
                    if(o!=null)
                    {
                        if(o.toString().equals("File sending successful"))
                        {
                            System.out.println("File sending successful");
                            break;
                        }
                    }
                }

                System.out.println(file.length()+""+fSize);


                if(file.length()-fSize<=chunkSize)
                {
                    System.out.println("size thikase");
                    vf.add(new FileBox(fid, sid, rid,
                            "./ServerFiles/" + fid + fName));

                    bz.setCurrentBufferSize(bz.getCurrentBufferSize() + file.length());
                }
                else
                {
                    System.out.println(file.length()+" "+fSize);
                    file.delete();
                }
            }



        }

        nu.closeConnection();
    }



}
