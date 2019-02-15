import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Controller{
    @FXML
    TextArea log;
}


class ServerWriter {

    Hashtable<SocketChannel, byte[]> taskList = new Hashtable<>();
    Server server;

    ServerWriter(Server s) {
        server = s;
    }

    void add(SocketChannel s, byte[] data) {
        taskList.put(s, data);
        try {
            s.register(server.selector, SelectionKey.OP_WRITE);
            //server.selector.wakeup();
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }

    }

    byte[] get(SocketChannel s) {
        return taskList.get(s);
    }

}
