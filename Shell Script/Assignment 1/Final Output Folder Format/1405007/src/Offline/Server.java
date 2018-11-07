
package Offline;

/**
 *
 * @author Rifat
 */
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;

public class Server extends javax.swing.JFrame implements Runnable{
    public static Server Srv=new Server();
    public BufferedReader Br;
    public OutputStreamWriter osw;
    public Socket sc;
    
    public static ArrayList<NameSocket> arra=new ArrayList<NameSocket>();
    public String str;                                                          
    public NameSocket NS;
    
    public final static int buffer_size=9022386;
    public static int total_size=0;
    
    public String receiver;
    public Socket receiverSocket;
    public String SendingFile;
    public int chunk_receive;
    public int summation;
    public int File_size;
    
    public int Total_Chunks;
    public int GBN =8;
    
   
    
    public InputStream IS;
    public OutputStream OS;
    
    public Server() {
        initComponents();
    }
    
    
    public Server(Socket s) throws IOException
    {
        IS=s.getInputStream();
        Br=new BufferedReader(new InputStreamReader(s.getInputStream()));
        osw=new OutputStreamWriter(s.getOutputStream());
        sc=s;
    }
    
    
    public Socket getSocket(String s)
    {
        Socket sk=null;
        for(int i=0;i<arra.size();i++)
        {
            if(arra.get(i).str.equals(s)==true)
            {
                sk= arra.get(i).sckt;
                break;
            }
        }
        return sk;
    }
    
    //bit destuff
    public String bitDestuff(String input)
    {
        int i,j,k,l,len;
        StringBuffer sbuf=new StringBuffer(input);
        len=sbuf.length();
        char cha;
        String output="";
        int count=0;
        i=0;
        while(i<len)
        {
            cha=sbuf.charAt(i);
            if(cha=='0')
            {
                output=output+sbuf.charAt(i);
                count=0;
                i++;
            }
            else
            {
                count++;
                output=output+sbuf.charAt(i);
                if(count==5)
                {
                    output=output;
                    i=i+2;
                    count=0;
                }
                else
                {
                    i++;
                }
            }
        }
        return output;
    }
    
    //BitPatternToByteArray
    public  void bitPatternToByteArray(String str,byte[] ara)
    {
        StringBuffer sb=new StringBuffer(str);
        String s;
        int len=sb.length();

         //ara=new byte[len/8];
        int count=0;
        for(int k=0;k<len;k=k+8)
        {
            s=sb.substring(k,k+8);
            ara[count]=Byte.parseByte(s, 2);
            System.out.println(ara[count]);
            count++;
        }
        //return ara;
    }
    
    //num of 1's in string
    public int OneCount(String str)
    {
        StringBuffer sb=new StringBuffer(str);
        int len=sb.length();
        int count=0;
        for(int i=0;i<len;i++)
        {
            if(sb.charAt(i)=='1')
            {
                count++;
            }
        }
        return count;
    }

    //complement of bit string pattern
    public String Complement(String input)
    {
        StringBuffer sb=new StringBuffer(input);
        for(int i=0;i<8;i++)
        {
            if(sb.charAt(i)=='1')
            {
                sb.setCharAt(i,'0');
            }
            else
            {
                sb.setCharAt(i,'1');
            }
        }
        String str=sb.toString();
        return str;

    }
    
