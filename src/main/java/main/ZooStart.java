package main;

import logic.Philosopher;
import network.zookeeper.Handler;
import org.apache.zookeeper.KeeperException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ZooStart {

    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        int starvationTime = Integer.parseInt(args[0]);
        int numberOfPhilosophers = Integer.parseInt(args[1]);
        int whoAmI = Integer.parseInt(args[2]);

        Settings.numberPhilosopher = numberOfPhilosophers;
        Settings.starvationTime = starvationTime;
        Settings.whoAmI = whoAmI;

        Handler.I.init(args[3]);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        repl(reader);
    }

    public static void repl(BufferedReader reader) throws IOException, KeeperException, InterruptedException {
        System.out.println("Ready for user input");
        while(true) {
            String input = reader.readLine();
            switch (input) {
                case "start":
                    Handler.I.wakeUp();
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
                    JButton hungryButton = new JButton();
                    hungryButton.addActionListener((ae) -> {
                        if(Philosopher.INSTANCE.isHungry()) {
                            Philosopher.INSTANCE.nowThinking(System.currentTimeMillis());
                        } else {
                            Philosopher.INSTANCE.nowHungry(System.currentTimeMillis());
                        }
                    });
                    JButton thirstyButton = new JButton();
                    thirstyButton.addActionListener(ae -> {
                        if(Philosopher.INSTANCE.isThirsty()) {
                            Philosopher.INSTANCE.setThirsty(System.currentTimeMillis(), false);
                        } else {
                            Philosopher.INSTANCE.setThirsty(System.currentTimeMillis(), true);
                        }
                    });

                    JButton playButton = new JButton();
                    playButton.addActionListener(ae -> {
                        if(Philosopher.INSTANCE.wantsToPlay()){
                            Philosopher.INSTANCE.setWantsToPlay(false);
                        } else {
                            Philosopher.INSTANCE.setWantsToPlay(true);
                        }
                    });

                    JButton manualMode = new JButton("Manual: " + Settings.manualMode);
                    manualMode.addActionListener(ae -> {
                        Settings.manualMode = !Settings.manualMode;
                        manualMode.setText("Manual: " + Settings.manualMode);
                    });

                    JButton knockOut = new JButton();
                    knockOut.addActionListener(ae -> {
                        if(Philosopher.INSTANCE.isPassedOut()) {
                            Philosopher.INSTANCE.wakeUp(System.currentTimeMillis());
                        } else {
                            Philosopher.INSTANCE.passOut(System.currentTimeMillis());
                        }
                    });

                    panel.add(manualMode);
                    panel.add(knockOut);
                    panel.add(playButton);
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

                                    EventQueue.invokeLater(() -> knockOut.setText("Is Awake: " + !Philosopher.INSTANCE.isPassedOut()));
                                    EventQueue.invokeLater(() -> hungryButton.setText("Is hungry: " + Philosopher.INSTANCE.isHungry()));
                                    EventQueue.invokeLater(() -> thirstyButton.setText("Is thirsty: " + Philosopher.INSTANCE.isThirsty()));
                                    EventQueue.invokeLater(() -> playButton.setText("Wants To Play: " + Philosopher.INSTANCE.wantsToPlay()));
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
