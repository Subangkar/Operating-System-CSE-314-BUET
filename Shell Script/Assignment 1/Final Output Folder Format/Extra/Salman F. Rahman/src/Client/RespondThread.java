package Client;

import util.NetworkUtil;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nahiyan on 01/10/2017.
 */
public class RespondThread implements  Runnable{

    private Thread thread;
    private OutputStream os;
    private NetworkUtil nuw;
    private NetworkUtil nuf;

    private boolean flag = false , done ;
    Check check,checkt;
    private byte[] mybytearray;
    long init;
    int ackNo;


    //public RespondThread(NetworkUtil nuw, Check check,Check checkt)
    public RespondThread(NetworkUtil nuf, Check check,Check checkt,int ackNo)
    {

        //this.nuw=nuw ;
        this.nuf=nuf;
        this.check=check;
        this.checkt=checkt;
        this.ackNo=ackNo;
        thread=new Thread(this);
        thread.start();
    }

    public void run()
    {
        try
        {
            //System.out.println("created respond thread for ackNo "+ackNo);

            while (checkt.get()==true)
            {
                // -------old code----------
                /*Object o = nuw.read();
                //System.out.println("here");
                if(o!=null)
                {
                    if((int)o==1) {
                        //System.out.println("here2");



                        check.reset();
                        break;
                    }
                }*/


                //------new code------------

                byte[] o = new byte[4];
                InputStream is= nuf.socket.getInputStream();
                int n = is.read(o);


                if(n!=-1)
                {
                    System.out.println("ackNo n :"+ackNo+" ");
                    // ;
                    if ((int)o[2]==ackNo)
                    {
                        System.out.println("ack milse");
                        check.reset();
                        break;

                    }

                }

            }
            //System.out.println("closed respond thread for ackNo "+ackNo);
        }catch(Exception e){}

    }
}
