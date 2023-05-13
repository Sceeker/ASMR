import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import fr.emse.fayol.maqit.simulator.components.Orientation;
import fr.emse.fayol.maqit.simulator.robot.GridTurtlebot;

public class INS extends GridTurtlebot {
    private Restaurant restaurant;
    private int[] orderOnHold;
    private boolean follow;
    private GridPath curPath;
    private int pathStep;

    public INS(int id, String name, int field, int debug, int[] pos, int r, int c, Restaurant restaurant) {
        super(id, name, field, debug, pos, r, c);

        this.restaurant = restaurant;
        orderOnHold = null;
        follow = false;
        pathStep = 0;
    }

    private void manageOrder() {
        orderOnHold = null;
        follow = true;
    }

    private void registerOrder() {
        TimerTask task = new TimerTask() {
            public void run() {
                manageOrder();
            }
        };

        Timer timer = new Timer();
        
        timer.schedule(task, 1000);
    }

    private void cancelOrder() {
        if (orderOnHold != null)
            orderOnHold = null;
    }

    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            case 0:     // Une table veut commander
                if (! follow) {
                    orderOnHold = new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)};
                    registerOrder();

                    PathFinding solver = new PathFinding(restaurant);
                    
                    curPath = solver.findPath(getLocation(), orderOnHold);

                    ArrayList<Integer> trans = new ArrayList<Integer>();
                    trans.add(curPath.getDistance());
                    trans.add(orderOnHold[0]);
                    trans.add(orderOnHold[1]);
                    restaurant.getAir().radioTransmission(new RadioData(this, 1, trans));
                }
                break;

            case 1:     // Distance d'un autre INS
                if (orderOnHold[0] == dat.getCommandData().get(1) && orderOnHold[1] == dat.getCommandData().get(2) && curPath.getDistance() > dat.getCommandData().get(0)) {
                    cancelOrder();
                }
                break;

            default :
                break;
        }
    }

    @Override
    public void move(int arg0) {
        if (follow) {
            followPath();

            if (Arrays.equals(curPath.coordsArray()[curPath.coordsArray().length - 1], getLocation())) {
                follow = false;
                pathStep = 0;
                curPath = null;
                orderOnHold = null;
            }
        }
    }

    public void followPath() {
        int[] cur = curPath.coordsArray()[pathStep];
        pathStep++;
        int[] next = curPath.coordsArray()[pathStep];

        restaurant.getEnv().moveComponent(getLocation(), next, 6);

        restaurant.getEnv().addComponent(getLocation(), 0, restaurant.typeColor(0));

        if (next[1] - cur[1] > 0) {
            if (getCurrentOrientation() == Orientation.up) {
                moveRight();
            }
            if (getCurrentOrientation() == Orientation.down) {
                moveLeft();
            }
            if (getCurrentOrientation() == Orientation.left) {
                moveRight();
                moveRight();
            }
        }

        if (next[1] - cur[1] < 0) {
            if (getCurrentOrientation() == Orientation.up) {
                moveLeft();
            }
            if (getCurrentOrientation() == Orientation.down) {
                moveRight();
            }
            if (getCurrentOrientation() == Orientation.right) {
                moveRight();
                moveRight();
            }
        }

        if (next[0] - cur[0] < 0) {
            if (getCurrentOrientation() == Orientation.left) {
                moveRight();
            }
            if (getCurrentOrientation() == Orientation.right) {
                moveLeft();
            }
            if (getCurrentOrientation() == Orientation.down) {
                moveRight();
                moveRight();
            }
        }

        if (next[0] - cur[0] > 0) {
            if (getCurrentOrientation() == Orientation.left) {
                moveLeft();
            }
            if (getCurrentOrientation() == Orientation.right) {
                moveRight();
            }
            if (getCurrentOrientation() == Orientation.up) {
                moveRight();
                moveRight();
            }
        }

        this.moveForward();

        restaurant.getEnv().addComponent(cur, 0, restaurant.typeColor(0));
    }
}