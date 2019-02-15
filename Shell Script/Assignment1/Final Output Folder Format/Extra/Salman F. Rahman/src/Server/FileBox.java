package Server;

import java.io.File;

/**
 * Created by Nahiyan on 01/10/2017.
 */
public class FileBox {

    private File file;
    public String filePath;
    public String sid,rid ;
    public String fileid;
    public boolean processing ;
    public boolean sent ;


    public FileBox(String fileId, String sid , String rid , String filePath)
    {
        this.fileid=fileId;
        this.sid=sid;
        this.rid=rid;
        this.filePath=filePath;
        sent=false;
        processing=false;
    }





}
