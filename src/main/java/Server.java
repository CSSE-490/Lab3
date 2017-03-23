import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by CJ on 3/23/2017.
 */
class Server implements Runnable{

    private final int port;

    public Server(int port) {
        this.port = port;
    }

	@Override
	public void run() {
		//all server code here
		//you should have a "left" server connection
		//and a "right" server connection

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Unable to start local Server-Server");
            e.printStackTrace();
            return;
        }

        while(true) {
            try {
                Socket socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

}
