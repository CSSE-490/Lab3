package network;

import logic.Philosopher;
import main.Settings;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by CJ on 4/4/2017.
 */
public class ConnectionAttempter {

    public static void attemptConnection(boolean isLeft, Node node) {
        try {
            Socket socket = new Socket(node.host, node.port);

            ClientResponder responder = new ClientResponder(socket);

//            socket.setKeepAlive(true);
//            socket.setSoTimeout(50);

            if(isLeft) {
                responder.registerAsLeft();
            } else {
                responder.registerAsRight();
            }
            new Thread(responder).start();
            System.out.println("Successfull connect to " + isLeft);
        } catch (IOException e) {
            System.err.format("Unable to connect to other client, %s\n", isLeft ? "Left" :  "Right");
        }
    }
}
