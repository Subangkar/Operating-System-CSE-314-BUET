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
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Abhishek
 */
public class Server implements Runnable{
    public static Scanner scanner;
    public  OutputStream outputStream = null;
    public  InputStream inputStream = null;
    public Socket socket;
    public  static Vector<Info> myVector=new Vector<Info>();
    public  static Vector<ReceiveInfo> myV=new Vector<ReceiveInfo>();
    int array[]=new int [100];
    public int kmp=0;
    public int sen=0;
    public int Tbytes=0;
    public  String fileN;
    public  int size;
    public int id;
    public static int bufferSize=0;
    public  int chunkSize=0;
    public static int currentSize=0;
    public  int sender=0;
    public  int receiver=0;
    public  String Filename=null;
    public  String ServerFileName=null;
    public int siz=0;
    public void run() {
        try {
            System.out.println("Accepted connection : " + socket);
            outputStream = socket.getOutputStream();
           inputStream = socket.getInputStream();
          // Info object=new Info(id,socket);
           //myVector.add(object);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        String fileLocation,fileName;
		int portNo;
                long fileSize;
                //System.out.println("Please enter file location with file name (e.g. 'D:\\abc.txt'): ");
		//fileLocation=scanner.next();
                //Path p = Paths.get("temp").toAbsolutePath();
                
                
        try {
            int a;
           a= recieveID();
           if(a==0)
               return;
           sendFile();
             byte[] readBuffer = new byte[200];
                int num = inputStream.read(readBuffer);
                String recvedMessage="";
                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
                recvedMessage = new String(arrayBytes, "UTF-8");
               // System.out.println("Received message abhi :" + recvedMessage);
                }
                 if(recvedMessage.equals("Yes"))
                {
                    System.out.println("Client "+id+" wants to send file"); 
                    a=seId();
                     System.out.println("Received message: " + recvedMessage);
                    if(a==0)
                        return;
                     //System.out.println("Received message: " + recvedMessage);
                    a=recieveName();
                    if(a==0)
                    return;
                   a= recieve();
                   if(a==0)
                       return;
                 }
                 else
                 {
                     System.out.println("Client "+id +" does not want to send a file");
                     return;
                 }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Server(Socket socket) {
        this.socket = socket;
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
    public static int byteArrayToInt(byte[] b) 
{
    return   b[3] & 0xFF |
            (b[2] & 0xFF) << 8 |
            (b[1] & 0xFF) << 16 |
            (b[0] & 0xFF) << 24;
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
public int hasChecksumError(byte[] array,int foo,int n)
{
   int k=0,i,j;
    byte b,bs,bit,ad;
    for(i=0;i<n;i++)
    {
         b=array[i];
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
        System.out.println("K "+k);
        if(k==foo)
        {
            return 1;
        }
        else
        {
            return 0;
        } 
}
public byte[] bitDestuff(byte[] bytes,int n)
{
    int i,j,flag=0,flag1=0,len=0,kl=0,jl,klm,ks=0,w=0,q=0,kms=0,std=1;
    byte bs,ab,b,bst,bste,bstef,ad,ac,bas,bitt,adt;
    byte bit=0;
    byte[] arr=new byte[n];
    for(i=0;i<n;i++)
    {
         b=bytes[i];
         //printbit(b);
        for(j=7;j>=0;j--)
        {
            kl=len/8;
          bs=(byte)(1<<j);
          bit=(byte)(b&bs);
          ad=(byte)((bit>>j)&1);
          //printbit(bs);
          //printbit(bit);
          if(ks==1)
          {
              if(ad==0)
              {
                  w=j-1;
                  if(w<0)
                  {
                       //System.out.println("abhi1");
                      adt=0;
                      if(i+1>=n)
                      {
                          std=0;
                      }
                      if(std==1)
                      {
                      ac=bytes[i+1];
                      bas=(byte)(1<<7);
                      bitt=(byte)(ac&bas);
                      adt=(byte)((bitt>>7)&1);
                      }
                      std=1;
                      if(adt==1)
                      {
                       //    System.out.println("abhi2");
                          ks=0;
                          flag1=0;
                          kms=1;
                      }
                      else
                      {
                          ks=0;
                          flag1=0;
                          kms=0;
                      }
                  
                  }
                  else
                  {
                      // System.out.println("abhi3");
                      q=j-1;
                      bas=(byte)(1<<q);
                      bitt=(byte)(b&bas);
                      adt=(byte)((bitt>>q)&1);
                       if(adt==1)
                      {
                        //  System.out.println("abhi4");
                          ks=0;
                          flag1=0;
                          kms=1;
                      }
                      else
                      {
                          ks=0;
                          flag1=0;
                          kms=0;
                      }
                  }
              }
          }
           if(ad==1)
           {
               flag1++;
              // System.out.println("abhi6");
           }
           else
           {
               flag1=0;
           }
           if(flag1==5)
           {
                //System.out.println("abhi5");
               ks=1;
           }
          kl=len/8;
          if(kms==0)
          {
          if(ad==1)
          {
              arr[kl]=(byte)((arr[kl]<<1)|1);
          }
          else{
              arr[kl]=(byte)(arr[kl]<<1);
          }
          }
          else
          {
              kms=0;
              len--;
          }
          len++;
        }
    }
       // printbit(arr[kl]);
    Tbytes=len;
    return arr;
}
public byte[] bitDestuffC(byte[] bytes,int n)
{
    int i,j,flag=0,flag1=0,len=0,kl=0,jl,klm,ks=0,w=0,q=0,kms=0,std=1;
    byte bs,ab,b,bst,bste,bstef,ad,ac,bas,bitt,adt;
    byte bit=0;
    byte[] arr=new byte[n];
    for(i=0;i<n;i++)
    {
         b=bytes[i];
         //printbit(b);
        for(j=7;j>=0;j--)
        {
            kl=len/8;
          bs=(byte)(1<<j);
          bit=(byte)(b&bs);
          ad=(byte)((bit>>j)&1);
          //printbit(bs);
          //printbit(bit);
           if(flag1==5)
           {
                //System.out.println("abhi5");
               ks=1;
           }
           if(ad==1)
           {
               flag1++;
              // System.out.println("abhi6");
           }
           else
           {
               flag1=0;
           }
          kl=len/8;
          if(ks==0)
          {
          if(ad==1)
          {
              arr[kl]=(byte)((arr[kl]<<1)|1);
          }
          else{
              arr[kl]=(byte)(arr[kl]<<1);
          }
          }
          else
          {
              ks=0;
              len--;
          }
          len++;
        }
    }
       // printbit(arr[kl]);
    Tbytes=len;
    return arr;
}
    public  int recieveID() throws IOException
    {
       int i,flag=0;
        id=inputStream.read();
        sender=id;
        for(i=0;i<myVector.size();i++)
                 {
                     //System.out.println(myVector.elementAt(i).getId());
                     if(myVector.elementAt(i).getId()==id) {
                         flag=1;
                         break;
                     }
                 }
        if(flag==1)
        {
            String typedMessage = "Yes";
            System.out.println("Client "+id+" is already logged in");
            outputStream.write(typedMessage.getBytes("UTF-8"));
            return 0;
        }
        else
        {
              String typedMessage = "No";
            outputStream.write(typedMessage.getBytes("UTF-8"));
        System.out.println("ID no "+ id+" is connected");
        Info object=new Info(id,socket);
        myVector.add(object);
        }
        return 1;
    }
     public synchronized void sendFile() throws IOException
    {
       int i,flag=0,number,senderId=0,flags=0,l,m,ps=0,a;
       String flieNs;
        byte[] b;
        boolean bool;
        File file=null;
        String fileN="";
        byte[] readBuffer = new byte[200];
                int num = inputStream.read(readBuffer);

                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
                String recvedMessage = new String(arrayBytes, "UTF-8");
                //System.out.println("Received message :" + recvedMessage);
                if(recvedMessage.equals("Yes"))
                {
                    number=id;
                      System.out.println("Client "+number+" wants to receive file"); 
                       
                    for(i=0;i<myV.size();i++)
                 {
                     
                     if(myV.elementAt(i).getToId()==number) {
                         flag++;
                  //       System.out.println("myV match "+flag);
                     }
                 }
                      outputStream.write(flag);
                    outputStream.flush();
                    if(flag==0)
                    {
                        System.out.println("No one wants to send client "+ number+"a file");
                        return;
                    }
                   // System.out.println("myV size "+myV.size());
                     for(i=0;i<myV.size();i++)
                 {
                    // System.out.println("myV match "+i);
                     if(myV.elementAt(i).getToId()==number) {
                    //     System.out.println("myV matches "+i);
                         array[kmp]=i;
                         kmp++;
                       //  System.out.println("kmp "+kmp);
                         senderId=myV.elementAt(i).getFromId();
                        outputStream.write(senderId);
                        outputStream.flush();
                        fileN=myV.elementAt(i).getFromName();
                        outputStream.write(fileN.getBytes("UTF-8"));
                        
                        outputStream.flush();
                        System.out.println("The filename is "+fileN);
                        flags=1;
                        readBuffer = new byte[200];
                        num = inputStream.read(readBuffer);
                        a=myV.elementAt(i).getSize();
                        String strI = Integer.toString(a);
                        // System.out.println("String "+strI);
                         outputStream.write(strI.getBytes("UTF-8"));
                         outputStream.flush();
                             System.out.println("The filesize is "+a);
                         num = inputStream.read(readBuffer);
                         fileN=myV.elementAt(i).getToName();
                         FileInputStream fileInputStream = null;
		BufferedInputStream bufferedInputStream = null;
                        Path p = Paths.get("temp").toAbsolutePath();
		//creating connection between sender and recei
				try {
                                    String fileLocation=p+"\\"+fileN;
                                        file = new File (fileLocation);
						
                                        byte [] byteArray  = new byte [(int)file.length()];
					fileInputStream = new FileInputStream(file);
					bufferedInputStream = new BufferedInputStream(fileInputStream);
					int n=bufferedInputStream.read(byteArray,0,byteArray.length); 
					System.out.println("Sending " + fileN + "( size: " + byteArray.length + " bytes)");
					outputStream.write(byteArray,0,byteArray.length);			//copying byteArray to socket
					outputStream.flush();	
                                        a=myV.elementAt(i).getSize();
                                        currentSize-=a;
					System.out.println("size "+n+" Done.");
                                          num = inputStream.read(readBuffer);
					} catch (IOException e) {	
				        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                        }
					finally {
						if (bufferedInputStream != null) bufferedInputStream.close();
						if (outputStream != null) bufferedInputStream.close();
                                                if(fileInputStream!=null) fileInputStream.close();
					}
                                   bool=file.delete();
                System.out.println("Client "+senderId+"'s stored file "+fileN+" has been deleted "+bool);
                     }
                 }
                }
                else
                {
                     System.out.println("Client does not want to receive file");
                      for(i=0;i<myV.size();i++)
                 {
                     
                     if(myV.elementAt(i).getToId()==id) {
                         array[kmp]=i;
                         kmp++;
                         flags=1;
                          senderId=myV.elementAt(i).getFromId();
                        fileN=myV.elementAt(i).getFromName();
                        System.out.println("The filename is "+fileN);
                         fileN=myV.elementAt(i).getToName();
                        Path p = Paths.get("temp").toAbsolutePath();
                         String fileLocation=p+"\\"+fileN;
                              file = new File (fileLocation);
                         a=myV.elementAt(i).getSize();
                         currentSize-=a;
                          bool=file.delete();
                System.out.println("Client "+senderId+"'s stored file "+fileN+" has been deleted "+bool);
                         System.out.println("Current Size "+currentSize+" fileSize "+a);
                     }
                 }
                }
                }
                ps=myV.size();
                for(l=0;l<ps;l++)
                {
                    for(m=0;m<myV.size();m++)
                    {
                        if(myV.elementAt(m).getToId()==id)
                        {
                            myV.remove(m);
                            break;
                        }
                    }
                }
                //System.out.println("itr");
                //System.out.println("File "+fileN+" deleted "+bool);
    }
     public  int seId() throws IOException
     {
         int i,flag=0;
        receiver=inputStream.read();
        // System.out.println("Received message : 123" );
        for(i=0;i<myVector.size();i++)
                 {
                     //System.out.println("Vector1"+myVector.elementAt(i).getId()+" receiver "+receiver);
                     if(myVector.elementAt(i).getId()==receiver) {
                        // System.out.println("Vector size "+myVector.size());
         //               System.out.println("Received message : 1234" );
                         flag=1;
                         break;
                     }
                 }
        if(flag==1)
        {
            String typedMessage = "Yes";
           // System.out.println("Received message : 12345" );
            outputStream.write(typedMessage.getBytes("UTF-8"));
                     System.out.println("Receiver is  logged in");
                      System.out.println("File can be sent");
        }
        else
        {
              String typedMessage = "No";
            outputStream.write(typedMessage.getBytes("UTF-8"));
            System.out.println("Receiver is not logged in"); 
            System.out.println("File can not be sent");
             for(i=0;i<myVector.size();i++)
              {
                 if(myVector.elementAt(i).getId()==sender) {
                 System.out.println("Client "+sender+" logged out");
                 myVector.remove(i);
                 break;
              }
               return 0;
        }
        }
        return 1;
     }
         public  int recieveName() throws IOException
    {
        byte[] readBuffer = new byte[200];
                int num = inputStream.read(readBuffer);
                int si=0,i,sit=0;
                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
                String recvedMessage = new String(arrayBytes, "UTF-8");
                fileN=recvedMessage;
                Filename=fileN;
                System.out.println("Received filename :" + recvedMessage);
                }
                num = inputStream.read(readBuffer);
                if (num > 0) {
                byte[] arrayBytes = new byte[num];
                System.arraycopy(readBuffer, 0, arrayBytes, 0, num);
                si=byteArrayToInt(arrayBytes);
                System.out.println("Received fileSize "+si);
                }
                sit=si+currentSize;
                if(bufferSize>=sit)
                {
                    size=si;
                     siz=size;
                    String typedMessage = "No";
                    outputStream.write(typedMessage.getBytes("UTF-8"));
                    outputStream.flush();
                }
                else
                {
                    String typedMessage = "Yes";
                    outputStream.write(typedMessage.getBytes("UTF-8"));
                    outputStream.flush();
                     System.out.println("Filesize is too large"); 
                    for(i=0;i<myVector.size();i++)
                    {
                        if(myVector.elementAt(i).getId()==sender) {
                        System.out.println("Client "+sender+" logged out");
                        myVector.remove(i);
                         break;
                        }
                    }   
                     return 0;
                }
                return 1;
    }
    public synchronized int recieve() throws IOException
    {
        int bytesRead=0;
        int current = 0;
        int ack=0,kms=1,sen=1,wq=1,total=0,mark=0,cursz=0,mq=0;
         int i,flag=0,value,k=0,j,cursize=0,flags=0,az=0,kq=0,checksum=0;
         byte[] b,abhishe,abhi,abhis,abhishek,arpita,isheka;
         File f=null;
        Random rn=new Random();
        chunkSize=rn.nextInt()%10;
        chunkSize=chunkSize+15;
        if(chunkSize<10)
        {
            chunkSize+=8;
        }
        System.out.println("Chunk Size "+chunkSize);
         String strI = Integer.toString(chunkSize);
         // System.out.println("String "+strI);
          outputStream.write(strI.getBytes("UTF-8"));
          outputStream.flush();
        // b = intToByteArray(chunkSize);
        // outputStream.write(b,0,b.length);
        // outputStream.flush();
        Path p = Paths.get("temp").toAbsolutePath();
          System.out.println("Path "+p);
         // Path ps=Paths.get("G:\\abhishek").toAbsolutePath();
         // System.out.println("Path "+ps);
        String fileId = "temp"+FileTransmission.countFile;
        System.out.println("FileId "+fileId);
        String typedMessage = fileId;
        outputStream.write(typedMessage.getBytes("UTF-8"));
        outputStream.flush();
        ServerFileName=fileId;
        FileTransmission.countFile++;
        FileOutputStream fileOutputStream = null;
        String recvedMessage="";
		BufferedOutputStream bufferedOutputStream = null;
        try{
			
			// receive file
			byte [] byteArray;					//I have hard coded size of byteArray, you can send file size from socket before creating this.
			System.out.println("Please wait downloading file");
			
			//reading file from socket
                        f=new File(p+"\\"+fileId);
			fileOutputStream = new FileOutputStream(f);
			bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			current = 0;
                        k=0;
			 j=size/chunkSize;
                            if(size%chunkSize!=0)
                            {
                             j=j+1;
                            }
                            // System.out.println("j size "+j);
                            while(true)
                                {
                                  byteArray  = new byte [200];
                                k++;
                                if(wq==j+1)
                                {
                                    break;
                                }
				bytesRead =inputStream.read(byteArray);
                                if(bytesRead==-1)
                                {
                                    System.out.println("Connection terminated");
                                    fileOutputStream.close();
                                    boolean bool;
                                    bool=f.delete();
                                    return 0;
                                }
                                    System.out.println("Receiving Chunks" + " chunksize: " + bytesRead + " bytes");
                                    printbyte(byteArray);
                                   // checksum=(int)(byteArray[bytesRead-2]);
                                   cursz=0;
                                   for(int lck=0;lck<bytesRead;lck++)
                                   {
                                       if(byteArray[lck]==126)
                                       {
                                           mq++;
                                           //System.out.println("mq "+mq +" lck "+lck);
                                       }
                                        if(mq<2)
                                         {
                                            continue;
                                         }
                                        if(mq==2)
                                        {
                                            mq=0;
                                            mark=cursz;
                                            cursz=lck+1;
                                            total=lck;
                                        }
                                        
                                    az=total-mark+1;
                                    arpita=new byte[az];
                                    System.arraycopy(byteArray, mark, arpita,0,az );
                                    System.out.println("Receiving Chunk" + " chunksize: " + az+ " bytes");
                                       printbyte(arpita);
                                    az=az-3;
                                    abhi=new byte[az];
                                     System.arraycopy(arpita,2, abhi, 0, az);
                                     abhis= bitDestuffC(abhi,az);
                                     //checksum=(int)(abhis[az-1]);
                                     //System.out.println("checksum "+checksum);
                                   // printbyte(abhis);
                                     sen=Tbytes/8;
                    ack=(int)abhis[0];
                    if(wq==ack)
                    {
                        kms=1;
                        wq++;
                    }
                    else
                    {
                        kms=0;
                    }
                    int bcd=sen;
                    bcd=bcd-1;
                    abhishe=new byte[bcd];
                    int bl;
                    bl=bcd-1;
                     //System.out.println("bl "+bl+" cursize "+cursize);
                    System.arraycopy(abhis, 1, abhishe, 0,bcd);
                      System.out.println("Chunk NO "+ack+" after destuffing with checksum" + " chunksize: " + bcd+ " bytes");
                    printbyte(abhishe);
                    checksum=(int)(abhishe[bcd-1]);
                    System.out.println("checksum "+checksum);
                    kq=hasChecksumError(abhishe,checksum,bcd-1);
                    if(kq==0)
                    {
                        wq--;
                        kms=0;
                         System.out.println(" checksum error");
                    }
                    else
                    {
                        System.out.println("No checksum error");
                    }
                    //System.out.println("kq "+kq);
                    //System.out.println("Sent "+sen);
                    isheka=new byte[4];
                                //System.out.println("Receiving Chunk" +k+ " chunksize: " + bytesRead + " bytes");
                                if(kq==1)
                                {
                                    isheka[0]=126;
                                    isheka[3]=126;
                                    isheka[2]=(byte)ack;
                                    isheka[1]=1;
                                    System.out.println("ackNo "+ack);
                                    printbyte(isheka);
                                outputStream.write(isheka);
                                outputStream.flush();
                                }
                                if(kms==1)
                                {
                                    cursize+=bl;
                                bufferedOutputStream.write(abhishe,0,bl);							//writing byteArray to file
                                bufferedOutputStream.flush();
                                }
                                }
			} 
													//flushing buffers
			    byte[] readBs = new byte[200];
                int numst= inputStream.read(readBs);
                if (numst > 0) {
                byte[] arrayByte = new byte[numst];
                System.arraycopy(readBs, 0, arrayByte, 0, numst);
                recvedMessage = new String(arrayByte, "UTF-8");
                
                if(recvedMessage.equals("Yes"))
                {
                    
                      System.out.println("File " + fileId  + " downloaded"); 
                       for(i=0;i<myVector.size();i++)
                 {
                     if(myVector.elementAt(i).getId()==sender) {
                         System.out.println("Client "+sender+" logged out");
                          myVector.remove(i);
                         break;
                     }
                 }
                      if(cursize==size)
                      {
                           currentSize+=cursize;
                           System.out.println("Success");
                              ReceiveInfo object=new ReceiveInfo(sender,receiver,Filename,ServerFileName,siz);
                              myV.add(object);
                              typedMessage = "Yes";
                           outputStream.write(typedMessage.getBytes("UTF-8"));
                           outputStream.flush();
                      }
                      else
                      {
                          System.out.println("Error");
                           typedMessage = "Error";
                           outputStream.write(typedMessage.getBytes("UTF-8"));
                           outputStream.flush();
                           flags=1;
                      }
                }//flushing sock
                }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (fileOutputStream != null) fileOutputStream.close();
			if (bufferedOutputStream != null) bufferedOutputStream.close();
			if (socket != null) socket.close();
		}
        if(flags==1)
        {
                             boolean bool;
                           bool=f.delete();
                            System.out.println("File has been deleted "+bool);
                            return 0;
        }
        return 1;
	}
    }       
