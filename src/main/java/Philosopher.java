/**
 * Created by CJ on 3/24/2017.
 */
public class Philosopher {
    public static Philosopher INSTANCE;

    static {
        INSTANCE = new Philosopher();
    }

    public volatile boolean leftChopstick;
    public volatile boolean rightChopstick;
    public volatile boolean hungry;

    private Philosopher() {
        this.leftChopstick = false;
        this.rightChopstick = false;
    }

    public synchronized boolean isThinking() {
        return !(leftChopstick || rightChopstick) && !hungry;
    }

    public synchronized boolean isEating() {
        return leftChopstick && rightChopstick && hungry;
    }
}
