import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by CJ on 3/23/2017.
 */
class Client implements Runnable{

    private final int port;

    public Client(int port) {
        this.port = port;
    }


	@Override
	public void run() {
		//all client code here
		//you should have a "left" client connection
		//and a "right" client connection

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
