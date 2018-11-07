/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import javax.sound.sampled.Line;
import java.io.FileOutputStream;
import java.util.*;

public class ServerReaderWriter implements Runnable{

    public HashMap<String,Information> clientList;
    public ConnectionUtillities connection;
    public String user;
    public Vector<Chunk> serverStorage;
    public String file_id=" ";
    long  Max_size=1000000000; //1000MB storage
    int i=0;
    public ServerReaderWriter(String username,ConnectionUtillities con, HashMap<String,Information> list,Vector<Chunk> serverStorage){
        connection=con;
        clientList=list;
        user=username;
        this.serverStorage=serverStorage;
    }
    
    @Override
    public void run() {
        try{
        while(true){
            connection.write("Do you want to send any file?(Y/N)");
            i++;
            Object o=connection.read();
            if(o.toString().equals("Y")) {
                o = connection.read();
                String data = o.toString();
                if (clientList.containsKey(data)) {
                    connection.write("Found Recipient");
                    Object ob = connection.read();
                    String msg[] = ob.toString().split(",", 2);
                    int file_size = Integer.parseInt(msg[1]);
                    System.out.println(file_size);
                    Iterator<Chunk> vtr = serverStorage.iterator();
                    int size = 0;
                    while (vtr.hasNext()) {
                        size += vtr.next().size;
                    }
                    if ((size + file_size) > Max_size) {
                        connection.write("File size Exceeded");
                    } else {
                        file_id = user + i;
                        connection.write(file_id);
                        Random rand = new Random();
                        int n = rand.nextInt(5)+5;
                        int time_out=0;
                        connection.write(n);
                        int next_expected=0;
                        Chunk recieved;
                        while (true) {
                            o = connection.read();
                            if (o.toString().equals("Completed")) break;
                            else if(o.toString().equals("time out")){
                                time_out=1;
                                break;
                            }

                            recieved = (Chunk) o;
                            recieved.de_stuff();
                            System.out.println("After destuffing:");
                            recieved.after_bit_pattern();
                            if(recieved.get_seq_no()==next_expected)
                            {
                                Chunk ackn;
                                if(recieved.hasCheckSumError())
                                {
                                    ackn=new Chunk(0," ");
                                    ackn.create_ackn_frame((byte)0,(byte)next_expected);
                                    System.out.println("Error in Checksum..Sending Acknowledgment no: "+next_expected);
                                    connection.write(ackn);
                                }
                                else {
                                    ackn = new Chunk(0, " ");
                                    next_expected = 1 - next_expected;
                                    ackn.create_ackn_frame((byte)0,(byte)next_expected);
                                    System.out.println("Recieved Successfully..Sending Acknowledgment no: "+next_expected);

                                    connection.write(ackn);
                                    serverStorage.add(recieved);
                                }


                            }
                            else
                            {
                                Chunk ackn;
                                ackn=new Chunk(0," ");
                                ackn.create_ackn_frame((byte)0,(byte)next_expected);
                                System.out.println("Need to resend..Sending Acknowledgment no: "+next_expected);
                                connection.write(ackn);
                            }

                        }
                        List<Chunk> toObserve = new Vector<>();
                        for ( Chunk c :serverStorage) {
                            if (c.file_id.equals(file_id)) {
                                toObserve.add(c);
                            }
                        }
                        if(time_out==1){
                         serverStorage.removeAll(toObserve);
                         continue;
                        }
                        System.out.println("Done  Recieving all Chunks.." + " File ID: " +  file_id);

                        int recieved_size=0;
                        Iterator<Chunk> it=toObserve.iterator();
                        while (it.hasNext()) {
                            Chunk temp = it.next();
                            if ((temp.file_id).equals(file_id)) {
                                recieved_size+=temp.size;
                            }

                        }
                        System.out.println(recieved_size+"and"+file_size);
                        if(recieved_size!=file_size)
                        {
                            connection.write("Server's recieved file size does not match with initial file size");
                            serverStorage.removeAll(toObserve);
                            continue;
                        }

                        Information info = new Information();
                        if (clientList.containsKey(data)) {
                            info = clientList.get(data);
                        }

                        info.connection.write("You have a file request from " + user + "\nFile name:" + msg[0] + "\n File size: " + file_size + "\nDo you want to recieve?(Y/N)");
                        String feedback = info.connection.read().toString();
                        if (feedback.equals("Y") && info.state!=0) {
                            //Iterator<Chunk> it = serverStorage.iterator();
                            List<Chunk> toRemove = new Vector<>();
                            for (Chunk c :serverStorage) {
                                if (c.file_id.equals(file_id)) {
                                    toRemove.add(c);
                                }
                            }
                            it=toRemove.iterator();
                            while (it.hasNext()) {
                                Chunk temp = it.next();
                                if ((temp.file_id).equals(file_id)) {
                                    info.connection.write(temp);
                                    Object object = info.connection.read();
                                    if (object.toString().equals("Got a Chunk")) {
                                        System.out.println("Reciever got a Chunk");
                                        continue;
                                    }
                                }

                            }


                            info.connection.write("Done");
                            System.out.println("Done sending");
                            info.state=0;
                            serverStorage.removeAll(toRemove);
                            System.out.println("Now in server has:"+serverStorage.size());
                        }
                        else{
                            info.state=0;
                            System.out.println("File sending not Successfull");
                            Iterator<Chunk> it2 = serverStorage.iterator();
                            while (it2.hasNext()) {
                                Chunk temp = it2.next();
                                if ((temp.file_id).equals(file_id)) {
                                    it2.remove();
                                }
                            }
                        }
                    }

                }
                else{
                    connection.write("Not Found");
                }
            }
            else
            {
                Information info=new Information(connection,user);
                info.state=1;
                clientList.replace(user,info);
                while(clientList.get(user).state!=0){}
            }

        }
        } catch(Exception e)
        {
            e.printStackTrace();
            clientList.remove(user);
            List<Chunk> toRemove = new Vector<>();
            for (Chunk c :serverStorage) {
                if (c.file_id.equals(file_id)) {
                    toRemove.add(c);
                }
            }
            serverStorage.removeAll(toRemove);
            }
            System.out.println("User "+user +" Logged out");

        }
    }
    


