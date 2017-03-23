/**
 * Created by CJ on 3/23/2017.
 */
public class Main {
    public static void main(String[] args) {

        int serverPort = 5555;

        //create new instances of Client and Server
        Runnable r1 = new Client(null,null);
        Runnable r2 = new Server(serverPort);


        //Create threads to run Client and Server as Threads
        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);

        //start the threads
        t1.start();
        t2.start();
    }
}
