import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by CJ on 3/23/2017.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Local Server Port");


        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String port = reader.readLine();

        int serverPort = Integer.parseInt(port);
        Runnable r2 = new Server(serverPort);
        Thread t2 = new Thread(r2);
        t2.start();

        System.out.println("Press enter to proceed to connection input.");
        reader.readLine();

        try {
            if(Communicator.INSTANCE.leftSocket == null) {
                System.out.println("IP:Port of left Connection");
                String input = reader.readLine();
                Node left = getNode(input);


                Socket socket = new Socket(left.host, left.port);

                ClientResponder leftClient = new ClientResponder(socket);
                leftClient.registerAsLeft();
                new Thread(leftClient).start();
            }
            if(Communicator.INSTANCE.rightSocket == null) {
                System.out.println("IP:Port of the right Connection");
                String input = reader.readLine();
                Node right = getNode(input);

                Socket socket = new Socket(right.host, right.port);

                ClientResponder rightClient = new ClientResponder(socket);
                rightClient.registerAsRight();
                new Thread(rightClient).start();
            }
        } catch (UnknownHostException e) {
            System.err.println("Invalid Arguments");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start Philosopher Code
        //Philosopher.INSTANCE.wakeUp();

        System.out.println("Ready for user input");
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
