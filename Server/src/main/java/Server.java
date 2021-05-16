import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public Server(){
        System.out.println("Server has started to work...");
        ExecutorService service = Executors.newFixedThreadPool(4);
        System.out.println("Ready to accept new connections...");
        try(ServerSocket server = new ServerSocket(6789)){
            while(true){
                Socket ClientSocket = server.accept();
                System.out.println("Client connected: " + ClientSocket.getInetAddress());
                service.execute(new ClientHandler(ClientSocket));
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
