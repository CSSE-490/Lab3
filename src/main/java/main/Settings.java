package main;

public final class Settings {
    public static int numberPhilosopher;
    public static long starvationTime;
    public static int whoAmI;
    public static boolean manualMode;


    public static int mod(int number, int mod) {
        int toReturn = number % mod;
        if(toReturn < 0)
            toReturn += mod;

        return toReturn;
    }
}
