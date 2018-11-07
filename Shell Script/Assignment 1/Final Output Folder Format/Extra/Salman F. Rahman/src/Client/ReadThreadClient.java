package Client;

//import Server.BufferSize;
import util.NetworkUtil;

import java.util.Map;


/**
 * Created by Nahiyan on 26/09/2017.
 */
public class ReadThreadClient implements  Runnable {
    public Thread thread ;
    private NetworkUtil nu ;
    private Map<Integer,String> map ;
    private int id ;
    private String address ;
    //public BufferSize bz ;


    public ReadThreadClient(NetworkUtil nu)//, int id , String address, BufferSize bz)
    {
        //this.id=id ;
        //this.address=address;
        //System.out.println("Server read cnstructor");
        //this.map = map ;
        thread = new Thread(this);
        this.nu = nu ;
        //this.bz=bz ;

        thread.start();
        //System.out.println("Server read thread started");
    }

    public void run()
    {
        //bz.setMaxBufferSize(bz.getMaxBufferSize()+10);


        while(true)
        {
            //System.out.println("Server run \n");


            String string ;
            string = (String)nu.read().toString() ;
            if(string.equals("-2ep6%d$u#o!p@0^o&o*m(n)")) break ;
            if(string==null) continue;
            //else if (string.equals("-1"))break;

            System.out.println("Server says : " + string);

        }
        nu.closeConnection();
    }



}
