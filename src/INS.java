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
    private boolean recompute;

    public INS(int id, String name, int field, int debug, int[] pos, int r, int c, Restaurant restaurant) {
        super(id, name, field, debug, pos, r, c);

        this.restaurant = restaurant;
        orderOnHold = null;
        state = INSState.waiting;
        recompute = false;
        pathStep = 0;
    }

    private void manageOrder() {
        if (state != INSState.waiting) {
            follow = true;

            ArrayList<Integer> trans = new ArrayList<Integer>();
            trans.add(orderOnHold[0]);
            trans.add(orderOnHold[1]);
            restaurant.getAir().radioTransmission(new RadioData(this, 5, trans));
        }
    }

    private void registerOrder() {
        TimerTask task = new TimerTask() {
            public void run() {
                manageOrder();
            }
        };

        Timer timer = new Timer();
        
        timer.schedule(task, 20);
    }

    private void cancelOrder() {
        if (state != INSState.waiting) {
            state = INSState.waiting;
            follow = false;
            orderOnHold = null;
            curPath = null;
        }
    }

    private Table getTable() {
        for (Table table: restaurant.getTables()) {
            if (Arrays.equals(table.getCoords(), orderOnHold))
                return table;
        }

        return null;
    }

    private void transmitDistance(INS bot) {
        TimerTask task = new TimerTask() {
            public void run() {
                if (state != INSState.waiting) {
                    ArrayList<Integer> trans = new ArrayList<Integer>();
                    trans.add(curPath.getDistance());
                    trans.add(orderOnHold[0]);
                    trans.add(orderOnHold[1]);
                    restaurant.getAir().radioTransmission(new RadioData(bot, 1, trans));
                }
            }
        };

        Timer timer = new Timer();
        
        timer.schedule(task, 10);
    }

    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            case 0:     // Une table veut commander
                if (state == INSState.waiting) {
                    orderOnHold = new int[] {dat.getCommandData().get(2), dat.getCommandData().get(3)};
                    state = INSState.taking;
                    registerOrder();

                    computePath(new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)});

                    transmitDistance(this);
                }
                break;

            case 1:    // Distance d'un autre INS
                if (state != INSState.waiting && Arrays.equals(orderOnHold, new int[] {dat.getCommandData().get(1), dat.getCommandData().get(2)})) {
                    if (curPath.getDistance() >= dat.getCommandData().get(0))
                        cancelOrder();
                }
                break;

            case 3:     // Une commande est prête
                if (state == INSState.waiting) {
                    orderOnHold = new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)};
                    state = INSState.picking;
                    registerOrder();

                    Random rng = new Random();
                    computePath(restaurant.getCollectPoints().get(rng.nextInt(restaurant.getCollectPoints().size())));

                    transmitDistance(this);
                }
                break;

            case 6:
                if (state == INSState.waiting) {
                    PathFinding solver = new PathFinding(restaurant);
                    ArrayList<int[]> free = solver.neighboringCoords(getLocation());
                    free.add(getLocation());
                    int[] check = new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)};

                    boolean move = false;

                    for (int[] coords: free) {
                        if (Arrays.equals(coords, check)) {
                            move = true;
                            break;
                        }
                    }

                    if (move) {
                        free = solver.freeNeighboringCoords(getLocation());

                        curPath = new GridPath();
                        curPath.addCoords(getLocation());
                        curPath.addCoords(free.get(0));
                        pathStep = 0;
                        follow = true;
                    }
                }
                break;

            default :
                break;
        }
    }

    private void computePath(int[] dest) {
        PathFinding solver = new PathFinding(restaurant);

        ArrayList<Integer> trans = new ArrayList<Integer>();
        if (solver.freeNeighboringCoords(getLocation()).isEmpty()) {
            trans.add(getLocation()[0]);
            trans.add(getLocation()[1]);
        } else if (solver.freeNeighboringCoords(dest).isEmpty() || restaurant.getEnv().getEnvironment().getCellContent(dest[0], dest[1]) != 0) {
            trans.add(dest[0]);
            trans.add(dest[1]);
        }
        
        if (! trans.isEmpty()) {
            follow = false;
            recompute = true;

            restaurant.getAir().radioTransmission(new RadioData(this, 6, trans));

            return;
        }

        curPath = solver.findPath(getLocation(), dest);
        pathStep = 0;
        if (state == INSState.waiting)
            follow = true;
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
                        
                        int[] tablepos = getTable().findAccessor(getLocation());
                        computePath(tablepos);
                        break;

                    case delivering:
                        System.out.println("[INS] Order delivered");
                        getTable().changeTableState(TableState.occupied);
                        state = INSState.waiting;
                        follow = false;
                        orderOnHold = null;
                        curPath = null;
                        break;

                    case taking:
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

                    default:
                        state = INSState.waiting;
                        follow = false;
                        curPath = null;
                        break;    
                }

                pathStep = 0;
            }
        } else if (recompute) {
            recompute = false;

            if (state == INSState.delivering) {
                int[] tablepos = getTable().findAccessor(getLocation());
                computePath(tablepos);
            } else {
                computePath(orderOnHold);
            }

            if (! recompute)
                follow = true;
        }
    }

    public void followPath() {
        int[] cur = curPath.coordsArray()[pathStep];

        pathStep++;

        int[] next = curPath.coordsArray()[pathStep];

        PathFinding solver = new PathFinding(restaurant);
        ArrayList<int[]> free = solver.freeNeighboringCoords(cur);

        boolean gogogo = false;

        for (int[] coord: free) {
            if (Arrays.equals(coord, next)) {
                gogogo = true;
                break;
            }
        }

        if (gogogo) {  
            setLocation(next);
    
            restaurant.getEnv().moveComponent(cur, next, 6);
            restaurant.getEnv().addComponent(cur, 0, restaurant.typeColor(0));
            restaurant.getEnv().getEnvironment().changeCell(cur[0], cur[1], 0);
        } else {
            computePath(curPath.coordsArray()[curPath.coordsArray().length - 1]);
        }
    }
}