package Server;


import util.NetworkUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Vector;

/**
 * Created by Nahiyan on 01/10/2017.
 */
public class ServerWriteThread implements Runnable {
    public Thread t ;
    Map<String, NUBox> route;
    Vector<FileBox> vf ;
    BufferSize bz;


    public ServerWriteThread(Map<String, NUBox> route,Vector<FileBox> vf,BufferSize bz )
    {
        this.bz=bz;
        this.route=route;
        this.vf=vf;
        t= new Thread(this);
        t.start();
    }


    public void run()
    {
        while(true)
        {
            Vector<Integer> v = new Vector<>();
            for(int j=0; j<vf.size();j++)
            {

                FileBox temp = vf.get(j);
                if(temp.processing==false && route.containsKey(temp.rid)==true)
                {
                    System.out.println("Sending files to "+temp.rid);


                    temp.processing=true;
                    NetworkUtil nu1 = route.get(temp.rid).nu1;
                    NetworkUtil nu2 = route.get(temp.rid).nu2;
                    nu1.write("Do you want to receive files from "+temp.sid+"?(y/n)");
                    boolean flag = true;
                    while(true)
                    {
                        Object o =nu1.read();
                        if(o!=null)
                        {
                            if((int)o==1)
                            {
                                flag= true;
                            }
                            else flag=false;
                            break;
                        }

                    }



                    //-------------------------------Sending Files to receiver------------------------------------------------
                    File file = new File(temp.filePath);

                    if(flag=false)
                    {
                        v.add(j);

                        long size = bz.getCurrentBufferSize()-file.length();
                        bz.setCurrentBufferSize(size);
                        file.delete();

                        continue;
                    }

                    nu1.write(file.getName());
                    nu1.write(file.length());





                    int packetsize=30;
                    double nosofpackets=Math.ceil(((int) file.length())/packetsize);

                    try{
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));


                        long init;
                        for(double i=0;i<nosofpackets+1;i++)
                        {
                            try
                            {

                                byte[] mybytearray = new byte[packetsize];
                                bis.read(mybytearray, 0, mybytearray.length);
                                //System.out.println("Packet:" + (i + 1));
                                OutputStream os = nu2.socket.getOutputStream();
                                os.write(mybytearray, 0, mybytearray.length);
                                os.flush();


                            }
                            catch(Exception e){}
                        }
                        bis.close();

                    }catch(Exception e){}
                    v.add(j);

                    long size = bz.getCurrentBufferSize()-(long)file.length();
                    bz.setCurrentBufferSize(size);
                    file.delete();
                }
            }

            for(int j=v.size()-1;j>=0;j--)
            {
                FileBox temp = vf.get(j);
                //File file = new File(temp.filePath);
                System.out.println("sobshesh");
                //file.delete();

                vf.remove(temp);



            }


        }

    }
}
