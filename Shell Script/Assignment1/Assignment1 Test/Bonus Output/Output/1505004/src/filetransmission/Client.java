/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransmission;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Abhishek
 */
public class Client {
    public static Scanner scanner;
	/**
	 * 
	 * @param args
	 * this function collect all required data from user.
	 * @throws IOException 
	 */
    public  OutputStream outputStream = null;
    public  InputStream inputStream = null;
    public  Socket socket = null;
    public    FileInputStream fileInputStream = null;
    public  BufferedInputStream bufferedInputStream = null;
    public  int chunkSize=0;
    public int size=0;
    public int id;
    public int Tbyte=0;
    public int sent=0;
    public int goN=0;
    public int payback=0;
	public static void main(String[] args) throws IOException
	{
		String fileLocation,ipAddress;
		int portNo;
                int ids;
                int i;
                int flag=0;
                int receiverId=0;
		scanner=new Scanner(System.in);
		System.out.println("Enter ipAddress of machine(if you are testing this on same machine than enter 127.0.0.1) :");
		ipAddress="127.0.0.1";

		System.out.println("Enter port number of machine(e.g. '2000') :");
		portNo=2000;
                Client client=new Client();
                 client.socket = new Socket(ipAddress,portNo);
                 client.socket.setSoTimeout(30000);
                 client.outputStream = client.socket.getOutputStream();
                 client.inputStream=client.socket.getInputStream();
                 System.out.println("Enter login id from 1 to 255 :");
                 client.id =scanner.nextInt();
                int a= client.sendID(client.id);
                if(a==0)
                    return;
                 System.out.println("Do you want to recieve?Enter Yes or No");
                String ab=scanner.next();
                if(ab.equals("Yes"))
                {
                client.receive();
                }
                 else
                {
                    String  typedMessage = "No";
            client.outputStream.write(typedMessage.getBytes("UTF-8"));
                    System.out.println("Client does not want to receive file");
                }
                System.out.println("Do you want to send?Enter Yes or No");
                 ab=scanner.next();
                if(ab.equals("Yes"))
                {
                    String typedMessage = "Yes";
            client.outputStream.write(typedMessage.getBytes("UTF-8"));
                    System.out.println("Client "+client.id+" wants to send a file");
                System.out.println("Please enter the receiverId: ");		//you can modify this program to receive file name from server and then you can skip this step
		receiverId=scanner.nextInt();
                   // System.out.println("Abhi1");
                 a=client.recID(receiverId);
                if(a==0)
                    return;
		System.out.println("Please enter file location with file name to save (e.g. 'D:\\abc.txt'): ");		//you can modify this program to receive file name from server and then you can skip this step
		fileLocation=scanner.next();
                int value=client.sendName(fileLocation);
                if(value==0)
                    return;
		a=client.send(fileLocation);
                if(a==0)
                    return;
                }
                else
                {
                     String typedMessage = "No";
            client.outputStream.write(typedMessage.getBytes("UTF-8"));
                    System.out.println("Client "+client.id+" does not want to send a file");
                return;
                }
	}
        public static int byteArrayToInt(byte[] b) 
{
    return   b[3] & 0xFF |
            (b[2] & 0xFF) << 8 |
            (b[1] & 0xFF) << 16 |
            (b[0] & 0xFF) << 24;
}
public void printbyte(byte[] bytes)
{
    byte bs,bst;
    
    /*bst=(byte)((bs>>1)& 127);
    System.out.print(Integer.toBinaryString(bst & 255 | 256).substring(1)+"  ");
    bs=(byte)((bs)& bst);
      System.out.print(Integer.toBinaryString(bs & 255 | 256).substring(1)+"  ");*/
    for (byte b : bytes)
    {
        System.out.print(Integer.toBinaryString(b & 255 | 256).substring(1)+"  ");
    }
    System.out.println(" ");
}
public void printbit(byte bytes)
{
    byte bs,bst;
    
    /*bst=(byte)((bs>>1)& 127);
    System.out.print(Integer.toBinaryString(bst & 255 | 256).substring(1)+"  ");
    bs=(byte)((bs)& bst);
      System.out.print(Integer.toBinaryString(bs & 255 | 256).substring(1)+"  ");*/
        System.out.print(Integer.toBinaryString(bytes & 255 | 256).substring(1)+"  ");
    System.out.println(" ");
}
public byte[] bitStuff(byte[] bytes,int n,int k)
{
    int i,j,flag=0,flag1=0,len=0,kl=0,jl,klm;
    byte bs,ab,b,bst,bste=0,bstef,ad;
    byte bit=0;
    byte[] arr=new byte[30];
    ab=(byte)(k);
   for(jl=7;jl>=0;jl--)
        {
            klm=len/8;
          bst=(byte)(1<<jl);
          bste=(byte)(ab&bst);
          ad=(byte)((bste>>jl)&1);
           if(ad==1)
           {
               flag1++;
           }
           else
           {
               flag1=0;
           }
           if(flag1==6)
           {
              arr[klm]=(byte)((arr[klm]<<1)); 
              len++;
              flag1=1;
           }
          klm=len/8;
          if(ad==1)
          {
              arr[klm]=(byte)((arr[klm]<<1)|1);
          }
          else{
              arr[klm]=(byte)(arr[klm]<<1);
          }
          len++;
        }
    for(i=0;i<n;i++)
    {
         b=bytes[i];
         //printbit(b);
        for(j=7;j>=0;j--)
        {
            kl=len/8;
          bs=(byte)(1<<j);
          bit=(byte)(b&bs);
          ad=(byte)((bit>>j)& 0x01);
          //printbit(bs);
          //printbit(bit);
           if(ad==1)
           {
               flag1++;
           }
           else
           {
               flag=0;
               flag1=0;
           }
           if(flag1==6)
           {
              arr[kl]=(byte)((arr[kl]<<1)); 
              len++;
               flag1=1;
           }
           // System.out.print("ad "+ad+" flag "+flag1+" ");
          kl=len/8;
          if(ad==1)
          {
              arr[kl]=(byte)((arr[kl]<<1)|1);
          }
          else{
              arr[kl]=(byte)(arr[kl]<<1);
          }
            //printbit(arr[kl]);
            if((i==n-1)&&(j==0))
            {
                //System.out.println("abhi");
                int ag=len%8;
                ag=7-ag;
                // System.out.println("ag "+ag);
                arr[kl]=(byte)(arr[kl]<<ag);
            }
          len++;
          Tbyte=len;
        }
        //System.out.println("mn ");
        //System.out.println("");
        /*for(int mn=0;mn<=kl;mn++)
        {
        printbit(arr[mn]);
        }*/
    }
       // printbit(arr[kl]);
    return arr;
}
public byte[] bitStuffC(byte[] bytes,int n,int k)
{
    int i,j,flag=0,flag1=0,len=0,kl=0,jl,klm;
    byte bs,ab,b,bst,bste=0,bstef,ad;
    byte bit=0;
    byte[] arr=new byte[30];
    ab=(byte)(k);
   for(jl=7;jl>=0;jl--)
        {
            klm=len/8;
          bst=(byte)(1<<jl);
          bste=(byte)(ab&bst);
          ad=(byte)((bste>>jl)&1);
           if(ad==1)
           {
               flag1++;
           }
           else
           {
               flag1=0;
           }
          klm=len/8;
          if(ad==1)
          {
              arr[klm]=(byte)((arr[klm]<<1)|1);
          }
          else{
              arr[klm]=(byte)(arr[klm]<<1);
          }
          len++;
          klm=len/8;
           if(flag1==5)
           {
              arr[klm]=(byte)((arr[klm]<<1)); 
              len++;
              flag1=0;
           }
        }
    for(i=0;i<n;i++)
    {
         b=bytes[i];
         //printbit(b);
        for(j=7;j>=0;j--)
        {
            kl=len/8;
          bs=(byte)(1<<j);
          bit=(byte)(b&bs);
          ad=(byte)((bit>>j)& 0x01);
          //printbit(bs);
          //printbit(bit);
           if(ad==1)
           {
               flag1++;
           }
           else
           {
               flag=0;
               flag1=0;
           }
           // System.out.print("ad "+ad+" flag "+flag1+" ");
          kl=len/8;
          if(ad==1)
          {
              arr[kl]=(byte)((arr[kl]<<1)|1);
          }
          else{
              arr[kl]=(byte)(arr[kl]<<1);
          }
           len++;
           kl=len/8;
           if(flag1==5)
           {
              arr[kl]=(byte)((arr[kl]<<1)); 
              len++;
               flag1=0;
           }
            //printbit(arr[kl]);
            if((i==n-1)&&(j==0))
            {
                //System.out.println("abhi");
                int ag=len%8;
                ag=8-ag;
                // System.out.println("ag "+ag);
                arr[kl]=(byte)(arr[kl]<<ag);
            }
          Tbyte=len;
        }
        //System.out.println("mn ");
        //System.out.println("");
        /*for(int mn=0;mn<=kl;mn++)
        {
        printbit(arr[mn]);
        }*/
    }
       // printbit(arr[kl]);
    return arr;
}
public int checkSum(byte [] arr,int n)
{
    int k=0,i,j;
    byte b,bs,bit,ad;
    for(i=0;i<n;i++)
    {
         b=arr[i];
         //printbit(b);
        for(j=7;j>=0;j--)
        {
          bs=(byte)(1<<j);
          bit=(byte)(b&bs);
          ad=(byte)((bit>>j)&1);
          //printbit(bs);
          //printbit(bit)
          if(ad==1)
          {
              k++;
          }
        }
    }
        System.out.println("Checksum "+k);
    return k;
}
public  byte[] intToByteArray(int a)
{
    return new byte[] {
        (byte) ((a >> 24) & 0xFF),
        (byte) ((a >> 16) & 0xFF),   
        (byte) ((a >> 8) & 0xFF),   
        (byte) (a & 0xFF)
    };
}
        public int sendID(int id)
        {
        try {
            outputStream.write(id);
            outputStream.flush();
            byte[] readBuffer = new byte[200];
                int num = inputStream.read(readBuffer);

                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
                String recvedMessage = new String(arrayBytes, "UTF-8");
                //System.out.println("Received message :" + recvedMessage);
                if(recvedMessage.equals("Yes"))
                {
                      System.out.println("Already logged in"); 
                     return 0;
                }
                else
                {
                     System.out.println("Logged in successfully"); 
                }
                }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1;
        }
        public  void receive() throws UnsupportedEncodingException, IOException
        {
            int number,i,id,si=0;
            String fname;
            String typedMessage = "Yes",recvedMessage="";
            outputStream.write(typedMessage.getBytes("UTF-8"));
            outputStream.flush();
            number=inputStream.read();
            if(number==0)
            {
                System.out.println("No one wants to send a file");
            }
           // System.out.println("number "+number);
            for(i=0;i<number;i++)
            {
                id=inputStream.read();
                System.out.println("Client "+id+" wants to send you a file");
                 byte[] readBuffer = new byte[200];
                int num = inputStream.read(readBuffer);
                //String abhi= new String(readBuffer);
                //System.out.println(ab);
                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
                String receivedMessage = new String(arrayBytes, "UTF-8");
                recvedMessage=receivedMessage;
                System.out.println("Received filename :" + recvedMessage);
                }
            typedMessage = "Yes";
            outputStream.write(typedMessage.getBytes("UTF-8"));
            outputStream.flush();
                 num = inputStream.read(readBuffer);
                 int foo=0;
                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
                String receivedMessage = new String(arrayBytes, "UTF-8");
                foo = Integer.parseInt(receivedMessage);
                System.out.println("Received filesize :" + foo);
                }
                outputStream.write(typedMessage.getBytes("UTF-8"));
            outputStream.flush();
               /* num = inputStream.read(readBuffer);
                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
                si=byteArrayToInt(arrayBytes);
                System.out.println("Received fileSize "+si);
                }*/
                int bytesRead=0;
		int current = 0;
                 Path ps=Paths.get("G:\\abhishek").toAbsolutePath();
                 File  file = new File (ps+"\\"+recvedMessage);
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		try {
			byte [] byteArray  = new byte [foo];					//I have hard coded size of byteArray, you can send file size from socket before creating this.
			System.out.println("Please wait downloading file");
                        
			fileOutputStream = new FileOutputStream(file);
			bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			//bytesRead = inputStream.read(byteArray,0,byteArray.length);					//copying file from socket to byteArray
                        //System.out.println("total bytes read " + bytesRead);
			current = bytesRead;
				bytesRead =inputStream.read(byteArray);
				if(bytesRead >= 0) current += bytesRead;
                                System.out.println("total size read " + current);
			bufferedOutputStream.write(byteArray, 0 , bytesRead);							//writing byteArray to file
			bufferedOutputStream.flush();												//flushing buffers
			outputStream.write(typedMessage.getBytes("UTF-8"));
                        outputStream.flush();
			System.out.println("File " + recvedMessage  + " downloaded ( size: " + current + " bytes read)");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (fileOutputStream != null) fileOutputStream.close();
			if (bufferedOutputStream != null) bufferedOutputStream.close();
		}
            }
        }
         public int recID(int id)
        {
        try {
           // System.out.println("Abhi");
            outputStream.write(id);
            outputStream.flush();
             //System.out.println("Received message :123");
            byte[] readBuffer = new byte[200];
                int num = inputStream.read(readBuffer);

                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
                String recvedMessage = new String(arrayBytes, "UTF-8");
                System.out.println("Received message :" + recvedMessage);
                if(recvedMessage.equals("No"))
                {
                      System.out.println("Receiver is not logged in");
                      System.out.println("File can not be sent");
                     return 0;
                }
                else
                {
                     System.out.println("Receiver is  logged in"); 
                     System.out.println("File can be sent");
                }
                }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 1;
        }
        public  int sendName(String fileLocation) throws UnsupportedEncodingException, IOException
        {
            byte [] b;
            String path = fileLocation.substring(fileLocation.lastIndexOf("\\") + 1,fileLocation.length());
            System.out.println("filename "+path); 
            String typedMessage = path;
            outputStream.write(typedMessage.getBytes("UTF-8"));
            outputStream.flush();
            File file = new File (fileLocation);
            int a=(int)file.length();
            System.out.println("Size "+a); 
               b = intToByteArray(a);
               outputStream.write(b,0,b.length);
               outputStream.flush();
                   byte[] readB = new byte[200];
                int nums= inputStream.read(readB); 
                if (nums > 0) {
                byte[] arrayBytes = new byte[nums];
                System.arraycopy(readB, 0, arrayBytes, 0, nums);
                String recvedMessage = new String(arrayBytes, "UTF-8"); 
                if(recvedMessage.equals("Yes"))
                {
                    
                      System.out.println("Filesize is too large"); 
                      return 0;
                }//flushing sock
                }
                return 1;
        }
        public int send(String fileLocation) throws IOException
        {
            int num,si=0,i,j,k=0,cursize=0,n=0,flags=0,tin=0,check=0,sen,ks=0,flg=1,flag=1;
             byte[] readBuffer = new byte[200];
             byte[] rBuffer=new byte[200];
             byte[] st;
             int []arrb=new int[5];
             byte[][] copy=new byte[300][];
             /*
            num = inputStream.read(readBuffer);
            if (num > 0) {
            byte[] arrayBytes = new byte[num];
             System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
            si=byteArrayToInt(arrayBytes);
            System.out.println("Received chunkSize "+si);
            }*/
              num = inputStream.read(readBuffer);
                // int foo=0;
                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
                String receivedMessage = new String(arrayBytes, "UTF-8");
                si = Integer.parseInt(receivedMessage);
                System.out.println("Received chunkSize :" + si);
                }
                num = inputStream.read(rBuffer);
                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(rBuffer, 0, arrayBytes, 0, num);
                String recvedMessage = new String(arrayBytes, "UTF-8");
                System.out.println("Received fileId :" + recvedMessage);
                }
            chunkSize=si;
            try{
		//creating object to send 
		File file = new File (fileLocation);
                  size=(int)file.length();
                 System.out.println("Sending " + fileLocation + "( size: " + size + " bytes)");
		fileInputStream = new FileInputStream(file);
		bufferedInputStream = new BufferedInputStream(fileInputStream);
                j=size/chunkSize;
                sen=1;
                if(size%chunkSize!=0)
                {
                j=j+1;
                }
               while(true)
               {
                   int lv;
                   lv=chunkSize;
                   byte[] byteArray;
                   byte[] chck;
                   chck=new byte[chunkSize+1];
                  byteArray  = new byte [chunkSize];
                   int [] b=new int [chunkSize];
                   if(flg==0)
                   {
                      // System.out.println("sen "+sen+" payback "+payback);
                       for(int it=sen;it<=payback;it++)
                       {
                                 System.out.println("Sending Chunk" +it+ " chunksize: " + n + " bytes" +byteArray.length );
                                 System.out.println("After Stuffing Resending");
                                 printbyte(copy[it]);
                       outputStream.write(copy[it]);
                       goN++;
                       }
                       flg=1;
                               
                   }
                   ks++;
                   if(ks<=j)
                   {
		n=bufferedInputStream.read(byteArray);
                payback++;
                k++;
                goN++;
               // byteArray[2]=(byte)255;
               //byteArray[3]=(byte)255;
                System.out.println("Sending Chunk" +k+ " chunksize: " + n + " bytes" +byteArray.length );
                      check=checkSum(byteArray, n);
                      System.arraycopy(byteArray, 0, chck, 0,n);
                      chck[n]=(byte)check;
                       System.out.println("Payload");
                       printbyte(chck);
                   st= bitStuffC(chck, n+1, k);
                   // printbyte(st);
                    sent=Tbyte/8;
                    if((Tbyte%8)>0)
                    {
                        sent++;
                    }
                    int bc=sent;
                    //System.out.println("Sent "+sent);
                    sent+=3;
                    byte[] abhishek=new byte[sent];
                    abhishek[0]=126;
                    abhishek[1]=0;
                    System.arraycopy(st, 0, abhishek, 2, bc);
                    abhishek[sent-1]=126;
                    printbyte(abhishek);
                    copy[ks]=new byte[sent];
                    System.arraycopy(abhishek, 0, copy[ks], 0, sent);
                  // copy[ks]=abhishek;
                  /*if(k==4)
                  {
                      abhishek[sent-2]=(byte)255;
                  }*/
                       System.out.println("After Stuffing");
                       printbyte(copy[ks]);
                outputStream.write(abhishek);
		//outputStream.write(abhishek,0,sent);
                outputStream.flush();//copying byteArray to sock
                }
                    byte[] readB = new byte[10];
                    if((goN==5)||(ks>=j))
                    {
                try{
                    for(int lm=0;lm<goN;lm++)
                    {
                        int w;
                int nums= inputStream.read(readB,0,4);
                 // System.out.println("Ack");
                       // printbyte(readB);
                //System.out.println("Nums "+nums);
                if (nums > 0) {
                byte[] arrayBytes = new byte[nums];
                System.arraycopy(readB, 0, arrayBytes, 0, nums);
                 //String typedMessage = "Yes";
               //outputStream.write(typedMessage.getBytes("UTF-8"));
               //outputStream.flush();
               w=(int)arrayBytes[nums-2];
                //System.out.println("Sen "+sen+" w "+w);
                    System.out.println("Waiting for ackNo "+sen);
                 System.out.println("Received ackNO "+w);
                    printbyte(arrayBytes);
               if(sen==w)
               {
                   flg=1;
                   sen++;
               }
               else{
                   flg=0;
                    }
                    //System.out.println("AGa "+w);
                    
                      System.out.println("Chunk sent");
                }
               }
                }catch (InterruptedIOException iioe)
                {
                       // String typedMessage = "Error";
                        System.out.println("Timeout");
                        flg=0;
              // outputStream.write(typedMessage.getBytes("UTF-8"));
               //outputStream.flush();
                }
                goN=0;
                 if(sen==j+1)
                       break;
                    }
            }
               String typedMessage = "Yes";
               outputStream.write(typedMessage.getBytes("UTF-8"));
               outputStream.flush();
               byte[] readBs = new byte[200];
                int numst= inputStream.read(readBs);
                if (numst > 0) {
                byte[] arrayByte = new byte[numst];
                System.arraycopy(readBs, 0, arrayByte, 0, numst);
                String recivedMessage = new String(arrayByte, "UTF-8");
                if(recivedMessage.equals("Yes"))
                {
                    System.out.println("Success");
                }
                else if(recivedMessage.equals("Error"))
                      {
                          System.out.println("Error");
                          return 0;
                      }
                }//flushi
            }
		finally {
			if (bufferedInputStream != null) bufferedInputStream.close();
			if (outputStream != null) bufferedInputStream.close();
			if (socket!=null) socket.close();
			}	
            return 1;
        }
        
}
