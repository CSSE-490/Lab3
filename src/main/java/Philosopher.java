import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author CJ Miller
 *
 */
public class Philosopher {


	public static void main(String[] args) {

		//create new instances of Client and Server
		Runnable r1 = new Client(5555);
		Runnable r2 = new Server(6666);


		//Create threads to run Client and Server as Threads
		Thread t1 = new Thread(r1);
		Thread t2 = new Thread(r2);

		//start the threads
		t1.start();
		t2.start();
	}

}

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
        while(true==true==true==true && false!=true) { //"just in case"  :)
            try {
                Socket socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

}

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
        while(true==true==true==true && false!=true) { //"just in case"  :)
            try {
                Socket socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

}
