package logic;


import main.Settings;
import network.Communicator;
import network.ConnectionAttempter;
import network.Server;

import java.util.Random;

/**
 * Created by CJ on 3/24/2017.
 */
public class Philosopher {

    public static Philosopher INSTANCE;

    static {
        INSTANCE = new Philosopher();
    }

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
    private boolean running;
    private boolean isPassedOut;

    private int drinkingRequests;
    private long sentCupRequest;
    private boolean requestingCup;

    private Philosopher() {
        this.hasLeftChopstick = false;
        this.hasRightChopstick = false;
        this.hungry = new Random().nextBoolean();
        this.running = false;
    }

    public synchronized boolean isRunning() {
        return this.running;
    }

    public synchronized boolean isEating() {
        return hasLeftChopstick && hasRightChopstick && hungry;
    }

    public synchronized void wakeUp() {
        if(this.running)
            return;

        this.timeLastAte = System.currentTimeMillis();
        if (this.hungry) {
            this.startedChopstickAttempt = System.currentTimeMillis();
        } else {
            this.startedThinking = System.currentTimeMillis();
        }

        new Thread(this::run).start();
        this.running = true;
        System.out.println("Started philosopher");
    }


    private void run() {
        System.out.println("I know who is on my left and right..");
        System.out.println("On my left is " + Communicator.INSTANCE.leftSocket);
        System.out.println("On my right is " + Communicator.INSTANCE.rightSocket);

        while (true) {
            long currentTime = System.currentTimeMillis();

            debugPrint(currentTime);

            if(isPassedOut) {
                if(Math.random() < 0.0001) {
                    isPassedOut = false;

                    Server.createServer(Settings.serverPort);
                    ConnectionAttempter.attemptConnection(true,Settings.leftNode);
                    ConnectionAttempter.attemptConnection(false,Settings.rightNode);

                    long delta = currentTime - passedOutAt;

                    timeLastAte += delta;
                    startedEating += delta;
                    startedThinking += delta;

                    System.out.println("Woke up");
                }
            } else {
                dinningPhilosopher(currentTime);
                drinkingPhilosopher(currentTime);
            }

            try {
                Thread.sleep(Math.round(Math.ceil(Settings.starvationTime / 4000.0)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void drinkingPhilosopher(long currentTime) {
        if(hasCup && startedDrinking + Settings.starvationTime < currentTime) {
            passOut(currentTime);
        }

        if(!thirsty &&  Math.random() < 0.00001) {
            thirsty = true;
        } else if(thirsty) {
            //random chance to put down cup
            if (thirsty && hasCup && Math.random() < 0.0001) {
                notThirsty();
            }
            //start drinking
            else if(!hasCup && drinkingRequests == Settings.numberPhilosopher) {
                hasCup = true;
                startedDrinking = currentTime;
                requestingCup = false;
                System.out.println("Started Drinking");
            }
            // timeout for cup request
            else if(requestingCup && (sentCupRequest + 20 * Settings.numberPhilosopher) < currentTime) {
                requestingCup = false;
                drinkingRequests = 0;
            }
            // request cup if after request cooldown
            else if (!requestingCup && sentCupRequest + Settings.starvationTime/20 + 20 * Settings.numberPhilosopher < currentTime && Math.random() < 0.0001) {
                requestingCup = true;
                sentCupRequest = currentTime;
                Communicator.INSTANCE.leftSocket.requestCup();
            }

            if(hasCup) {
                System.out.println("GULP");
            }
        }
    }

    private void notThirsty() {
        this.thirsty = false;
        this.hasCup = false;
        System.out.println("No Longer Thirsty Drinking");
    }

    private void passOut(long currentTime) {
        synchronized (this) {
            hasLeftChopstick = false;
            hasRightChopstick = false;
            hasCup = false;
            isPassedOut = true;
            passedOutAt = currentTime;
            thirsty = false;
            Communicator.close();
            Server.close();
        }
        System.err.println("ZZZZZZ");
    }

    private void dinningPhilosopher(long currentTime) {
        // If eating, reset timestamp
        if (isEating()) {
            this.timeLastAte = currentTime;
            System.out.println("OM NOM NOM");
        }

        if (this.timeLastAte + Settings.starvationTime < currentTime) {
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
            Thread.sleep(Math.round(Math.random() * Settings.starvationTime / 40.0));
            startedChopstickAttempt = System.currentTimeMillis();
        } catch (InterruptedException e) { }
    }

    private synchronized boolean shouldWaitABit(long currentTime) {
        return this.hungry && !this.isEating() && startedChopstickAttempt + Settings.starvationTime / 40 < currentTime;
    }

    private synchronized boolean shouldStopThinking(long currentTime) {
        return !this.hungry && (Settings.starvationTime / 4 + startedThinking < currentTime || Math.random() > 0.99);
    }

    private synchronized boolean shouldStopEating(long currentTime) {
        return isEating() && (Settings.starvationTime / 40 + startedEating < currentTime || Math.random() > 0.9);
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


    public boolean willAbsorbRequest() {
        if(hasCup) {
            return  true;
        }

        if(thirsty && requestingCup) {
            drinkingRequests++;
            return true;
        }

        return false;
    }

    public synchronized void setThirsty(long currentTime, boolean thirsty) {
        if(thirsty) {
            this.thirsty = true;
        } else {
            notThirsty();
        }
    }

    public boolean isThirsty() {
        return thirsty;
    }
}
