import java.util.Random;

/**
 * Created by CJ on 3/24/2017.
 */
public class Philosopher implements Runnable {

    public static Philosopher INSTANCE;

    static {
        INSTANCE = new Philosopher();
    }

    private long starvationTime = 4000L;
    private volatile boolean hasLeftChopstick;
    private volatile boolean hasRightChopstick;
    private volatile boolean hasCup;
    private volatile boolean hungry;
    private volatile boolean thirsty;


    private long timeLastAte;
    private long startedEating = 0L;
    private long startedThinking = 0L;
    private long startedChopstickAttempt = 0L;
    private long passedOutAt;
    private long startedDrinking;
    private boolean awake;
    private boolean isPassedOut;

    private Philosopher() {
        this.hasLeftChopstick = false;
        this.hasRightChopstick = false;
        this.hungry = new Random().nextBoolean();
        this.awake = false;
    }

    public synchronized boolean isAwake() {
        return this.awake;
    }

    public void setStarvationTime(long starvationTime) {
        this.starvationTime = starvationTime;
    }

    public synchronized boolean isEating() {
        return hasLeftChopstick && hasRightChopstick && hungry;
    }

    public synchronized void wakeUp() {
        this.timeLastAte = System.currentTimeMillis();
        if (this.hungry) {
            this.startedChopstickAttempt = System.currentTimeMillis();
        } else {
            this.startedThinking = System.currentTimeMillis();
        }

        new Thread(this).start();
        this.awake = true;
        System.out.println("Started philosopher");
    }


    @Override
    public void run() {
        System.out.println("I know who is on my left and right..");
        System.out.println("On my left is " + Communicator.INSTANCE.leftSocket);
        System.out.println("On my right is " + Communicator.INSTANCE.rightSocket);

        while (true) {
            long currentTime = System.currentTimeMillis();

            debugPrint(currentTime);

            if(isPassedOut) {
                if(Math.random() * 1000.0 < 1) {
                    isPassedOut = false;
                    // Reconnection stuff




                    long delta = currentTime - passedOutAt;

                    timeLastAte += delta;
                    startedEating += delta;
                    startedThinking += delta;
                }
            } else {
                dinningPhilosopher(currentTime);
                drinkingPhilosopher(currentTime);
            }

            try {
                Thread.sleep(Math.round(Math.ceil(starvationTime / 4000.0)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void drinkingPhilosopher(long currentTime) {
        if(hasCup && startedDrinking + starvationTime < currentTime) {
            passOut(currentTime);
        }
    }

    private void passOut(long currentTime) {
        synchronized (this) {
            hasLeftChopstick = false;
            hasRightChopstick = false;
            hasCup = false;
            isPassedOut = true;
            passedOutAt = currentTime;
            Communicator.INSTANCE.close();
        }
    }

    private void dinningPhilosopher(long currentTime) {
        // If eating, reset timestamp
        if (isEating()) this.timeLastAte = currentTime;

        if (this.timeLastAte + starvationTime < currentTime) {
            System.err.println("I starved");
            System.exit(1);
        }

        if (shouldStopEating(currentTime)) { // check how long we've been eating and stop if necessary
            nowThinking(currentTime);
        } else if (shouldStopThinking(currentTime)) { // Don't think for more than 1 second
            this.nowHungry(currentTime);
        } else if (shouldWaitABit(currentTime)) {
            waitABit();
        }

        // If hungry but not eating, try to take chopsticks
        if (!isEating() && this.hungry) {
            synchronized (this) {
                if (!hasLeftChopstick) Communicator.INSTANCE.leftSocket.requestChopstick();
                if (!hasRightChopstick) Communicator.INSTANCE.rightSocket.requestChopstick();
            }
        }
    }

    private void debugPrint(long currentTime) {
//        System.out.format("Current Time: %d\n", currentTime);
//        System.out.format("Is hungry: %b since %d\n", this.hungry, this.startedChopstickAttempt - currentTime);
//        System.out.format("Last ate: %d    startedEating: %d\n", this.timeLastAte - currentTime, startedEating - currentTime);
//        System.out.format("Table state, Left: %b  Right: %b\n", this.hasLeftChopstick, this.hasRightChopstick);
    }

    private void waitABit() {
        synchronized (this) {
            hasLeftChopstick = false;
            hasRightChopstick = false;
        }
        try {
            System.err.println("Sleeping for a bit");
            Thread.sleep(Math.round(Math.random() * starvationTime / 40.0));
            startedChopstickAttempt = System.currentTimeMillis();
        } catch (InterruptedException e) { }
    }

    private synchronized boolean shouldWaitABit(long currentTime) {
        return this.hungry && !this.isEating() && startedChopstickAttempt + starvationTime / 40 < currentTime;
    }

    private synchronized boolean shouldStopThinking(long currentTime) {
        return !this.hungry && (starvationTime / 4 + startedThinking < currentTime || Math.random() > 0.99);
    }

    private synchronized boolean shouldStopEating(long currentTime) {
        return isEating() && (starvationTime / 40 + startedEating < currentTime || Math.random() > 0.9);
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
        if (isLeft) {
            this.hasLeftChopstick = false;
        } else {
            this.hasRightChopstick = false;
        }
    }

    public synchronized void nowHungry(long currentTime) {
        this.hungry = true;
        this.startedChopstickAttempt = currentTime;
        System.out.println("Done thinking, now hungry");
    }

    public synchronized void nowThinking(long currentTime) {
        this.hasLeftChopstick = false;
        this.hasRightChopstick = false;
        this.hungry = false;
        this.startedThinking = currentTime;
        System.out.println("Finished eating, thinking now.");

    }

    public boolean isHungry() {
        return hungry;
    }


}
