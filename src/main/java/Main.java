import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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

        System.out.println("IP:Port of first Connection");
        String input = reader.readLine();
        Node left = getNode(input);

        System.out.println("IP:Port of the second Connection");
        input = reader.readLine();
        Node right = getNode(input);
        
        Runnable r1 = new Client(left,right);
        Thread t1 = new Thread(r1);
        t1.start();
    }

    private static Node getNode(String s) {
        String[] array = s.split(":");
        if(array.length != 2) {
            throw new RuntimeException("Invalid Arguments");
        }

        return new Node(array[0], Integer.parseInt(array[1]));
    }
}
