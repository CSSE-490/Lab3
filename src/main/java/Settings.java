/**
 * Created by CJ on 4/3/2017.
 */
public class Settings {
    public static Settings INSTANCE;

    static {
        INSTANCE = new Settings();
    }

    public int serverPort;
    public Node leftNode;
    public Node rightNode;
    public int numberPhilosopher;
}