    //Checksum Error Verification
    public boolean hasChecksumError(String payload,String checksum)
    {
        int numOf1=OneCount(payload);
        int chksm=Integer.parseInt(checksum,2);
        numOf1=numOf1%1024;
        int ttl=numOf1+chksm;

        String s1 = String.format("%11s", Integer.toBinaryString(ttl & 0x7FF)).replace(' ', '0');

        //System.out.println(s1);

        String LS8bit=s1.substring(3, 11);
        String MS2bit=s1.substring(0, 3);

        //System.out.println(LS8bit+"  "+MS2bit);

        int ls8=Integer.parseInt(LS8bit, 2);
        int ms2=Integer.parseInt(MS2bit, 2);

        int sum=ls8+ms2;

        s1 = String.format("%8s", Integer.toBinaryString(sum & 0xFF)).replace(' ', '0');
        s1=Complement(s1);

        if(s1.compareTo("00000000")==0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    //Bit-Stuffing Implement
    public String bitStuff(String input)
    {
        char cha;
        int i,j,k,l,len;
        StringBuffer sbuf=new StringBuffer(input);
        len=sbuf.length();
        String output="";
        int count=0;
        for(i=0;i<len;i++)
        {
            cha=sbuf.charAt(i);
            if(cha=='0')
            {
                count=0;
                output=output+sbuf.charAt(i);
            }
            else
            {
                count++;
                output=output+sbuf.charAt(i);
                if(count==5)
                {
                    output=output+'0';
                    count=0;
                }
            }
        }
        return output;
    }
    
    //Acknowledement Frame
    public String AckFrame(String SeqNo)
    {
        String delimited="01111110";
        String type="11111111";
        String Ackframe="";
        Ackframe=type+SeqNo;
        String AckStuffedframe=bitStuff(Ackframe);
        String realframe=""+delimited+AckStuffedframe+delimited;
        return realframe;
    }
    
    
    
    @Override
    public void run() {
        //Checking existance, add, delete
        int byteRead;
        int curr=0;
        BufferedOutputStream BOS = null;
        FileOutputStream FOS = null;
        String str1=null;
        DefaultTableModel model=(DefaultTableModel)Srv.jTable2.getModel();
        Object row[]=new Object[1];
        BufferedInputStream BIS=null;
        String TimeOut;
         try
         {
             str=Br.readLine();
             NS=new NameSocket(str,sc);//Name Socket object creation
             if(arra.isEmpty()==true)
             {
                 arra.add(NS);
                 Srv.jTextArea1.append(str+" connected \n");
                 osw.write(str+" Added\n");
                 osw.flush();
                 row[0]=str;
                 model.addRow(row);
             }
             else
             {
                 if(getSocket(str)!=null)
                 {
                     Srv.jTextArea1.append(str+" Already Exist\n");
                     osw.write(str+" Already Exist\n");
                     osw.flush();
                     sc.close();
                     
                 }
                 else
                 {
                     osw.write(str+" Added\n");
                     osw.flush();
                     arra.add(NS);
                     Srv.jTextArea1.append(str+" connected \n");
                     row[0]=str;
                     model.addRow(row);
                 }
             }
             
             //Send receiver name
             while(true){
             str1=Br.readLine();
             if(str1.equals("Yes")==false && str1.equals("No")==false){
             while(true){
                
                Srv.jTextArea1.append(str+" asks for the receiver "+str1+"\n");
                Socket k=getSocket(str1);
                if(k==null)
                {
                    osw.write(str1+" is not available now...\n");
                    osw.flush();
                    str1=Br.readLine();
                }
                else
                {
                    if(str.compareToIgnoreCase(str1)==0)
                    {
                        osw.write("User's own ID. Cannot transfer file...\n");
                        osw.flush();
                        str1=Br.readLine();
                    }
                    else{
                        osw.write(str1+" is available, give file name please....\n");
                        osw.flush();
                        break;
                    }
                }
             }
             
             receiver=str1;
             //Send the file name
             System.out.println("Receiver : "+receiver);
             String Strm=Br.readLine();
             System.out.println(Strm);
             
             String str2=Strm.substring(0, Strm.indexOf("?"));//file name of the file that is dending by sender
             SendingFile=str2;
             System.out.println(SendingFile+" "+str2);
             int sz=Integer.parseInt(Strm.substring(Strm.indexOf("?")+1,Strm.length()));//file size of the file that is dending by sender
             File_size=sz;
             
             total_size=total_size+sz;
             System.out.println("Total Size : "+total_size);
             Srv.jTextArea1.append("Total size of the file :"+sz+" bytes\n");
             if(buffer_size>=total_size){
                Random rand=new Random();
                int kk=rand.nextInt(sz);//Chunk Size Random created by the server
                //kk=1000;
                if(kk>2000)
                {
                    //rand=new Random();
                    while(true){
                        kk=rand.nextInt(2300);
                        if(kk>1000) break;
                    }
                }
                osw.write("Split and Transfer the file with Chunk_Size "+kk+"......"+" file_id "+SendingFile.substring(0, SendingFile.lastIndexOf("."))+str.substring(4,7)+receiver.substring(4, 7)+SendingFile.substring(SendingFile.lastIndexOf("."),SendingFile.length())+"\n");
                osw.flush();
                
                Total_Chunks=Integer.parseInt(Br.readLine());
                System.out.println("Total Chunks : "+Total_Chunks);
                
                ////////////////////////byte[] myby=new byte[buffer_size];
                //Serevr File Receive from sender
                //FOS=new FileOutputStream(new File("e:/"+SendingFile.substring(3, SendingFile.lastIndexOf(".")).replace('/', '%')+str.substring(4,7)+receiver.substring(4, 7)+SendingFile.substring(SendingFile.lastIndexOf("."),SendingFile.length())),true);
                int ii=1;
                int cntt=1;
                Random raa=new Random();
                int ran_lost;
                //Random Lost Frame Selection
                while(true){
                    ran_lost=raa.nextInt(Total_Chunks)+1;
                    if(ran_lost>=2) break;
                }
                //ran_lost=3;
                System.out.println("Random lost : "+ran_lost);
                Date d1=new Date();
                while(ii<=Total_Chunks){
                    //int szz=Integer.parseInt(Br.readLine());
                    TimeOut=Br.readLine();
                    int flaggy=0;
                    if(TimeOut.equals("Timeout"))
                    {
                        flaggy=1;
                        int dd1=ii+1;
                        //ii--;
                        int divi=(dd1-2)/GBN;
                        divi=(divi+1)*GBN;
                        if(divi<=Total_Chunks){
                            for(int xx=dd1;xx<=divi;xx++)
                            {
                                String discardmsg=Br.readLine();
                                Srv.jTextArea1.append("Frame "+xx+" has been discarded.\n");
                            }
                            Srv.jTextArea1.append("\n");
                        }
                        else
                        {
                            for(int xx=dd1;xx<=Total_Chunks;xx++)
                            {
                                String discardmsg=Br.readLine();
                                Srv.jTextArea1.append("Frame "+xx+" has been discarded.\n");
                            }
                            Srv.jTextArea1.append("\n");
                        }
                        
                    }
                    else{
                        chunk_receive=ii;
                        int szz=Integer.parseInt(TimeOut);
                        byte[] myby;
                        //byte[] myby=new byte[szz];
                        //byte[] myby1=new byte[buffer_size];
                        //FOS=new FileOutputStream(new File("e:/"+SendingFile.substring(3, SendingFile.lastIndexOf("."))+str.substring(4,7)+receiver.substring(4, 7)+Integer.toString(ii)+SendingFile.substring(SendingFile.lastIndexOf("."),SendingFile.length())));
                        
                        //int count=0;
                        //File bitFile=new File("e:/bitFile.txt"+Integer.toString(ii));
                        //PrintWriter out = new PrintWriter("e:/bitFile"+Integer.toString(ii)+".txt");
                        //String bit_pattern=null;
                        
                        //paltano lagbo
//                        while (count!=szz) {
//
//                            byteRead = IS.read(myby);
//                            //System.out.println("byte Read "+byteRead);
//                            //System.out.println("total byte"+myby.length);
////                            
//                            //System.out.print(bit_pattern);
//                            FOS.write(myby, 0, byteRead);
//                            count+=byteRead;
//                            System.out.println("bxddd");
//                            for(int dd=0;dd<byteRead;dd++)
//                            {
//                                System.out.println(myby[dd]);
//                            }
//                        }
                            String returnfrmm=Br.readLine();
                            Date d2=new Date();
                            long differ=(d1.getTime()-d2.getTime());
                            System.out.println("Received Full frame : "+returnfrmm);
                            String realfrm=bitDestuff(returnfrmm.substring(8, returnfrmm.length()-8));
                            System.out.println("Destuffed Frame : "+realfrm);
                            System.out.println("Frame Type : "+realfrm.substring(0, 8));
                            System.out.println("Sequence No : "+realfrm.substring(8, 16));
                            System.out.println("Acknowledgement No :"+realfrm.substring(16, 24));
                            String Main_Msg=realfrm.substring(24, realfrm.length()-8);
                            if(cntt%ran_lost==0)
                            {
                                Main_Msg=Main_Msg+"01110110";
                                System.out.println("Main Message lost : "+Main_Msg);
                            }
                            System.out.println("Main Message : "+realfrm.substring(24, realfrm.length()-8));
                            String Cheks=realfrm.substring(realfrm.length()-8, realfrm.length());
                            System.out.println("Checksum : "+realfrm.substring(realfrm.length()-8, realfrm.length()));
                            //myby=new byte[(realfrm.substring(24, realfrm.length()-8)).length()/8];
                            boolean error=hasChecksumError(Main_Msg,Cheks);
                            System.out.println("Error found : "+error+"\n");
                            if(error==false && differ<=30000 &&  returnfrmm.substring(0, 8).equals("01111110") && returnfrmm.substring(returnfrmm.length()-8, returnfrmm.length()).equals("01111110") && returnfrmm.substring(8, 16).equals("00000000")){
                                //bitPatternToByteArray(realfrm,myby);
                                String mainmsg=realfrm.substring(24, realfrm.length()-8);
                                
//                                BigInteger BI=new BigInteger(mainmsg,2);
//                                myby=BI.toByteArray();

                                StringBuffer sb=new StringBuffer(mainmsg);
                                String srs;
                                int len=sb.length();

                                myby=new byte[len/8];
                                int countt=0;
                                for(int k=0;k<len;k=k+8)
                                {
                                    srs=sb.substring(k,k+8);
                                    int ddx=Integer.parseInt(srs,2);
                                    myby[countt]=(byte)ddx;
                                    //System.out.println(myby[countt]);
                                    countt++;
                                }
                                FOS=new FileOutputStream(new File("e:/"+SendingFile.substring(3, SendingFile.lastIndexOf(".")).replace('/', '%')+str.substring(4,7)+receiver.substring(4, 7)+SendingFile.substring(SendingFile.lastIndexOf("."),SendingFile.length())),true);
                                FOS.write(myby);
                                FOS.flush();
                                FOS.close();
                                
                            }
                            else
                            {
                                //System.out.println("ki hoilo");
                                error=true;
                            }
                            
                            //FOS.write(myby, 0, myby.length);
                            //FOS.write(myby,0,myby.length);
                            ////////////////////////////////////////////////////
                        //out.close();
                        
                        //FOS.close();
                        if(chunk_receive==Total_Chunks && error==false && flaggy==0)
                        {
                            osw.write(AckFrame(realfrm.substring(16,24))+"\n");
                            //osw.write("Last Chunk has been Transferred to Server\n");
                            osw.flush();
                            Srv.jTextArea1.append("Frame "+Integer.parseInt(realfrm.substring(16,24),2)+" has been transmitted.\n");
                            String Complete=Br.readLine();
                            Srv.jTextArea1.append(Complete+"\n");
                            
                        }
                        else if(error==false && flaggy==0){
                            
                            osw.write(AckFrame(realfrm.substring(16,24))+"\n");
                            //osw.write("AckFrame : "+realfrm.substring(16,24)+" : Frame"+ii+" has been transferred to server Successfully. Send next chunk. Time needs to transfer : \n");
                            osw.flush();
                            Srv.jTextArea1.append("Frame "+Integer.parseInt(realfrm.substring(16,24),2)+" has been transmitted.\n");
                            if(ii%GBN==0) Srv.jTextArea1.append("\n");
                        }
                        else if(flaggy==0 && error==true)
                        {
                            int sqq=Integer.parseInt(realfrm.substring(16,24),2)+1;
                            String sq1=String.format("%8s", Integer.toBinaryString(sqq & 0xFF)).replace(' ', '0');
                            osw.write(AckFrame(sq1)+"\n");
                            osw.flush();
                            Srv.jTextArea1.append("Frame "+Integer.parseInt(realfrm.substring(16,24),2)+" has been discarded.\n");
                            int dd=ii+1;
                            ii--;
                            int divi=(dd-2)/GBN;
                            divi=(divi+1)*GBN;
                            if(divi<=Total_Chunks){
                                for(int xx=dd;xx<=divi;xx++)
                                {
                                    String discardmsg=Br.readLine();
                                    Srv.jTextArea1.append("Frame "+xx+" has been discarded.\n");
                            
                                }
                                Srv.jTextArea1.append("\n");
                                
                            }
                            else
                            {
                                for(int xx=dd;xx<=Total_Chunks;xx++)
                                {
                                    String discardmsg=Br.readLine();
                                    Srv.jTextArea1.append("Frame "+xx+" has been discarded.\n");
                            
                                }
                                Srv.jTextArea1.append("\n");
                            }
                        }
                    }
                    if(ii%GBN==0)
                    {
                        d1=new Date();
                    }
                    ii++;
                    cntt++;
                }
                //FOS.close();
                
                if(chunk_receive==Total_Chunks)
                {
//                    int sum=0;
//                    for(int dd=1;dd<=Total_Chunks;dd++)
//                    {
//                        sum=sum+(int)(new File("e:/"+SendingFile.substring(3, SendingFile.lastIndexOf("."))+str.substring(4,7)+receiver.substring(4, 7)+Integer.toString(dd)+SendingFile.substring(SendingFile.lastIndexOf("."),SendingFile.length()))).length();
//                    }
//                    summation=sum;
                    summation=(int)(new File("e:/"+SendingFile.substring(3, SendingFile.lastIndexOf(".")).replace('/', '%')+str.substring(4,7)+receiver.substring(4, 7)+SendingFile.substring(SendingFile.lastIndexOf("."),SendingFile.length()))).length();
                    System.out.println("server "+summation);
                    System.out.println(sz);
                    if(summation==sz){
                
                    //Server send message to receiver client
                        receiverSocket=getSocket(str1);
                        OutputStreamWriter osw1=new OutputStreamWriter(getSocket(str1).getOutputStream());
                        //BufferedReader Br1=new BufferedReader(new InputStreamReader(getSocket(str1).getInputStream()));
                        osw1.write("Hello "+str1+", Are you ready to receive file "+"e:/"+SendingFile.substring(3, SendingFile.lastIndexOf(".")).replace('/', '%')+str.substring(4,7)+receiver.substring(4, 7)+SendingFile.substring(SendingFile.lastIndexOf("."),SendingFile.length())+" from "+str+" Total chunks "+Total_Chunks+" and total size "+summation+"?\n");
                        osw1.flush();
                    }
                    else
                    {
                        //for(int dd=1;dd<=Total_Chunks;dd++)
                        //{
                            (new File("e:/"+SendingFile.substring(3, SendingFile.lastIndexOf("."))+str.substring(4,7)+receiver.substring(4, 7)+SendingFile.substring(SendingFile.lastIndexOf("."),SendingFile.length()))).delete();
                        //}
                        total_size=total_size-sz;
                        Srv.jTextArea1.append("Chunk sizes' summation not equal Total size. All Chunks Deleted\n");
                    
                    }
                }
                
                
             }
             else {
                 total_size=total_size-sz;
                 osw.write("Buffer memory overflow...\n");
                 osw.flush();
             }
             }
             else{
                //String ssd=Br1.readLine();
                //System.out.println("Peraaaaaa");
                if(str1.equals("Yes"))
                {
                    
                    String Rece=Br.readLine();//FileId
                    System.out.println(Rece);
                    int ttl_chnk=Integer.parseInt(Br.readLine());//total chunks
                    Srv.jTextArea1.append("Receiver is ready to receive file. File transfering.... \n");
                    BIS=new BufferedInputStream(new FileInputStream(new File(Rece.substring(0, Rece.lastIndexOf("."))+Rece.substring(Rece.lastIndexOf("."), Rece.length()))));
                    int k=(int)(new File(Rece.substring(0, Rece.lastIndexOf("."))+Rece.substring(Rece.lastIndexOf("."), Rece.length()))).length();
                    if(k%25000==0)
                    {
                        ttl_chnk=k/25000;
                    }
                    else
                    {
                        ttl_chnk=(k/25000)+1;
                    }
                    osw.write(ttl_chnk+"\n");
                        osw.flush();
                    //Server Send
                    for(int ddl=1;ddl<=ttl_chnk;ddl++){
                        //osw.write((int)(new File(Rece.substring(0, Rece.lastIndexOf("."))+Integer.toString(ddl)+Rece.substring(Rece.lastIndexOf("."), Rece.length()))).length()+"\n");//chunk size
                        int readbyte;
                        if(k<25000)
                        {
                            readbyte=k;
                        }
                        else
                        {
                            readbyte=25000;
                        }
                        osw.write(readbyte+"\n");
                        osw.flush();
                        String hh=Br.readLine();
                        //byte[] myby1=new byte[(int)(new File(Rece.substring(0, Rece.lastIndexOf("."))+Integer.toString(ddl)+Rece.substring(Rece.lastIndexOf("."), Rece.length()))).length()];
                        byte[] myby1=new byte[readbyte];
                        //BIS=new BufferedInputStream(new FileInputStream(new File(Rece.substring(0, Rece.lastIndexOf("."))+Integer.toString(ddl)+Rece.substring(Rece.lastIndexOf("."), Rece.length()))));
                        int read=BIS.read(myby1,0,readbyte);
                        k=k-readbyte;
                        assert (read == myby1.length);
                        OS=sc.getOutputStream();
                        OS.write(myby1,0,readbyte);
                        //System.out.println("pathay tohhhhhhhhhh");
                        OS.flush();
                        //OS.close();
                        //BIS.close();
                        //total_size=total_size-(int)(new File(Rece.substring(0, Rece.lastIndexOf("."))+Integer.toString(ddl)+Rece.substring(Rece.lastIndexOf("."), Rece.length()))).length();
                        total_size=total_size-readbyte;
                        
                        //new File(Rece.substring(0, Rece.lastIndexOf("."))+Integer.toString(ddl)+Rece.substring(Rece.lastIndexOf("."), Rece.length())).delete();
                        System.out.println("Total Size : "+total_size);
                        Srv.jTextArea1.append("Chunk deleted\n");
                        hh=Br.readLine();
                    }
                    BIS.close();
                    new File(Rece.substring(0, Rece.lastIndexOf("."))+Rece.substring(Rece.lastIndexOf("."), Rece.length())).delete();
                    
                }
                else if(str1.equals("No"))
                {
                    Srv.jTextArea1.append(receiver+" refused to receive file...... \n");
                }
                
             }
             }
             
         }
         catch(IOException e)
         {
             
             if(chunk_receive<Total_Chunks){
             
                for(int dd=1;dd<=chunk_receive;dd++)
               {
                   (new File("e:/"+SendingFile.substring(3, SendingFile.lastIndexOf("."))+str.substring(4,7)+receiver.substring(4, 7)+Integer.toString(dd)+SendingFile.substring(SendingFile.lastIndexOf("."),SendingFile.length()))).delete();
                   total_size=total_size-File_size;
               }
                Srv.jTextArea1.append(str+" disconnected and All chunks deleted\n");
             }
             
             for(int j=0;j<model.getRowCount();j++)
             {
                 if(model.getValueAt(j, 0).equals(str)==true && getSocket(model.getValueAt(j, 0).toString())==sc)
                 {
                     Srv.jTextArea1.append(str+" disconnected\n");
                     model.removeRow(j);
                     break;
                 }
             }
             if(arra.contains(NS)==true){
                int j=arra.indexOf(NS);
                //System.out.println(j);
                arra.remove(j);
             }
         }
         
    }
    
    

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SERVER");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Online"
            }
        ));
        jScrollPane3.setViewportView(jTable2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(37, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        Srv.setVisible(true);
        try{
            ServerSocket SSoc=new ServerSocket(1193);
            while(true)
            {
                Socket So=SSoc.accept();
                Thread th=new Thread(new Server(So));
                th.start();
            }
        }
        catch(IOException e)
        {
            System.out.println("Error "+e);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    
}
