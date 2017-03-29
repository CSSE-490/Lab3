import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by CJ on 3/29/2017.
 */
public class FastStart {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(args[0]);
        Node left = getNode(args[1]);
        Node right = getNode(args[2]);
        long sleepTime = Long.parseLong(args[3]);

        Runnable r2 = new Server(port);
        Thread t2 = new Thread(r2);
        t2.start();

        Thread.sleep(sleepTime);

        if(Communicator.INSTANCE.leftSocket == null) {
            Socket socket = new Socket(left.host, left.port);

            ClientResponder leftClient = new ClientResponder(socket);
            leftClient.registerAsLeft();
            new Thread(leftClient).start();
        }
        if(Communicator.INSTANCE.rightSocket == null) {
            Socket socket = new Socket(right.host, right.port);

            ClientResponder rightClient = new ClientResponder(socket);
            rightClient.registerAsRight();
            new Thread(rightClient).start();
        }

        System.out.println("Ready for user input");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            String input = reader.readLine();
            switch (input) {
                case "start":
                    Communicator.INSTANCE.leftSocket.sendWakeup();
                    Communicator.INSTANCE.rightSocket.sendWakeup();
                    Philosopher.INSTANCE.wakeUp();
                    break;
            }
        }
    }

    private static Node getNode(String s) {
        String[] array = s.split(":");
        if(array.length != 2) {
            throw new RuntimeException("Invalid Arguments");
        }

        return new Node(array[0], Integer.parseInt(array[1]));
    }
}
