package network;

import logic.Message;
import logic.Philosopher;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by CJ on 3/24/2017.
 */
public class ClientResponder implements Runnable {

    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final Socket socket;
    private boolean isLeft;

    private boolean onGoingRequest;
    private boolean onRequestCooldown;

    public ClientResponder(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            while (true) {
                String messageRaw;
                try {
                    messageRaw = reader.readLine();
                } catch (SocketException e) {
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                if(messageRaw == null) {
                    return;
                }

                Message message;
                try {
                    message = Message.valueOf(messageRaw);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(messageRaw);
                    return;
                }


                switch (message) {
                    case REQUEST_CHOPSTICK:
                        if(onGoingRequest) {
                            sendMessage(Message.NO_CHOPSTICK);
                            onRequestCooldown = true;
                            new Thread(() -> {
                                try {
                                    Thread.sleep(Math.round(Math.random() * 100));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                onGoingRequest = true;
                                onRequestCooldown = false;
                                sendMessage(Message.REQUEST_CHOPSTICK);
                            }).start();
                            onGoingRequest = false;
                            break;
                        }

                        boolean available = Philosopher.INSTANCE.requestChopstick(isLeft);

                        if (available) {
                            sendMessage(Message.YES_CHOPSTICK);
                        } else {
                            sendMessage(Message.NO_CHOPSTICK);
                        }

                        break;

                    case YOU_ARE_MY_LEFT:
                        Communicator.INSTANCE.rightSocket = this;
                        this.isLeft = false;
                        System.out.println("A client has identified as my right");
                        break;

                    case YOU_ARE_MY_RIGHT:
                        Communicator.INSTANCE.leftSocket = this;
                        this.isLeft = true;
                        System.out.println("A client has identified as my left");
                        break;

                    case YES_CHOPSTICK:
                        Philosopher.INSTANCE.takeChopstick(isLeft);
                        onGoingRequest = false;
                        break;
                    case NO_CHOPSTICK:
                        Philosopher.INSTANCE.dropChopstick(isLeft);
                        onGoingRequest = false;
                        break;
                    case WAKE_UP:
                        if (!Philosopher.INSTANCE.isRunning()) {
                            if (isLeft) Communicator.INSTANCE.rightSocket.sendMessage(Message.WAKE_UP);
                            else  Communicator.INSTANCE.leftSocket.sendMessage(Message.WAKE_UP);
                            Philosopher.INSTANCE.wakeUp();
                        }
                        break;
                    case REQUEST_CUP:
                        boolean absorbed = Philosopher.INSTANCE.willAbsorbRequest();

                        if(!absorbed) {
                            Communicator.INSTANCE.leftSocket.sendMessage(Message.REQUEST_CUP);
                            Communicator.INSTANCE.leftSocket.sendMessage(Message.FORWARD_REQUEST_CUP);
                        }
                        break;
                    case FORWARD_REQUEST_CUP:
                        absorbed = Philosopher.INSTANCE.willAbsorbRequest();

                        if(!absorbed) {
                            Communicator.INSTANCE.leftSocket.sendMessage(Message.FORWARD_REQUEST_CUP);
                        }
                        break;
                    default:
                        System.err.println("Received invalid message from a client, logic.Message: " + messageRaw);
                        break;
                }
            }
        } finally {
            System.out.format("%s Client has disconnected\n", isLeft ? "Left" : "Right");
            try {
                socket.close();
            } catch (IOException e) { }
        }
    }

    public void registerAsLeft() {
        sendMessage(Message.YOU_ARE_MY_LEFT);
        Communicator.INSTANCE.leftSocket = this;
        this.isLeft = true;
    }

    public void registerAsRight() {
        sendMessage(Message.YOU_ARE_MY_RIGHT);
        Communicator.INSTANCE.rightSocket = this;
        this.isLeft = false;
    }

    private void sendMessage(Message s) {
        if(socket.isClosed())
            return;

        try {
            writer.write(s + "\n");
            writer.flush();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.err.println("Unable to communicate with via " + this.toString());
        }
    }

   @Override
    public String toString() {
        return String.format("%s:%s:%s", socket.getInetAddress(), socket.getLocalPort(), socket.getPort());
    }

    public void requestChopstick() {
        if(socket.isClosed()) {
            Philosopher.INSTANCE.takeChopstick(isLeft);
            return;
        }

        if(!(onGoingRequest || onRequestCooldown)) {
            onGoingRequest = true;
            this.sendMessage(Message.REQUEST_CHOPSTICK);
        }
    }

    public void sendWakeup() {
        this.sendMessage(Message.WAKE_UP);
    }

    public void close() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(isLeft) {

        }
    }

    public void requestCup() {
        this.sendMessage(Message.REQUEST_CUP);
    }
}
