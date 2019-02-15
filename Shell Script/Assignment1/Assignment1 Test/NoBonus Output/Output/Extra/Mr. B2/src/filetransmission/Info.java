/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filetransmission;

import java.net.Socket;

/**
 *
 * @author Abhishek
 */
public class Info {
    int id;
    Socket socket;

    public Info(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
}
