/**
 * Created by CJ on 3/24/2017.
 */
public class Philosopher implements Runnable {
    public static Philosopher INSTANCE;

    static {
        INSTANCE = new Philosopher();
    }

    public volatile boolean hasLeftChopstick;
    public volatile boolean hasRightChopstick;
    public volatile boolean hungry;

    private Philosopher() {
        this.hasLeftChopstick = false;
        this.hasRightChopstick = false;
    }

    public synchronized boolean isThinking() {
        return !(hasLeftChopstick || hasRightChopstick) && !hungry;
    }

    public synchronized boolean isEating() {
        return hasLeftChopstick && hasRightChopstick && hungry;
    }

    public void wakeUp() {
        new Thread(this).start();
    }


    @Override
    public void run() {
        System.out.println("I know who is on my left and right..");
        System.out.println("On my left is " + Communicator.INSTANCE.leftSocket);
        System.out.println("On my right is " + Communicator.INSTANCE.rightSocket);
    }
}
