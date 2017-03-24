/**
 * Created by CJ on 3/24/2017.
 */
public class Table {

    public static Table INSTANCE;

    static {
        INSTANCE = new Table();
    }

    private boolean hasLeftChopstick;
    private boolean hasRightChopstick;

    private Table() {
        this.hasLeftChopstick = true;
        this.hasRightChopstick = true;
    }

    public synchronized boolean requestLeftChopstick() {
        if(hasLeftChopstick) {
            hasLeftChopstick = false;
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean requestRightChopstick() {
        if(hasRightChopstick) {
            hasRightChopstick = false;
            return true;
        } else {
            return false;
        }
    }

    public synchronized void returnLeftChopstick() {
        hasLeftChopstick = true;
    }

    public synchronized void returnRightChopstick() {
        hasRightChopstick = true;
    }
}
