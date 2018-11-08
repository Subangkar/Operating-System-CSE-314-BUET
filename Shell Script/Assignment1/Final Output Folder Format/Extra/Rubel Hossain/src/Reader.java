/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;

/**
 *
 * @author uesr
 */
public class Reader implements Runnable{
    public ConnectionUtillities connection;
    String state=" ";
    public Reader(ConnectionUtillities con){
        connection=con;
    }

    @Override
    public void run() {

        Scanner in=new Scanner(System.in);
        int size,chunk_size;
        while(true){
            //System.out.println("Do you want to send any file??(Y/N)");
            Object object= null;
            try {
                object = connection.read();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            }
            System.out.println(object.toString());
            if(object.toString().equals("Server's recieved file size does not match with initial file size")) continue;
            String text=in.nextLine();
            connection.write(text);
            if(Objects.equals(text, "Y")) {
                System.out.println("Enter User name of recipient:");
                text = in.nextLine();
                connection.write(text);
                Object o= null;
                try {
                    o = connection.read();
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
                if(Objects.equals(o.toString(), "Found Recipient")) {
                    System.out.println("Enter File path:");
                    int retry=0;
                    do{
                    text=in.nextLine();
                    File f=new File(text);
                    try{
                        FileInputStream f1 = new FileInputStream(f) ;
                        size=f1.available();
                        String s=f.getName()+","+size;
                        System.out.println("File size= "+size);
                        connection.write(s);
                        s=connection.read().toString();
                        if(s.equals("File size Exceeded"))
                        {

                         System.out.println("Does not have enough storage.");
                            continue;
                        }
                        chunk_size=Integer.parseInt(connection.read().toString());
                        System.out.println("Max size of a chunk is:"+chunk_size);
                        byte array[]=new byte[chunk_size];
                        int chunk_count=(size)/chunk_size+1;
                        System.out.println(chunk_count+" chunk/s is/are needed" );
                        connection.sc.setSoTimeout(30*1000);
                        int next_to_send=0;
                        System.out.println("Do you want to show error detection(Y/N?)");
                        String dec=in.nextLine();
                        int error=0;
                        for(int i=0;i<chunk_count;i++)
                        {
                              int read=f1.read(array);

                              Chunk c=new Chunk(read,s);

                            if(error==3 && dec.equals("Y")){
                                c.create_frame(array,(byte)1,(byte)next_to_send,(byte)0,1);
                                //error_free.create_frame(array,(byte)1,(byte)next_to_send,(byte)0,0);
                            }
                              else c.create_frame(array,(byte)1,(byte)next_to_send,(byte)0,0);
                              System.out.println("After stuffing");
                              c.after_bit_pattern();
                              next_to_send=1-next_to_send;
                              do {
                                  error++;
                                  System.out.println("Sending Chunk "+i+"("+read+")bytes....Seq no: "+(1-next_to_send));
                                  connection.write(c);
                                  Chunk ackn = null;
                                  try {
                                      ackn = (Chunk) connection.read();
                                      ackn.de_stuff();
                                      System.out.println("Acknowledgement recieved "+ackn.get_ackn_no());
                                      //ackn.after_bit_pattern();
                                  }
                                  catch(SocketTimeoutException e)
                                  {
                                      continue;
                                  }
                                  if(error==7 && dec.equals("Y")){
                                      next_to_send=ackn.get_ackn_no();
                                      break;
                                  }
                                  if(ackn.get_ackn_no()==next_to_send) break;


                              }while(true);

                        }

                        connection.sc.setSoTimeout(0);
                        connection.write("Completed");
                        retry=0;
                    } catch(InterruptedIOException e) {
                       connection.write("Time Out");
                       e.printStackTrace();
                       retry=0;
                    } catch(IOException e) {
                        System.out.println("File not Found");
                        e.printStackTrace();
                        retry=1;
                    }

                    }while (retry==1);
                }
                else {
                    System.out.println("Recipient not Logged in");
                }
            }
            else
            {
                Object o= null;
                try {
                    o = connection.read();
                } catch (SocketTimeoutException e) {
                    e.printStackTrace();
                }
                String info=o.toString();
                System.out.println(info);
                String file_name[]=info.split("\n",3);
                String f_name[]=file_name[1].split(":",2);
                if("You have".equals(info.substring(0, 8)))
                {
                    FileOutputStream fo= null;
                    text=in.nextLine();
                    connection.write(text);
                    String path_name="D:\\Output\\";
                    try {
                        fo = new FileOutputStream(path_name+f_name[1]);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (text.equals("Y"))
                    {
                        try {
                            o=connection.read();
                        } catch (SocketTimeoutException e) {
                            e.printStackTrace();
                        }
                        while(!o.toString().equals("Done"))
                         {
                             Chunk temp=(Chunk)o;
                             byte[] output=temp.payload_array_byte();
                             try {
                                 fo.write(output);
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                             /*Iterator<Byte> it2=temp.storage.iterator();
                             while(it2.hasNext())
                             {
                                 try {
                                     fo.write(it2.next());
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }
                             }*/
                             connection.write("Got a Chunk");
                             try {
                                 o=connection.read();
                             } catch (SocketTimeoutException e) {
                                 e.printStackTrace();
                             }
                         }
                         System.out.println("File has been saved at location:"+path_name);
                    }
                }
                //System.out.println(o.toString());
            }
        }
    }
}
