/**
 * Created by CJ on 3/24/2017.
 */
public enum Message {

    REQUEST_CHOPSTICK ("I am requesting the chopstick"),
    YES_CHOPSTICK("Yes, here is the chopstick"),
    NO_CHOPSTICK("No, you may not have the chopstick"),
    YOU_ARE_MY_LEFT ("You are my left node"),
    YOU_ARE_MY_RIGHT ("You are my right node"),
    WAKE_UP("Yo, wake up bruh");

    private final String message;

    Message(String message) {
        this.message = message;
    }

    String getMessage() {return message;}
}
