package main;

import javafx.scene.control.PasswordField;
import logic.*;
import network.ClientResponder;
import network.Communicator;
import network.Node;
import network.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by CJ on 3/23/2017.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        try {
            Settings.starvationTime = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid tick rate provided");
            System.exit(1);
        }

        System.out.println("Local network.Server Port");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String port = reader.readLine();

        int serverPort = Integer.parseInt(port);
        Server.createServer(serverPort);

        System.out.println("How many Philosophers are there?");
        Settings.numberPhilosopher = Integer.parseInt(reader.readLine());

        System.out.println("Press enter to proceed to connection input.");
        reader.readLine();

        try {
            if(Communicator.INSTANCE.leftSocket == null) {
                System.out.println("IP:Port of left Connection");
                String input = reader.readLine();
                Node left = getNode(input);


                Socket socket = new Socket(left.host, left.port);

                ClientResponder leftClient = new ClientResponder(socket);
                leftClient.registerAsLeft();
                new Thread(leftClient).start();
            }
            if(Communicator.INSTANCE.rightSocket == null) {
                System.out.println("IP:Port of the right Connection");
                String input = reader.readLine();
                Node right = getNode(input);

                Socket socket = new Socket(right.host, right.port);

                ClientResponder rightClient = new ClientResponder(socket);
                rightClient.registerAsRight();
                new Thread(rightClient).start();
            }
        } catch (UnknownHostException e) {
            System.err.println("Invalid Arguments");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start logic.Philosopher Code
        //logic.Philosopher.INSTANCE.wakeUp();

        repl(reader);

    }

    private static Node getNode(String s) {
        String[] array = s.split(":");
        if(array.length != 2) {
            throw new RuntimeException("Invalid Arguments");
        }

        return new Node(array[0], Integer.parseInt(array[1]));
    }

    public static void repl(BufferedReader reader) throws IOException {
        System.out.println("Ready for user input");
        while(true) {
            String input = reader.readLine();
            switch (input) {
                case "start":
                    Communicator.INSTANCE.leftSocket.sendWakeup();
                    Communicator.INSTANCE.rightSocket.sendWakeup();
                    Philosopher.INSTANCE.wakeUp();
                    break;

                case "hungry":
                    Philosopher.INSTANCE.nowHungry(System.currentTimeMillis());
                    break;

                case "thinking":
                    Philosopher.INSTANCE.nowThinking(System.currentTimeMillis());
                    break;
                case "thirsty":
                    Philosopher.INSTANCE.setThirsty(System.currentTimeMillis(), true);
                    break;
                case "gui":
                    JFrame frame = new JFrame("Philosopher");
                    JPanel panel = new JPanel();
                    JButton hungryButton = new JButton("Philosopher is hungry: " + Philosopher.INSTANCE.isHungry());
                    hungryButton.addActionListener((ae) -> {
                        if(Philosopher.INSTANCE.isHungry()) {
                            Philosopher.INSTANCE.nowThinking(System.currentTimeMillis());
                        } else {
                            Philosopher.INSTANCE.nowHungry(System.currentTimeMillis());
                        }
                    });
                    JButton thirstyButton = new JButton(("Philosopher is thirsty: "));
                    thirstyButton.addActionListener(ae -> {
                        if(Philosopher.INSTANCE.isThirsty()) {
                            Philosopher.INSTANCE.setThirsty(System.currentTimeMillis(), false);
                        } else {
                            Philosopher.INSTANCE.setThirsty(System.currentTimeMillis(), true);
                        }
                    });

                    panel.add(hungryButton);
                    panel.add(thirstyButton);
                    frame.add(panel);
                    frame.pack();
                    frame.addWindowListener(new WindowListener() {
                        private Thread thread;
                        private boolean shouldStop;
                        @Override
                        public void windowOpened(WindowEvent we) {
                            thread = new Thread(() -> {
                                while(true) {
                                    if(shouldStop)
                                        return;

                                    EventQueue.invokeLater(() -> hungryButton.setText("Philosopher is hungry: " + Philosopher.INSTANCE.isHungry()));
                                    EventQueue.invokeLater(() -> thirstyButton.setText("Philosopher is thirsty: " + Philosopher.INSTANCE.isThirsty()));
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            thread.start();
                        }

                        @Override
                        public void windowClosing(WindowEvent e) { }

                        @Override
                        public void windowClosed(WindowEvent e) {
                            shouldStop = true;
                        }

                        @Override
                        public void windowIconified(WindowEvent e) { }

                        @Override
                        public void windowDeiconified(WindowEvent e) { }

                        @Override
                        public void windowActivated(WindowEvent e) { }

                        @Override
                        public void windowDeactivated(WindowEvent e) { }
                    });
                    frame.setVisible(true);
            }
        }
    }
}
