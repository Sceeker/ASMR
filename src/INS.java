import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import fr.emse.fayol.maqit.simulator.components.Orientation;
import fr.emse.fayol.maqit.simulator.robot.GridTurtlebot;

enum INSState {
    waiting,
    taking,
    picking,
    delivering,
}

public class INS extends GridTurtlebot {
    private Restaurant restaurant;
    private int[] orderOnHold;
    private GridPath curPath;
    private int pathStep;
    private boolean follow;
    private INSState state;

    public INS(int id, String name, int field, int debug, int[] pos, int r, int c, Restaurant restaurant) {
        super(id, name, field, debug, pos, r, c);

        this.restaurant = restaurant;
        orderOnHold = null;
        state = INSState.waiting;
        pathStep = 0;
    }

    private void manageOrder() {
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
        if (orderOnHold != null) {
            state = INSState.waiting;
            orderOnHold = null;
        }
    }

    private Table getTable() {
        for (Table table: restaurant.getTables()) {
            if (Arrays.equals(table.getCoords(), orderOnHold))
                return table;
        }

        return null;
    }

    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            case 0:     // Une table veut commander
                if (state == INSState.waiting) {
                    orderOnHold = new int[] {dat.getCommandData().get(2), dat.getCommandData().get(3)};
                    state = INSState.taking;
                    registerOrder();

                    PathFinding solver = new PathFinding(restaurant);
                    curPath = solver.findPath(getLocation(), new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)});

                    ArrayList<Integer> trans = new ArrayList<Integer>();
                    trans.add(curPath.getDistance());
                    trans.add(orderOnHold[0]);
                    trans.add(orderOnHold[1]);
                    restaurant.getAir().radioTransmission(new RadioData(this, 1, trans));
                }
                break;

            case 1:     // Distance d'un autre INS
                if (state == INSState.taking && orderOnHold[0] == dat.getCommandData().get(1) && orderOnHold[1] == dat.getCommandData().get(2) && curPath.getDistance() > dat.getCommandData().get(0)) {
                    cancelOrder();
                }
                break;

            case 3:     // Une commande est prête
                if (state == INSState.waiting) {
                    orderOnHold = new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)};
                    state = INSState.picking;
                    registerOrder();

                    PathFinding solver = new PathFinding(restaurant);
                    Random rng = new Random();
                    curPath = solver.findPath(getLocation(), restaurant.getCollectPoints().get(rng.nextInt(restaurant.getCollectPoints().size())));

                    ArrayList<Integer> trans = new ArrayList<Integer>();
                    trans.add(curPath.getDistance());
                    trans.add(orderOnHold[0]);
                    trans.add(orderOnHold[1]);
                    restaurant.getAir().radioTransmission(new RadioData(this, 4, trans));
                }
                break;

            case 4:

            default :
                break;
        }
    }

    @Override
    public void move(int arg0) {
        if (follow) {
            if (pathStep < curPath.getDistance())
                followPath();

            if (Arrays.equals(curPath.coordsArray()[curPath.coordsArray().length - 1], getLocation())) {
                switch (state) {
                    case picking:
                        System.out.println("[INS] Order picked up");
                        state = INSState.delivering;

                        PathFinding solver = new PathFinding(restaurant);
                        curPath = solver.findPath(getLocation(), getTable().findAccessor(getLocation())); 
                        break;

                    case delivering:
                        System.out.println("[INS] Order delivered");
                        getTable().changeTableState(TableState.occupied);
                        state = INSState.waiting;
                        follow = false;
                        orderOnHold = null;
                        curPath = null;
                        break;

                    default:
                        System.out.println("[INS] Order taken");
                        getTable().changeTableState(TableState.waitingForOrder);

                        ArrayList<Integer> trans = new ArrayList<Integer>();
                        trans.add(orderOnHold[0]);
                        trans.add(orderOnHold[1]);
                        restaurant.getAir().radioTransmission(new RadioData(this, 2, trans));
                        state = INSState.waiting;
                        follow = false;
                        orderOnHold = null;
                        curPath = null;
                        break;
                }

                pathStep = 0;
            }
        }
    }

    public void followPath() {
        int[] cur = curPath.coordsArray()[pathStep];

        pathStep++;

        int[] next = curPath.coordsArray()[pathStep];

        restaurant.getEnv().moveComponent(getLocation(), next, 6);

        restaurant.getEnv().addComponent(getLocation(), 0, restaurant.typeColor(0));

        setLocation(next);

        restaurant.getEnv().addComponent(cur, 0, restaurant.typeColor(0));
    }
}