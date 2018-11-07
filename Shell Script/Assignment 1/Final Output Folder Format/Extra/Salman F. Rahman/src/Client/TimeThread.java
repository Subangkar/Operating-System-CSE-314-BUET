package Client;

import util.NetworkUtil;

import java.io.OutputStream;

/**
 * Created by Nahiyan on 01/10/2017.
 */
public class TimeThread implements Runnable {

    private Thread thread;
    private OutputStream os;
    private NetworkUtil nuw;
    private boolean flag = false , done ;
    Check check , checkt;
    private byte[] mybytearray;
    long init;
    public TimeThread(long time, Check check,Check checkt)
    {
        this.init=time ;
        this.check=check;
        this.checkt=checkt;
        thread=new Thread(this);
        thread.start();
    }

    public void run()
    {
        try
        {
            //os.write(mybytearray, 0, mybytearray.length);
            while (check.get()==true)
            {
                //System.out.println("time thread");
                if(System.currentTimeMillis()/1000-init>30)
                {
                    System.out.println("time sesh");


                    checkt.reset();
                    break;
                }
            }


        }catch(Exception e){}

    }


}
