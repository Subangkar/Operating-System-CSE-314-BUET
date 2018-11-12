
package util;

import java.io.Serializable;

/**
 * Created by Aaiyeesha Mostak on 9/29/2017.
 */
public class FileInfo implements Serializable{
    private String fileName;
    private long fileSize;
    private int fileID;
    private int chunkSize;
    private int noOfChunks;
    private int lastChunkSize;

    public FileInfo(String fileName, long fileSize)
    {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public int getLastChunkSize() {
        return lastChunkSize;
    }

    public void setLastChunkSize() {
        this.lastChunkSize = (int) (fileSize - (noOfChunks - 1)* chunkSize);
    }

    public int getChunkSize() {
        if(noOfChunks == 1) return lastChunkSize;
        else    return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getNoOfChunks() {
        return noOfChunks;
    }

    public void setNoOfChunks(int noOfChunks) {
        this.noOfChunks = noOfChunks;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }
}
