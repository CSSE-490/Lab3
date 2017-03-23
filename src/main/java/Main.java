/**
 * Created by CJ on 3/23/2017.
 */
public class Main {
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
