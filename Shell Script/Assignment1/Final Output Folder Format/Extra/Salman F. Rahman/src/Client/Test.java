package Client;

import util.NetworkUtil;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;



import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


/**
 * Created by Nahiyan on 30/09/2017.
 */


//import Server.ReadThreadClient;
import sun.nio.ch.Net;
import util.NetworkUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import static java.net.InetAddress.*;
import static javafx.scene.input.KeyCode.L;
import static javafx.scene.input.KeyCode.T;

/**
 * Created by Nahiyan on 26/09/2017.
 */
public class Test
{
    private TestRead rtc ;
    private boolean flag = true ;
    private Map<Integer,String> map ;

    int chunksize = 3 ;

    byte[] stuffing(byte[] mybytearray,int seqNo )
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

        String ds = deStuffed.substring(0+3*8,0+3*8+chunksize*8);
        //System.out.println("ds:"+ds);


        //byte[] byteframe = new BigInteger(ds, 2).toByteArray(); ;

        byte byteframe[] = new byte[chunksize];

        for (int i =0 ,j=0 ; i+7 < ds.length() ;j++, i+=8)
        {

            int val = Integer.parseInt(ds.substring(i,i+8), 2);
            byteframe[j] = (byte) val;
        }
        return byteframe;
    }

    public Test()
    {

        /* 10101100
        11001010
        10011001
         */
        String bString = "00011111111110101010010101010101010101010101010101010101010101010101011100111110";
        chunksize = bString.length()/8;
        System.out.println("Fromm caller: ");

        for(int i=0 ; i+7<bString.length() ; i+=8 )
        {
            System.out.println(bString.substring(i,i+8));
        }



        byte bval[] = new byte[chunksize];

        for (int i =0 ,j=0 ; i+7 < bString.length() ;j++, i+=8)
        {

            int val = Integer.parseInt(bString.substring(i,i+8), 2);
            bval[j] = (byte) val;
        }

        /*byte[] bval = new BigInteger(bString, 2).toByteArray();
        System.out.println(bval.length);
        for(int i=0; i<bval.length;i++)
        {
            System.out.println(bval[i]);

        }*/

        byte[] mybytearray = stuffing(bval,102);

        String byteString;


        /*System.out.println("came from stuffing");
        for(int i =0 ; i<mybytearray.length;i++)
        {

            // convert back to byte

            byteString=String.format( "%8s",Integer.toBinaryString(mybytearray[i] & 0xFF)).replace(' ', '0');
            System.out.println(byteString);
        }*/

        byte[] deStuffed = deStuffing(mybytearray);
        System.out.println("came from deStuffing");
        for(int i =0 ; i<deStuffed.length;i++)
        {

            // convert back to byte

            byteString=String.format( "%8s",Integer.toBinaryString(deStuffed[i] & 0xFF)).replace(' ', '0');
            System.out.println(byteString);
        }
    }

    boolean check(byte[] destuffed)
    {
        byte checksum = (byte)0;

        for(int i =3 ; i<destuffed.length ; i++)
        {
            int a =(int)checksum;
            int b =(int)destuffed[i];
            int c = a^b ;
            checksum = (byte)(c^0xFF) ;
        }

        for(int i =0 ; i<destuffed.length;i++)
        {
            // convert to ints and xor
            int one = (int)checksum;
            int two = (int)destuffed[i];
            int xor = one ^ two;

            // convert back to byte
            checksum = (byte)(0xff & xor);

            //byteString+=String.format( "%8s",Integer.toBinaryString(destuffed[i] & 0xFF)).replace(' ', '0');
            //System.out.println(byteString);

        }

        if((int)checksum==0)
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





    public static void main(String[] arg)
    {
        new Test();
    }
}


/*  to convert string to byte array
String bString = "0100101010011001";
byte[] bval = new BigInteger(bString, 2).toByteArray();

to convert byte to string
 byteString=String.format( "%8s",Integer.toBinaryString(mybytearray[i] & 0xFF)).replace(' ', '0');

// convert to ints and xor
int one = (int)byte1[0];
int two = (int)byte2[0];
int xor = one ^ two;

// convert back to byte
byte b = (byte)(0xff & xor);

 */

/*    String s = "10000000";
    int val = Integer.parseInt(s, 2);
    byte b = (byte) val;

*/
/* File transfer code


/*File file = new File("I:/cg fight/buet books/L 3 T 2/IronMan.jpg");

        byte[] bFile = new byte[(int)file.length()+1];
        byte[] bFile2 = new byte[(int)file.length()+1];

        FileOutputStream fos = null;
        try {
            FileInputStream fis= new FileInputStream(file);
            fis.read(bFile);


            fos = new FileOutputStream("I:/cg fight/buet books/L 2 T 1/"+file.getName());
            bFile2=bFile;
            fos.write(bFile2);
            fos.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }*/