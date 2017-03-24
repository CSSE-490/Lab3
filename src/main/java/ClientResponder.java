import java.io.*;
import java.net.Socket;

/**
 * Created by CJ on 3/24/2017.
 */
public class ClientResponder implements Runnable {

    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final Socket socket;

    public ClientResponder(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        while (true) {
            try {
                String messageRaw = reader.readLine();

                Message message = Message.valueOf(messageRaw);

                switch (message) {
                    case REQUEST_LEFT_CHOPSTICK:
                        boolean request = Table.INSTANCE.requestLeftChopstick();

                        if (request) {
                            sendMessage(Message.YES);
                        } else {
                            sendMessage(Message.NO);
                        }

                        break;
                    case REQUEST_RIGHT_CHOPSTICK:
                        request = Table.INSTANCE.requestRightChopstick();

                        if (request) {
                            sendMessage(Message.YES);
                        } else {
                            sendMessage(Message.NO);
                        }

                        break;
                    case YOU_ARE_MY_LEFT:
                        Communicator.INSTANCE.rightSocket = this;
                        System.out.println("A client has identified as my right");
                        break;
                    case YOU_ARE_MY_RIGHT:
                        Communicator.INSTANCE.leftSocket = this;
                        System.out.println("A client has identified as my left");
                        break;
                    default:
                        System.err.println("Received invalid message from a client, Message: " + messageRaw);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void registerAsLeft() throws IOException {
        sendMessage(Message.YOU_ARE_MY_LEFT);
        Communicator.INSTANCE.leftSocket = this;
    }

    public void registerAsRight() throws IOException {
        sendMessage(Message.YOU_ARE_MY_RIGHT);
        Communicator.INSTANCE.rightSocket = this;
    }

    private void sendMessage(Message s) throws IOException {
        writer.write(s + "\n");
        writer.flush();
    }

    @Override
    public String toString() {
        return String.format("%s:%s:%s",socket.getInetAddress(), socket.getLocalPort(), socket.getPort());
    }
}
