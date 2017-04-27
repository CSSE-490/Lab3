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

    private ZooKeeper keeper;

    public void init(String connectionString) throws IOException, KeeperException, InterruptedException {
        keeper = new ZooKeeper(connectionString, 800000, this);

        Thread.sleep(1000);

        // left ChopStick
        int leftNode = mod(whoAmI - 1, numberPhilosopher);
        leftChopStick = String.format("/Chop%d%d", leftNode, whoAmI);
        if (keeper.exists(leftChopStick, null) == null) {
            System.out.println("Creating " + leftChopStick);
            keeper.create(leftChopStick, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            keeper.setData(leftChopStick, new byte[0], -1);
        }

        // Right ChopStick
        rightChopStick = String.format("/Chop%d%d", whoAmI, mod(whoAmI + 1, numberPhilosopher));
        if (keeper.exists(rightChopStick, null) == null) {
            System.out.println("Creating " + rightChopStick);
            keeper.create(rightChopStick, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            keeper.setData(rightChopStick, new byte[0], -1);
        }

        // Cup (Table)
        tableCup = "/Table";
        if (keeper.exists(tableCup, null) == null) {
            System.out.println("Creating " + tableCup);
            keeper.create(tableCup, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            keeper.setData(tableCup, new byte[0], -1);
        }

        // Control (Table)
        tableControl = "/Control";
        if (keeper.exists(tableControl, this) == null) {
            System.out.println("Creating " + tableControl);
            keeper.create(tableControl, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            keeper.setData(tableControl, new byte[0], -1);
        }

        // Left Game
        leftGame = String.format("/Game%d%d", mod(whoAmI - 1, numberPhilosopher), whoAmI);
        if (keeper.exists(leftGame, null) == null) {
            System.out.println("Creating " + leftGame);
            keeper.create(leftGame, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } else {
            keeper.setData(leftGame, new byte[0], -1);
        }

        // Right Game
        rightGame = String.format("/Game%d%d", whoAmI, mod(whoAmI + 1, numberPhilosopher));
        if (keeper.exists(rightGame, null) == null) {
            System.out.println("Creating " + rightGame);
            keeper.create(rightGame, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }else {
            keeper.setData(rightGame, new byte[0], -1);
        }
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
                        Philosopher.INSTANCE.startLogicLoop();
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
        if(requestResource(leftChopStick)) {
            Philosopher.INSTANCE.takeChopstick(true);
        }
    }

    public void requestRightChopStick() {
        if(requestResource(rightChopStick)) {
            Philosopher.INSTANCE.takeChopstick(false);
        }
    }

    public void requestCup() {
        if(requestResource(tableCup)) {
            Philosopher.INSTANCE.takeCup();
        }
    }

    private boolean requestResource(String channel) {
        try {
            Stat stat = keeper.exists(channel, false);

            byte[] data = keeper.getData(channel, false, stat);

            if (data.length == 0) {
                try {
                    keeper.setData(channel, Integer.toString(whoAmI).getBytes(), stat.getVersion());
                    return true;
                } catch (KeeperException.BadVersionException e) {
                }
            }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("Missing node " + channel + "! Exiting.");
            e.printStackTrace();
            System.exit(1);
        }
        return false;
    }

    private void clearNode(String channel) {
     //   System.out.println("Attempting to clear node of: " + channel);
        try {
            Stat stat = keeper.exists(channel, false);

            try {
                keeper.setData(channel, new byte[0], stat.getVersion());
//                System.out.println("Cleared value for: " +channel);
            } catch (KeeperException.BadVersionException e) { }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("Missing node " + channel + "! Exiting.");
            e.printStackTrace();
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


    public boolean requestPlayOnLeft() {
        return requestPlaying(leftGame, rightGame);
    }

    public boolean requestPlayOnRight() {
        return requestPlaying(rightGame, leftGame);
    }

    private boolean requestPlaying(String connection, String otherConnection) {
        try {
            Stat stat = keeper.exists(connection, false);

            byte[] data = keeper.getData(connection, false, stat);

            if(data.length == 0) {
                keeper.setData(connection, Integer.toString(whoAmI).getBytes(), stat.getVersion());
                return false;
            }

            String message = new String(data);

            if(message.equals(Integer.toString(whoAmI)))
                return false;

            // System.err.println(message);
            if(message.equals(Integer.toString(mod(whoAmI + 1, numberPhilosopher))) ||
                    message.equals(Integer.toString(mod(whoAmI - 1, numberPhilosopher)))) {
                keeper.setData(connection, "playing".getBytes(), stat.getVersion());
                stopPlaying(otherConnection);
                return true;
            } else if(message.equals("playing")) {
                return true;
            } else if (message.equals("done")) {
                keeper.setData(connection, new byte[0], -1);
                Philosopher.INSTANCE.stopPlaying();
                return false;
            }

            System.err.format("Invalid Message in Game ZNode (%s): %s%n", connection,message);
            System.exit(1);
        } catch (KeeperException | InterruptedException e) {
            System.err.println("Missing node " + connection + "! Exiting.");
            e.printStackTrace();
            System.exit(1);
        }
        return false;
    }


    public void stopPlayingOnLeft() {
        stopPlaying(leftGame);
    }

    public void stopPlayingOnRight() {
        stopPlaying(rightGame);
    }

    private void stopPlaying(String channel) {
       // System.err.println("Stop playing on: " + channel);
        try {
            Stat stat = keeper.exists(channel, false);
            try {
                keeper.setData(channel, "done".getBytes(), stat.getVersion());
            } catch (KeeperException.BadVersionException e) { }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("Missing node " + channel + "! Exiting.");
            e.printStackTrace();
            System.exit(1);
        }
    }
}