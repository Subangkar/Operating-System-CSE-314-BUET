package Server;

//import Server.BufferSize;

import java.util.Map;
import java.util.Scanner;
import java.util.Vector;


/**
 * Created by Nahiyan on 26/09/2017.
 */
public class TestRead implements  Runnable {
    public Thread thread ;

    private Map<Integer,String> map ;
    Scanner sc = new Scanner(System.in);
    Vector<FileBox>vf;
    private int id ;


    public TestRead(Vector<FileBox>vf)
    {
        this.vf=vf;
        this.thread=new Thread();
        thread.start();
    }

    public void run()
    {
        int i=1;
        while(i!=0)
        {
            for(int j=0; j<vf.size();j++)
            {
                FileBox temp=vf.get(j) ;
                System.out.println(temp.fileid+" "+temp.sid+" "+temp.rid+" "+temp.processing+" "+temp.sent);
            }
            i = new Scanner(System.in).nextInt();

        }
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
