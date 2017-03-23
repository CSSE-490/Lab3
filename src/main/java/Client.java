import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by CJ on 3/23/2017.
 */
class Client implements Runnable{

    private final Node rightNode;
    private final Node leftNode;

    public Client(Node leftNode, Node rightNode) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }


	@Override
	public void run() {
		//all client code here
		//you should have a "left" client connection
		//and a "right" client connection

        Socket leftSocket = connect(leftNode);
        Socket rightSocket = connect(rightNode);

        while(true) {

        }
	}

	private Socket connect(Node node) {
        try {
            Socket socket = new Socket(node.host, node.port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
