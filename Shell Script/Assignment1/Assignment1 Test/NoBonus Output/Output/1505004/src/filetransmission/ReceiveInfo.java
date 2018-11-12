/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransmission;

/**
 *
 * @author Abhishek
 */
public class ReceiveInfo {
    int FromId;
    int ToId;
    int size;
     String FromName;
    String ToName;
      public ReceiveInfo(int FromId, int ToId, String FromName, String ToName,int size) {
        this.FromId = FromId;
        this.ToId = ToId;
        this.FromName = FromName;
        this.ToName = ToName;
        this.size=size;
    }
    public int getFromId() {
        return FromId;
    }

    public void setFromId(int FromId) {
        this.FromId = FromId;
    }

    public int getToId() {
        return ToId;
    }

    public void setToId(int ToId) {
        this.ToId = ToId;
    }

    public String getFromName() {
        return FromName;
    }

    public void setFromName(String FromName) {
        this.FromName = FromName;
    }

    public String getToName() {
        return ToName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setToName(String ToName) {
        this.ToName = ToName;
    }
    
}
