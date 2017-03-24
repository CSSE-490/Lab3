import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            System.out.println("Server Accepting Clients");
        } catch (IOException e) {
            System.err.println("Unable to start local Server-Server");
            e.printStackTrace();
            System.exit(1);
            return;
        }

        while(true) {
            try {
                Socket socketFirst = serverSocket.accept();
                System.out.println("First Socket Connected");
                Socket socketSecond = serverSocket.accept();
                System.out.println("Second Socket Connected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

	private class ClientResponder implements  Runnable {

        private final BufferedReader reader;

        public ClientResponder(Socket socket) throws IOException {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            while(true) {
                try {
                    String messageRaw = reader.readLine();

                    Message message = Message.valueOf(messageRaw);

                    switch(message) {
                        case DO_YOU_HAVE_LEFT_CHOPSTICK:

                            break;
                        case DO_YOU_HAVE_RIGHT_CHOPSTICK:
                            break;
                        case I_AM_LEFT:
                            break;
                        case I_AM_RIGHT:
                            break;
                        case I_HAVE_LEFT_CHOPSTICK:
                            break;
                        case I_HAVE_RIGHT_CHOPSTICK:
                            break;
                        default:
                            System.err.println("Received invalid message from a client");
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
