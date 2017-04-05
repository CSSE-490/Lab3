package network;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by CJ on 3/23/2017.
 */
public class Server implements Runnable {

    public static Server INSTANCE;

    public static void createServer(int port) {
        new Thread(new Server(port)).start();
    }

    private final int port;
    private ServerSocket serverSocket;

    private Server(int port) {
        INSTANCE = this;
        this.port = port;
    }

	@Override
	public void run() {
        serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server Accepting Clients");
        } catch (IOException e) {
            System.err.println("Unable to start local network.Server-network.Server");
            e.printStackTrace();
            System.exit(1);
            return;
        }

        while(true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Client Connected");

                new Thread(new ClientResponder(socket)).start();
            } catch (SocketException e) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
	}

	public static void close() {
        try {
            INSTANCE.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
