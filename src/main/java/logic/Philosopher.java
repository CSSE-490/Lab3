package logic;


import main.Settings;
import network.zookeeper.Handler;

import java.util.Random;

import static main.Settings.manualMode;

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
    private long passedOutAt;
    private boolean running;
    private boolean isPassedOut;
    private long startedDrinking = 0L;

    private boolean iWantToPlay;
    private boolean playingOnLeft;
    private boolean playing;
    private long startedChopstickAttempt = 0L;

    private Philosopher() {
        this.hasLeftChopstick = false;
        this.hasRightChopstick = false;
        this.hungry = new Random().nextBoolean();
        this.running = false;
    }

    public synchronized boolean isEating() {
        return hasLeftChopstick && hasRightChopstick && hungry;
    }

    public synchronized void startLogicLoop() {
        if (this.running)
            return;

        this.timeLastAte = System.currentTimeMillis();
        if (this.hungry) {
            this.startedChopstickAttempt = System.currentTimeMillis();
        }
        else {
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

            if (isPassedOut) {
                if (!manualMode && Math.random() < 0.0001) {
                    wakeUp(currentTime);
                }
            } else if (playingPhilosopher(currentTime)) {
                System.out.println("Playing");
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

    public void wakeUp(long currentTime) {
        isPassedOut = false;

        long delta = currentTime - passedOutAt;

        timeLastAte += delta;
        startedEating += delta;
        startedThinking += delta;

        System.out.println("Woke up");
    }

    private boolean playingPhilosopher(long currentTime) {
        if (iWantToPlay) {
            if(playing){
                this.timeLastAte = currentTime;
                if(playingOnLeft) {
                    Handler.I.stopPlayingOnRight();
                    if(!Handler.I.requestPlayOnLeft()) {
                        playing = false;
                        return false;
                    }
                } else {
                    Handler.I.stopPlayingOnLeft();
                    if(!Handler.I.requestPlayOnRight()) {
                        playing = false;
                        return false;
                    }
                }

                if (!manualMode && Math.random() > 0.999) {
                    stopPlayingAndNotify();
                    return false;
                }
                return true;
            } else {
                if (Handler.I.requestPlayOnLeft()) {
                    playingOnLeft = true;
                } else if (Handler.I.requestPlayOnRight()) {
                    playingOnLeft = false;
                } else {
                    return false;
                }
                startPlaying(currentTime);
                return true;
            }
        } else {
            if(!manualMode )
                iWantToPlay = Math.random() > .999;
        }
        return false;
    }

    private void stopPlayingAndNotify() {
        if (playingOnLeft) Handler.I.stopPlayingOnLeft();
        else Handler.I.stopPlayingOnRight();
    }

    private void drinkingPhilosopher(long currentTime) {
        if (hasCup && startedDrinking + Settings.starvationTime < currentTime) {
            passOut(currentTime);
        }

        if (!manualMode && !thirsty && Math.random() < 0.001) {
            thirsty = true;
            System.out.println("Now thirsty.");
        } else if (thirsty) {
            //random chance to put down cup
            if (!manualMode && thirsty && hasCup && Math.random() < 0.0001) {
                notThirsty();
            }
            //start drinking
            else if (!hasCup) {
                Handler.I.requestCup();
            } else if (hasCup) {
                System.out.println("GULP");
            }
        }
    }

    private void notThirsty() {
        this.thirsty = false;
        if (hasCup) Handler.I.clearCup();
        this.hasCup = false;
        System.out.println("No Longer Thirsty");
    }

    public void passOut(long currentTime) {
        synchronized (this) {
            if (hasCup) Handler.I.clearCup();
            if (hasLeftChopstick) Handler.I.clearLeftChopStick();
            if (hasRightChopstick) Handler.I.clearRightChopStick();

            hasLeftChopstick = false;
            hasRightChopstick = false;
            hasCup = false;
            isPassedOut = true;
            passedOutAt = currentTime;
            thirsty = false;
        }
        System.err.println("ZZZZZZ ZZZZZZ ZZZZZZ ZZZZZZ ZZZZZZ ZZZZZZ ZZZZZZ ZZZZZZ");
    }

    private synchronized void startPlaying(long currentTime) {
        if (hasCup) Handler.I.clearCup();
        if (hasLeftChopstick) Handler.I.clearLeftChopStick();
        if (hasRightChopstick) Handler.I.clearRightChopStick();

        hasLeftChopstick = false;
        hasRightChopstick = false;
        hasCup = false;
        thirsty = false;
        playing = true;
        System.out.println("And let the games begin.");
    }

    private void dinningPhilosopher(long currentTime) {
        // If eating, reset timestamp
        if (isEating()) {
            this.timeLastAte = currentTime;
            System.out.println("OM NOM NOM");
        } else if (!hungry) {
            System.out.println("Pondering");
        }

        if (this.timeLastAte + Settings.starvationTime < currentTime) {
            passOut(currentTime);
            System.err.println("I starved");
            System.exit(1);
        }

        if (!manualMode  && shouldStopEating(currentTime)) { // check how long we've been eating and stop if necessary
            nowThinking(currentTime);
        } else if (!manualMode  && shouldStopThinking(currentTime)) { // Don't think for more than 1 second
            this.nowHungry(currentTime);
        } else if (shouldWaitABit(currentTime)) {
            waitABit();
        }

        // If hungry but not eating, try to take chopsticks
        if (!isEating() && this.hungry) {
            System.out.println("I NEED THINGS");
            if (!hasLeftChopstick) Handler.I.requestLeftChopStick();
            if (!hasRightChopstick) Handler.I.requestRightChopStick();
        }
    }

    private void waitABit() {
        synchronized (this) {
            if(hasLeftChopstick) Handler.I.clearLeftChopStick();
            if(hasRightChopstick) Handler.I.clearRightChopStick();
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

    private void debugPrint(long currentTime) {
//        System.out.println("Tick");
//        System.out.format("Current Time: %d\n", currentTime);
//        System.out.format("Is hungry: %b since %d\n", this.hungry, this.startedChopstickAttempt - currentTime);
//        System.out.format("Last ate: %d    startedEating: %d\n", this.timeLastAte - currentTime, startedEating - currentTime);
//        System.out.format("Table state, Left: %b  Right: %b\n", this.hasLeftChopstick, this.hasRightChopstick);
    }

    private synchronized boolean shouldStopThinking(long currentTime) {
        return !this.hungry && (Settings.starvationTime / 4 + startedThinking < currentTime || Math.random() > 0.9);
    }

    private synchronized boolean shouldStopEating(long currentTime) {
        return isEating() && (Settings.starvationTime / 40 + startedEating < currentTime || Math.random() > 0.9);
    }

    public synchronized void nowHungry(long currentTime) {
        this.hungry = true;
        System.out.println("Done thinking, now hungry");
    }

    public synchronized void nowThinking(long currentTime) {
        if (hasLeftChopstick)
            Handler.I.clearLeftChopStick();
        if (hasRightChopstick)
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
        if (thirsty) {
            this.thirsty = true;
        } else {
            notThirsty();
        }
    }

    public boolean isThirsty() {
        return thirsty;
    }

    public synchronized void takeCup() {
        this.hasCup = true;
        startedDrinking = System.currentTimeMillis();
    }

    public synchronized void takeChopstick(boolean isLeft) {
        if (isLeft)
            hasLeftChopstick = true;
        else {
            hasRightChopstick = true;
        }

        if (hasLeftChopstick && hasRightChopstick)
            startedEating = System.currentTimeMillis();
    }

    public synchronized void stopPlaying() {
        this.iWantToPlay = false;
    }

    public boolean wantsToPlay() {
        return iWantToPlay;
    }

    public void setWantsToPlay(boolean b) {
        if(playing && !b && iWantToPlay)
            stopPlayingAndNotify();

        iWantToPlay = b;
    }

    public boolean isPassedOut() {
        return isPassedOut;
    }
}
