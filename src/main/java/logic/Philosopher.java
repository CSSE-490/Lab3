package logic;


import main.Settings;
import network.zookeeper.Handler;

import java.util.Random;

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
        while (true) {
            long currentTime = System.currentTimeMillis();

            debugPrint(currentTime);

            if(isPassedOut) {
                if(Math.random() < 0.0001) {
                    isPassedOut = false;

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
            else if(!hasCup) {
                Handler.I.requestCup();
            }else if(hasCup) {
                System.out.println("GULP");
            }
        }
    }

    private void notThirsty() {
        this.thirsty = false;
        if(hasCup) Handler.I.clearCup();
        this.hasCup = false;
        System.out.println("No Longer Thirsty Drinking");
    }

    private void passOut(long currentTime) {
        synchronized (this) {
            if(hasCup) Handler.I.clearCup();
            if(hasLeftChopstick) Handler.I.clearLeftChopStick();
            if(hasRightChopstick) Handler.I.clearRightChopStick();

            hasLeftChopstick = false;
            hasRightChopstick = false;
            hasCup = false;
            isPassedOut = true;
            passedOutAt = currentTime;
            thirsty = false;
        }
        System.err.println("ZZZZZZ ZZZZZZ ZZZZZZ ZZZZZZ ZZZZZZ ZZZZZZ ZZZZZZ ZZZZZZ");
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
        }

        // If hungry but not eating, try to take chopsticks
        if (!isEating() && this.hungry) {
//            System.out.println("Requesting Chopsticks");
            if (!hasLeftChopstick) Handler.I.requestLeftChopStick();
            if (!hasRightChopstick) Handler.I.requestRightChopStick();
        }
    }

    private void debugPrint(long currentTime) {
//        System.out.format("Current Time: %d\n", currentTime);
//        System.out.format("Is hungry: %b since %d\n", this.hungry, this.startedChopstickAttempt - currentTime);
//        System.out.format("Last ate: %d    startedEating: %d\n", this.timeLastAte - currentTime, startedEating - currentTime);
//        System.out.format("Table state, Left: %b  Right: %b\n", this.hasLeftChopstick, this.hasRightChopstick);
    }

    private synchronized boolean shouldStopThinking(long currentTime) {
        return !this.hungry && (Settings.starvationTime / 4 + startedThinking < currentTime || Math.random() > 0.99);
    }

    private synchronized boolean shouldStopEating(long currentTime) {
        return isEating() && (Settings.starvationTime / 40 + startedEating < currentTime || Math.random() > 0.9);
    }

    public synchronized void nowHungry(long currentTime) {
        this.hungry = true;
        this.startedChopstickAttempt = currentTime;
        System.out.println("Done thinking, now hungry");
    }

    public synchronized void nowThinking(long currentTime) {
        if(hasLeftChopstick)
            Handler.I.clearLeftChopStick();
        if(hasRightChopstick)
            Handler.I.clearRightChopStick();

        this.hasLeftChopstick = false;
        this.hasRightChopstick = false;
        this.hungry = false;
        this.startedThinking = currentTime;
        System.out.println("Finished eating, thinking now.");
    }

    public boolean isHungry() {
        return hungry;
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

    public void takeCup() {
        this.hasCup = true;
    }

    public synchronized void takeChopstick(boolean isLeft) {
        if(isLeft)
            hasLeftChopstick = true;
        else {
            hasRightChopstick = true;
        }
    }
}
