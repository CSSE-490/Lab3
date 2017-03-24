/**
 * Created by CJ on 3/24/2017.
 */
public enum Message {
    I_AM_LEFT ("I am Left"),
    I_AM_RIGHT ("I am Right"),
    DO_YOU_HAVE_LEFT_CHOPSTICK ("Do you have the left chopstick"),
    DO_YOU_HAVE_RIGHT_CHOPSTICK ("Do you have the right chopstick"),
    I_HAVE_LEFT_CHOPSTICK ("I have the left chopstick"),
    I_HAVE_RIGHT_CHOPSTICK ("I have the right chopstick");

    private final String message;

    Message(String message) {
        this.message = message;
    }

    String getMessage() {return message;}
}
