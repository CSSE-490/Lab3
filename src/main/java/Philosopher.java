import java.util.Random;

/**
 * Created by CJ on 3/24/2017.
 */
public class Philosopher implements Runnable {
    public static Philosopher INSTANCE;

    static {
        INSTANCE = new Philosopher();
    }

    private volatile boolean hasLeftChopstick;
    private volatile boolean hasRightChopstick;
    private volatile boolean hungry;
    private long timeLastAte;
    private long startedEating = 0L;
    private long startedThinking = 0L;
    private long hungrySince = 0L;

    private Philosopher() {
        this.hasLeftChopstick = false;
        this.hasRightChopstick = false;
        this.hungry = new Random().nextBoolean();
    }

    public synchronized boolean isThinking() {
        return !(hasLeftChopstick || hasRightChopstick) && !hungry;
    }

    public synchronized boolean isEating() {
        return hasLeftChopstick && hasRightChopstick && hungry;
    }

    public synchronized void wakeUp() {
        this.timeLastAte = System.currentTimeMillis();
        if(!this.hungry)
            this.startedThinking = System.currentTimeMillis();

        new Thread(this).start();
        System.out.println("Started philosopher");
    }


    @Override
    public void run() {
        System.out.println("I know who is on my left and right..");
        System.out.println("On my left is " + Communicator.INSTANCE.leftSocket);
        System.out.println("On my right is " + Communicator.INSTANCE.rightSocket);

        while (true) {
            long currentTime = System.currentTimeMillis();
            System.out.format("%d: %b:%b:%b\n", currentTime, this.hungry, this.hasLeftChopstick, this.hasRightChopstick);


            // If eating, reset timestamp
            if (isEating()) this.timeLastAte = currentTime;

            if (this.timeLastAte + 4000L < currentTime) {
                System.err.println("I starved");
                System.exit(1);
            }


            if (isEating() && 1000L + startedEating > currentTime) { // check how long we've been eating and stop if necessary
                synchronized (this) {
                    this.hasLeftChopstick = false;
                    this.hasRightChopstick = false;
                    this.hungry = false;
                    this.startedThinking = currentTime;
                }
                System.out.println("Finished eating, thinking now.");
            } else if (!this.hungry && 1000L + startedThinking > currentTime) { // Don't think for more than 1 second
                synchronized (this) {
                    this.hungry = true;
                    this.hungrySince = currentTime;
                }
                System.out.println("Done thinking, now hungry");
            } else if (this.hungry && !this.isEating() && hungrySince + 100L > currentTime) {
                synchronized (this) {
                    hasLeftChopstick = false;
                    hasRightChopstick = false;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }

            // If hungry but not eating, try to take chopsticks
            if (!isEating() && this.hungry) {
                synchronized (this) {
                    if (!hasLeftChopstick) Communicator.INSTANCE.leftSocket.requestChopstick();
                    if (!hasRightChopstick) Communicator.INSTANCE.rightSocket.requestChopstick();
                }
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public synchronized boolean requestChopstick(boolean isLeft) {
        if (isLeft) return !this.hasLeftChopstick;
        else return !this.hasRightChopstick;
    }

    public synchronized void takeChopstick(boolean isLeft) {
        if (!this.hungry)
            return;

        if (isLeft) this.hasLeftChopstick = true;
        else this.hasRightChopstick = true;

        if (isEating()) {
            startedEating = System.currentTimeMillis();
        }
    }

    public synchronized void dropChopstick(boolean isLeft) {
        if(isLeft) {
            this.hasLeftChopstick = false;
        } else {
            this.hasRightChopstick = false;
        }
    }
}
