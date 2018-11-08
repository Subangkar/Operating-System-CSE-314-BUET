
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    public ServerSocket servSocket;
    HashMap<String, Information> clientList;
    int Max_Capacity= 0x100;
    Vector<Chunk> server_buffer;
    public Server(int port){
        server_buffer=new Vector<>();
        clientList=new HashMap<String, Information>();

        try {
            servSocket=new ServerSocket(port);

            while(true){
                Socket clientSocket=servSocket.accept();
                ConnectionUtillities connection=new ConnectionUtillities(clientSocket);
                new Thread(new CreateClientConnection(clientList,connection,server_buffer)).start();
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new Server(5000);
    }
}
