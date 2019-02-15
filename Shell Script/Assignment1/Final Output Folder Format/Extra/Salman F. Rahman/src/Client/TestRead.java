package Client;

//import Server.BufferSize;
import util.NetworkUtil;

import java.util.Map;
import java.util.Scanner;


/**
 * Created by Nahiyan on 26/09/2017.
 */
public class TestRead implements  Runnable {
    public Thread thread ;

    private Map<Integer,String> map ;
    Scanner sc = new Scanner(System.in);

    private int id ;


    public TestRead()
    {
        this.thread=new Thread();
        thread.start();
    }

    public void run()
    {
        try {
            thread.sleep(1000);
            System.out.println(System.currentTimeMillis() / 1000);
            thread.sleep(1000);
            System.out.println(System.currentTimeMillis() / 1000);
            thread.sleep(1000);
            System.out.println(System.currentTimeMillis() / 1000);
        }catch(Exception e){}
    }


    /*public TestRead(int id , Map<Integer,String> map )//, int id , String address, BufferSize bz)
    {
        this.id=id ;
        thread = new Thread(this);

        this.map = map ;


        thread.start();

    }

    public void run()
    {

        int ch ;

        while(true)
        {
            ch = sc.nextInt();
            if(ch==0)
            {
                for (int key : map.keySet()) {
                    System.out.println(key + " " + map.get(key));
                }
            }
            else
            {
                map.put(ch,"client"+id);
            }
        }


    }*/



}
