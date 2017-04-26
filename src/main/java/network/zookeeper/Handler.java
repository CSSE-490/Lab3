package network.zookeeper;


import logic.Philosopher;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

import static main.Settings.*;

public enum Handler implements Watcher {
    I;

    private String leftChopStick;
    private String rightChopStick;
    private String tableCup;
    private String tableControl;
    private String leftGame;
    private String rightGame;

    public static void main(String[] args) throws InterruptedException, IOException, KeeperException {
        I.init("localhost");
    }

    private ZooKeeper keeper;

    public void init(String connectionString) throws IOException, KeeperException, InterruptedException {
        keeper = new ZooKeeper(connectionString, 3000, this);


        // left ChopStick
        int leftNode = mod(whoAmI - 1, numberPhilosopher);
        leftChopStick = String.format("/Chop%d%d", leftNode, whoAmI);
        if (keeper.exists(leftChopStick, null) == null)
            keeper.create(leftChopStick, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // Right ChopStick
        rightChopStick = String.format("/Chop%d%d", whoAmI, mod(whoAmI + 1, numberPhilosopher));
        if (keeper.exists(rightChopStick, null) == null)
            keeper.create(rightChopStick, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // Cup (Table)
        tableCup = "/Table";
        if (keeper.exists(tableCup, null) == null)
            keeper.create(tableCup, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // Control (Table)
        tableControl = "/Control";
        if (keeper.exists(tableControl, this) == null)
            keeper.create(tableControl, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // Left Game
        leftGame = String.format("/Game%d%d", mod(whoAmI - 1, numberPhilosopher), whoAmI);
        if (keeper.exists(leftGame, null) == null)
            keeper.create(leftGame, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        // Right Game
        rightGame = String.format("/Game%d%d", whoAmI, mod(whoAmI + 1, numberPhilosopher));
        if (keeper.exists(rightGame, null) == null)
            keeper.create(rightGame, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Event.EventType.NodeDataChanged) {
            try {
                byte[] data = keeper.getData(tableControl, this, keeper.exists(tableControl, false));

                if (data == null) {
                    return;
                }
                String message = new String(data);

                switch (message) {
                    case "START":
                        Philosopher.INSTANCE.wakeUp();
                        break;
//                    case "TICK_RATE":
//                        break;
//                    case "":
                }
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void wakeUp() throws KeeperException, InterruptedException {
        keeper.setData(tableControl, "START".getBytes(), -1);
    }

    public void requestLeftChopStick() {
        requestResource(leftChopStick);
    }

    public void requestRightChopStick() {
        requestResource(rightChopStick);
    }

    public void requestCup() {
        requestResource(tableCup);
    }

    private void requestResource(String channel) {
        try {
            Stat stat = keeper.exists(channel, false);

            byte[] data = keeper.getData(channel, false, stat);

            if (data.length == 0) {
                try {
                    keeper.setData(channel, Integer.toString(whoAmI).getBytes(), stat.getVersion());
                    Philosopher.INSTANCE.takeCup();
                } catch (KeeperException.BadVersionException e) {
                }
            }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("Missing node " + channel + "! Exiting.");
            System.exit(1);
        }
    }

    private void clearNode(String channel) {
        try {
            Stat stat = keeper.exists(channel, false);

            byte[] data = keeper.getData(channel, false, stat);

            if (data.length == 0) {
                try {
                    keeper.setData(channel, new byte[0], stat.getVersion());
                    Philosopher.INSTANCE.takeCup();
                } catch (KeeperException.BadVersionException e) {
                }
            }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("Missing node " + channel + "! Exiting.");
            System.exit(1);
        }
    }

    public void clearRightChopStick() {
        clearNode(rightChopStick);
    }

    public void clearLeftChopStick() {
        clearNode(leftChopStick);
    }

    public void clearCup() {
        clearNode(tableCup);
    }

}