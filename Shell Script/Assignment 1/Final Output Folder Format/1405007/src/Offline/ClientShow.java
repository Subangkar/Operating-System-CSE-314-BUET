
package Offline;

/**
 *
 * @author Rifat
 */
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClientShow extends javax.swing.JFrame {

    public Socket scc;
    public BufferedReader Br;
    public OutputStreamWriter osw;
    public String stri;
    public String strig;
    
    public static int sequenceNo=0;
    
    public int Chunk_Size;
    public int File_Size;
    public String File_Id;
    public int Total_Chunks;
    public int GBN=8;
    
    public String FN,txt;
    public int SendingChunkNum=0;
    
    public ClientShow() {
        initComponents();
    }
    public ClientShow(String ss,Socket s)
    {
        initComponents();
        scc=s;
        stri=ss;
        try{
        Br=new BufferedReader(new InputStreamReader(s.getInputStream()));
        //os=s.getOutputStream();
        osw=new OutputStreamWriter(s.getOutputStream());
        String str=Br.readLine();
        jTextArea1.append(str+"\n");
        }catch(IOException e)
        {
            
        }
        
    }
    
    public void AckThread() 
    {
         Thread recei = new Thread(new ReceiveRd());
         recei.start();
    }
    
    public class ReceiveRd implements Runnable
    {
        @Override
        public void run() 
        {
            try 
            {
                
           }catch(Exception ex) { }
        }
    }
    
    public int Chunking(String FileName,String FileId,int File_size,int Chunk_size)
    {
        File nwFl;
        FileOutputStream filePart;
        int fileSize = File_size;
        int nChunks=0,read, readLength;
        byte[] byteChunkPart;
        try {
            FileInputStream inputStream = new FileInputStream(new File(FileName));
            nChunks=0;
            read=0;
            readLength=0;
            while (fileSize > 0) {
                if (fileSize <Chunk_size) {
                    readLength = fileSize;
                }
                else
                {
                    readLength=Chunk_size;
                }
                byteChunkPart = new byte[readLength];
                read = inputStream.read(byteChunkPart, 0, readLength);
                fileSize =fileSize- read;
                assert (read == byteChunkPart.length);
                nChunks++;
                nwFl = new File (FileId.substring(0, FileId.lastIndexOf("."))+Integer.toString(nChunks)+FileId.substring(FileId.lastIndexOf("."), FileId.length()));
                filePart = new FileOutputStream(nwFl);
                filePart.write(byteChunkPart,0,read);
                filePart.flush();
                filePart.close();
                byteChunkPart = null;
            }
            inputStream.close();
            //return nChunks;
        } catch (IOException exception) {
            System.out.println(exception);
        }
        return nChunks;
    }
    
    //byte to bit pattern
    public String byteTobit(byte[] ara,int len)
    {
        String s1,s2="";
        for(int i=0;i<len;i++)
        {
            s1 = String.format("%8s", Integer.toBinaryString(ara[i] & 0xFF)).replace(' ', '0');

            s2=s2+s1;

        }
        return s2;
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
    
    //To Evaluate Checksum
    public String getChecksum(String payload)
    {
        StringBuffer sb=new StringBuffer(payload);
        int len=sb.length();
        int ones=OneCount(payload);
        ones=ones%1024;
        String s1 = String.format("%10s", Integer.toBinaryString(ones & 0x3FF)).replace(' ', '0');

        //System.out.println(s1);

        String LS8bit=s1.substring(2, 10);
        String MS2bit=s1.substring(0, 2);

        //System.out.println(LS8bit+"  "+MS2bit);

        int ls8=Integer.parseInt(LS8bit, 2);
        int ms2=Integer.parseInt(MS2bit,2);

        int sum=ls8+ms2;
        int out;
        s1 = String.format("%9s", Integer.toBinaryString(sum & 0x1FF)).replace(' ', '0');
        if(s1.charAt(0)=='1')
        {
             LS8bit=s1.substring(1, 9);
            MS2bit=s1.substring(0, 1);

            ls8=Integer.parseInt(LS8bit, 2);
            ms2=Integer.parseInt(MS2bit, 2);

            sum=ls8+ms2;
            s1 = String.format("%8s", Integer.toBinaryString(sum & 0xFF)).replace(' ', '0');
            s1=Complement(s1);
            out=Integer.parseInt(s1,2);
        }
        else
        {
            s1 = String.format("%8s", Integer.toBinaryString(sum & 0xFF)).replace(' ', '0');
            s1=Complement(s1);
            out=Integer.parseInt(s1,2);

        }
        //return out;
        return s1;
    }
    
    //Frame
    public String frame(String kind,String seqNo,String AckNo,String payload,String Checksum)
    {
        String out=""+kind+seqNo+AckNo+payload+Checksum;
        return out;
    }
    
    
    //final frame
    public String realframe(String flag1,String stuffedString,String flag2)
    {
        String out=""+flag1+stuffedString+flag2;
        return out;
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Home");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jButton1.setText("Log out");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Receiver Name");

        jButton2.setText("Select Receiver");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel2.setText("File Name");

        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jButton3.setText("Send");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(0, 102, 255));
        jButton4.setText("Transfer File");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setText("Refresh");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Yes", "No" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel3.setText("Receive File");

        jButton6.setText("Split");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton5))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton2)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jButton4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jButton6))
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton3))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton5)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jButton2)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jButton3))
                .addGap(42, 42, 42)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton6)
                    .addComponent(jButton4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //After logout 
        System.exit(1);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //Receiver ID input
        try {
            osw.write(jTextField1.getText()+"\n");
            osw.flush();
            String as=Br.readLine();
            jTextArea1.append(as+"\n");
        } catch (IOException ex) {
            Logger.getLogger(ClientShow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        //File name input and send
        try {
            FN=jTextField2.getText();
            int sz=(int)(new File(FN)).length();
            File_Size=sz;
            
            if(sz>0){
                osw.write(jTextField2.getText()+"?"+sz+"\n");
                osw.flush();
                //System.out.println("Bhoot");
                txt=Br.readLine();
                jTextArea1.append(txt+"\n");
            }
            else
            {
                jTextArea1.append("File Not Exist. Re-enter the valid file\n");
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ClientShow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

       
    
    
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        //Transfer File
        BufferedInputStream BIS=null;
        System.out.println(txt.substring(0,17));
        try{
            if(txt.substring(0,27).compareToIgnoreCase("Split and Transfer the file")==0)
            {   
                Date d1=new Date();
                
                while(txt.substring(0,27).compareToIgnoreCase("Split and Transfer the file")==0){
                //File Sender to Server e jabe
                //Date d1=new Date();
                Socket sk=scc;
                SendingChunkNum++;
                if(SendingChunkNum>Total_Chunks)
                {
                    jTextArea1.append("There is no Chunks anymore\n");
                    SendingChunkNum=0;
                    break;
                    
                }
                else{
                    osw.write((int)(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(SendingChunkNum)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))).length()+"\n");
                    osw.flush();
                    
                    System.out.println(SendingChunkNum);
                    System.out.println(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(SendingChunkNum)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()));
                    byte[] myby=new byte[(int)(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(SendingChunkNum)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))).length()];
                    BIS=new BufferedInputStream(new FileInputStream(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(SendingChunkNum)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))));
                    
                    BIS.read(myby,0,myby.length);
                    sequenceNo++;
                    String payld=byteTobit(myby,myby.length);
                    System.out.println("Main Message : "+payld);
                    String sequence=String.format("%8s", Integer.toBinaryString(SendingChunkNum & 0xFF)).replace(' ', '0');
                    System.out.println("Sequence No : "+sequence);
                    String typeFrame="00000000";
                    System.out.println("Frame Type : "+typeFrame);
                    String chcksm=getChecksum(payld);
                    System.out.println("Checksum : "+chcksm);
                    String frmm=frame(typeFrame,sequence,sequence,payld,chcksm);
                    System.out.println("Frame : "+frmm);
                    String Stuffedfrmm=bitStuff(frmm);
                    System.out.println("Bit Stuffed Frame : "+Stuffedfrmm);
                    String delimited="01111110";
                    String finalfrmm=realframe(delimited,Stuffedfrmm,delimited);
                    System.out.println("Final Frame : "+finalfrmm);
                    
                    osw.write(finalfrmm+"\n");
                    osw.flush();
                    ////////////////////////////////////////////////////////////////////////////
                    
//                    OutputStream os=sk.getOutputStream();
//                    os.write(myby,0, myby.length);
//                    os.flush();
                    
                    BIS.close();
                    
                    String msgg;
                    
                    //Waiting For Acknowledgement
                    msgg=Br.readLine();
                    Date d2=new Date();
                    
                    msgg=bitDestuff(msgg.substring(8, msgg.length()-8));
                    long diff=(d2.getTime()-d1.getTime());
                    if(msgg.substring(8,msgg.length()).equals(sequence)==true && SendingChunkNum==Total_Chunks)
                    {
                        jTextArea1.append("AckNo "+msgg.substring(8,msgg.length())+" : Last Frame has been Transferred to Server in "+diff+" miliseconds.\n");
                        System.out.println("AckNo "+msgg.substring(8,msgg.length())+" : Last Frame has been Transferred to Server.");
                       
                    }
                    else if(msgg.substring(8,msgg.length()).equals(sequence)==true){
                        jTextArea1.append("AckNo "+msgg.substring(8,msgg.length())+" : Frame"+SendingChunkNum+" has been transferred to server Successfully. Send next chunk. Time needs to transfer : in "+diff+" miliseconds.\n");
                        System.out.println("AckNo "+msgg.substring(8,msgg.length())+" : Frame"+SendingChunkNum+" has been transferred to server Successfully. Send next chunk.");
                    
                    }
                    
                    if(msgg.substring(8,msgg.length()).equals(sequence)==false)
                    {
                        //txt="";
                        int nxtfrm=SendingChunkNum+1;
                        SendingChunkNum--;
                        int divi=(nxtfrm-2)/GBN;
                        divi=(divi+1)*GBN;
                        if(divi<=Total_Chunks){
                            for(int dd=nxtfrm;dd<=divi;dd++)
                            {
                                byte[] myby1=new byte[(int)(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(dd)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))).length()];
                                BIS=new BufferedInputStream(new FileInputStream(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(dd)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))));

                                BIS.read(myby1,0,myby1.length);
                                //sequenceNo++;
                                String payld1=byteTobit(myby1,myby1.length);
                                String sequence1=String.format("%8s", Integer.toBinaryString(dd & 0xFF)).replace(' ', '0');
                                String typeFrame1="00000000";
                                String chcksm1=getChecksum(payld1);
                                String frmm1=frame(typeFrame1,sequence1,sequence1,payld1,chcksm1);
                                //System.out.println(frmm);
                                String Stuffedfrmm1=bitStuff(frmm1);
                                //System.out.println(Stuffedfrmm);
                                String delimited1="01111110";
                                String finalfrmm1=realframe(delimited1,Stuffedfrmm1,delimited1);
                                //System.out.println(finalfrmm);

                                osw.write(finalfrmm1+"\n");
                                osw.flush();
                                ////////////////////////////////////////////////////////////////////////////
                                BIS.close();
                            }
                            while(true)
                            {
                                Date d33=new Date();
                                if(d33.getTime()-d1.getTime()>30000) break;
                            }
                            d1=new Date();
                        }
                        
                        else
                        {
                            for(int dd=nxtfrm;dd<=Total_Chunks;dd++)
                            {
                                byte[] myby1=new byte[(int)(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(dd)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))).length()];
                                BIS=new BufferedInputStream(new FileInputStream(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(dd)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))));

                                BIS.read(myby1,0,myby1.length);
                                //sequenceNo++;
                                String payld1=byteTobit(myby1,myby1.length);
                                String sequence1=String.format("%8s", Integer.toBinaryString(dd & 0xFF)).replace(' ', '0');
                                String typeFrame1="00000000";
                                String chcksm1=getChecksum(payld1);
                                String frmm1=frame(typeFrame1,sequence1,sequence1,payld1,chcksm1);
                                //System.out.println(frmm);
                                String Stuffedfrmm1=bitStuff(frmm1);
                                //System.out.println(Stuffedfrmm);
                                String delimited1="01111110";
                                String finalfrmm1=realframe(delimited1,Stuffedfrmm1,delimited1);
                                //System.out.println(finalfrmm);

                                osw.write(finalfrmm1+"\n");
                                osw.flush();
                                ////////////////////////////////////////////////////////////////////////////
                                BIS.close();
                            }
                            while(true)
                            {
                                Date d33=new Date();
                                if(d33.getTime()-d1.getTime()>30000) break;
                            }
                            d1=new Date();
                        }
                        
                    }
                    else if(diff>30000)
                    {
                        //jTextArea1.append("File cannot be transferred\n");
                        osw.write("Timeout\n");
                        osw.flush();
                        int nxtfrm=SendingChunkNum+1;
                        //SendingChunkNum--;
                        int divi=(nxtfrm-2)/GBN;
                        divi=(divi+1)*GBN;
                        if(divi<=Total_Chunks){
                            for(int dd=nxtfrm;dd<=divi;dd++)
                            {
                                byte[] myby1=new byte[(int)(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(dd)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))).length()];
                                BIS=new BufferedInputStream(new FileInputStream(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(dd)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))));

                                BIS.read(myby1,0,myby1.length);
                                //sequenceNo++;
                                String payld1=byteTobit(myby1,myby1.length);
                                String sequence1=String.format("%8s", Integer.toBinaryString(dd & 0xFF)).replace(' ', '0');
                                String typeFrame1="00000000";
                                String chcksm1=getChecksum(payld1);
                                String frmm1=frame(typeFrame1,sequence1,sequence1,payld1,chcksm1);
                                //System.out.println(frmm);
                                String Stuffedfrmm1=bitStuff(frmm1);
                                //System.out.println(Stuffedfrmm);
                                String delimited1="01111110";
                                String finalfrmm1=realframe(delimited1,Stuffedfrmm1,delimited1);
                                //System.out.println(finalfrmm);

                                osw.write(finalfrmm1+"\n");
                                osw.flush();
                                ////////////////////////////////////////////////////////////////////////////
                                BIS.close();
                            }
                            while(true)
                            {
                                Date d33=new Date();
                                if(d33.getTime()-d1.getTime()>30000) break;
                            }
                            d1=new Date();
                        }
                        else
                        {
                            for(int dd=nxtfrm;dd<=Total_Chunks;dd++)
                            {
                                byte[] myby1=new byte[(int)(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(dd)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))).length()];
                                BIS=new BufferedInputStream(new FileInputStream(new File(File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(dd)+File_Id.substring(File_Id.lastIndexOf("."),File_Id.length()))));

                                BIS.read(myby1,0,myby1.length);
                                //sequenceNo++;
                                String payld1=byteTobit(myby1,myby1.length);
                                String sequence1=String.format("%8s", Integer.toBinaryString(dd & 0xFF)).replace(' ', '0');
                                String typeFrame1="00000000";
                                String chcksm1=getChecksum(payld1);
                                String frmm1=frame(typeFrame1,sequence1,sequence1,payld1,chcksm1);
                                //System.out.println(frmm);
                                String Stuffedfrmm1=bitStuff(frmm1);
                                //System.out.println(Stuffedfrmm);
                                String delimited1="01111110";
                                String finalfrmm1=realframe(delimited1,Stuffedfrmm1,delimited1);
                                //System.out.println(finalfrmm);

                                osw.write(finalfrmm1+"\n");
                                osw.flush();
                                ////////////////////////////////////////////////////////////////////////////
                                BIS.close();
                            }
                            while(true)
                            {
                                Date d33=new Date();
                                if(d33.getTime()-d1.getTime()>30000) break;
                            }
                            d1=new Date();
                        }
                    }
                    
                    if(SendingChunkNum==Total_Chunks)
                    {
                        osw.write("All Chunks transmission completed\n");
                        osw.flush();
                    }
                }
                if(SendingChunkNum%GBN==0)
                {
                    while(true)
                    {
                        Date d353=new Date();
                        if(d353.getTime()-d1.getTime()>30000) break;
                    }
                    
                    d1=new Date();
                }
                }
                for(int jj=1;jj<=Total_Chunks;jj++)
                {
                    new File (File_Id.substring(0, File_Id.lastIndexOf("."))+Integer.toString(jj)+File_Id.substring(File_Id.lastIndexOf("."), File_Id.length())).delete();
                }
            }
            
        }catch(IOException e)
        {
            
        }
        
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
       //Refresh
        try {
            strig=null;
           
                    
            if((strig=Br.readLine())!=null)
            {
                jTextArea1.append(strig+"\n");
            }
        } catch (IOException ex) {
            Logger.getLogger(ClientShow.class.getName()).log(Level.SEVERE, null, ex);
        }
        //ListenThread();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        //yes or no
        try{
            int byteRead;
        if(jComboBox1.getSelectedItem().toString().equals("Yes")==true)
        {
            osw.write("Yes\n");
            osw.flush();
            
            osw.write(strig.substring(strig.indexOf("file")+5,strig.indexOf("from")-1)+"\n");//FileID
            osw.flush();
            String Receive_Fl_id=strig.substring(strig.indexOf("file")+5,strig.indexOf("from")-1);
            
            osw.write(strig.substring(strig.indexOf("chunks")+7,strig.indexOf("and")-1)+"\n");//Total Chunks
            osw.flush();
            int total_chnk=Integer.parseInt(strig.substring(strig.indexOf("chunks")+7,strig.indexOf("and")-1));
            System.out.println("total chunks : "+total_chnk);
            int cou=0;
            InputStream IS=scc.getInputStream();
            
            FileOutputStream FOS=new FileOutputStream(new File("f:/"+Receive_Fl_id.substring(3, Receive_Fl_id.lastIndexOf("."))+Receive_Fl_id.substring(Receive_Fl_id.lastIndexOf("."), Receive_Fl_id.length())),true);
            total_chnk=Integer.parseInt(Br.readLine());
            for(int ddl=1;ddl<=total_chnk;ddl++){
                
                //System.out.println(" CHoleeeeeeee");
                //FileOutputStream FOS=new FileOutputStream(new File("f:/"+Receive_Fl_id.substring(3, Receive_Fl_id.lastIndexOf("."))+Integer.toString(ddl)+Receive_Fl_id.substring(Receive_Fl_id.lastIndexOf("."), Receive_Fl_id.length())));
                
                //System.out.println("Choleeeeeeeeeee");
                int count=0;
                byteRead=0;
                int sz=Integer.parseInt(Br.readLine());
                System.out.println("CHunk sizee "+sz);
                byte[] myby1=new byte[sz];
                osw.write("Done\n");
                osw.flush();
                
                //while(count!=sz)
                //{
                byteRead=IS.read(myby1);
                System.out.println(byteRead);
                FOS.write(myby1, 0, byteRead);
                  //count+=byteRead;
                //}
                osw.write("Done\n");
                osw.flush();
//                FOS.close();
                cou++;
            }
            FOS.close();
            if(cou==total_chnk){
                jTextArea1.append("File has been transferred to Client\n");
            }
            
        }
        else
        {
            osw.write("No\n");
            osw.flush();
        }
        }catch(IOException e)
        {
            
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        //Splitting
        try{
            if(txt.substring(0,27).compareToIgnoreCase("Split and Transfer the file")==0)
            {   
                Chunk_Size=Integer.parseInt(txt.substring(txt.indexOf("Size")+5, txt.indexOf("......")));
                File_Id=txt.substring(txt.indexOf("file_id")+8, txt.length());
                //Eikhane File Chunk hobe
                Total_Chunks=Chunking(FN,File_Id,File_Size,Chunk_Size);
                osw.write(Total_Chunks+"\n");
                osw.flush();
            }
            else
            {
                jTextArea1.append("Nothing to get Chunking\n");
            }
        }catch(IOException e)
        {
            
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
