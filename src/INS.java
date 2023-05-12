import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import fr.emse.fayol.maqit.simulator.components.Orientation;
import fr.emse.fayol.maqit.simulator.environment.ColorCell;
import fr.emse.fayol.maqit.simulator.robot.GridTurtlebot;

public class INS extends GridTurtlebot {
    private Restaurant restaurant;
    private boolean orderOnHold;
    private boolean follow;
    private GridPath curPath;
    private int pathStep;

    public INS(int id, String name, int field, int debug, int[] pos, int r, int c, Restaurant restaurant) {
        super(id, name, field, debug, pos, r, c);

        this.restaurant = restaurant;
        orderOnHold = false;
        follow = false;
        pathStep = 0;
    }

    private void manageOrder() {
        orderOnHold = false;
        follow = true;
    }

    private void registerOrder() {
        orderOnHold = true;

        TimerTask task = new TimerTask() {
            public void run() {
                manageOrder();
            }
        };

        Timer timer = new Timer();
        
        timer.schedule(task, 1000);
    }

    private void cancelOrder() {
        if (orderOnHold)
            orderOnHold = false;
    }

    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            case 0:     // Une table veut commander
                registerOrder();

                PathFinding solver = new PathFinding(restaurant);
                int content = restaurant.getEnv().getEnvironment().getCellContent(getLocation()[0], getLocation()[1]);
                CellNode start = new CellNode(new ColorCell(content, restaurant.typeColor(content)), new int[] {getLocation()[0], getLocation()[1]});
                curPath = solver.findPath(start, new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)});

                ArrayList<Integer> trans = new ArrayList<Integer>();
                trans.add(curPath.getDistance());
                restaurant.getAir().radioTransmission(new RadioData(this, 1, trans));
                break;

            case 1:     // Distance d'un autre INS
                if (orderOnHold && curPath.getDistance() > dat.getCommandData().get(0)) {
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
        }
    }

    public void followPath() {
        int[] lastCoords = getLocation();
        int[][] coord = curPath.coordsArray();

        if (coord[pathStep + 1][1] - coord[pathStep][1] > 0) {
            if (getCurrentOrientation() == Orientation.up) {
                this.moveRight();
            }
            if (getCurrentOrientation() == Orientation.down) {
                this.moveLeft();
            }
            if (getCurrentOrientation() == Orientation.left) {
                this.moveRight();
                this.moveRight();
            }
        }

        if (coord[pathStep + 1][1] - coord[pathStep][1] < 0) {
            if (getCurrentOrientation() == Orientation.up) {
                this.moveLeft();
            }
            if (getCurrentOrientation() == Orientation.down) {
                this.moveRight();
            }
            if (getCurrentOrientation() == Orientation.right) {
                this.moveRight();
                this.moveRight();
            }
        }

        if (coord[pathStep + 1][0] - coord[pathStep][0] > 0) {
            if (getCurrentOrientation() == Orientation.left) {
                this.moveRight();
            }
            if (getCurrentOrientation() == Orientation.right) {
                this.moveLeft();
            }
            if (getCurrentOrientation() == Orientation.down) {
                this.moveRight();
                this.moveRight();
            }
        }

            if (coord[pathStep + 1][0] - coord[pathStep][0] < 0) {
                if (getCurrentOrientation() == Orientation.left) {
                    this.moveLeft();
                }
                if (getCurrentOrientation() == Orientation.right) {
                    this.moveRight();
                }
                if (getCurrentOrientation() == Orientation.up) {
                    this.moveRight();
                    this.moveRight();
                }
        }
        this.moveForward();

        restaurant.getEnv().addComponent(lastCoords, 0, restaurant.typeColor(0));
    }
}