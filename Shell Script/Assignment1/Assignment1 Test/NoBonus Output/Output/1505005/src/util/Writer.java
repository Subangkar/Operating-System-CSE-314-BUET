/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.FileOutputStream;

/**
 *
 * @author uesr
 */
public class Writer implements Runnable{
    private ConnectionUtillities connection;
    private FileInfo fileInfo;
    private FileSender fileSender;
    private String destDir = "F:/Studies/Receiving/";
    private File dstFile = null;
    private FileOutputStream fileOutputStream;

    public Writer(ConnectionUtillities con){
        connection=con;
    }

    public void downloadFile(byte[] fileData)  {
        String sourceFileName = fileInfo.getFileName().substring(fileInfo.getFileName().lastIndexOf("/") + 1, fileInfo.getFileName().length());
        String outputFile = destDir + sourceFileName;
        if (!new File(destDir).exists()) {
            new File(destDir).mkdirs();
        }
        dstFile = new File(outputFile);
        try {
            fileOutputStream = new FileOutputStream(dstFile);
            fileOutputStream.write(fileData);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Output file : " + outputFile + " is successfully saved ");
    }
    @Override
    public void run() {
        
        while(true){
            Object o=connection.read();
            if(o instanceof String) {
                String str = o.toString();
                System.out.println(str);

                if(str.equals("Acknowledged")) {
                    Object ob = connection.read();
                    if(ob instanceof byte[])   {
                        byte[] arr = (byte[]) ob;
                        if((int)arr[0] == 126 && (int)arr[4] == 126 && (int)arr[1] == 0)    {
                            if((int)arr[3] > 0) {
                                fileSender.setAcknowledgeStatus(((int) arr[2] - 1) % 8);
                            }   else    {System.out.println("Frame " + (int)arr[2] + " was not acknowledged");}
                        }
                        else    {
                            System.out.println("Error occurred in frame delimiter");
                        }
                     //   System.out.println(((Integer) ob).intValue() + " ");
                    }
                }
                else if(str.equals("Start sending the file now"))   {
                    fileSender = new FileSender(connection, fileInfo);
                    new Thread(fileSender).start();
                }
            }
            else if(o instanceof FileInfo)  {
                this.fileInfo = (FileInfo) o;
            }
            else if(o instanceof byte[])    {
                System.out.println("Receiver received a byte array of " + fileInfo.getFileName() + " and fileSize is : " + fileInfo.getFileSize());
                Object temp = connection.read();
                if(temp instanceof String)  {
                    if(temp.toString().equalsIgnoreCase("yes"))   {
                        System.out.println("Receiver wants to receive the file");
                        this.downloadFile((byte[]) o);
                    }
                    else if(temp.toString().equalsIgnoreCase("no"))    {
                        System.out.println("Receiver declines to receive the file, file is discarded.");
                    }
                }
            }
        }
    }
}
