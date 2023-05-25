import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import fr.emse.fayol.maqit.simulator.components.Orientation;
import fr.emse.fayol.maqit.simulator.robot.GridTurtlebot;

enum INSState {
    waiting,
    taking,
    picking,
    delivering,
}

public class INS extends GridTurtlebot {
    protected Restaurant restaurant;
    protected int[] orderOnHold;
    protected GridPath curPath;
    protected int pathStep;
    protected boolean follow;
    protected INSState state;
    protected boolean recompute;
    protected ArrayList<Integer> dists;

    public INS(int id, String name, int field, int debug, int[] pos, int r, int c, Restaurant restaurant) {
        super(id, name, field, debug, pos, r, c);

        this.restaurant = restaurant;
        orderOnHold = null;
        state = INSState.waiting;
        recompute = false;
        pathStep = 1;
        dists = new ArrayList<Integer>();
    }

    private void manageOrder() {
        if (state != INSState.waiting) {
            follow = true;
            dists.clear();

            ArrayList<Integer> trans = new ArrayList<Integer>();
            trans.add(orderOnHold[0]);
            trans.add(orderOnHold[1]);
            restaurant.getAir().radioTransmission(new RadioData(this, 5, trans));
        }
    }

    private void cancelOrder() {
        if (state != INSState.waiting) {
            state = INSState.waiting;
            follow = false;
            orderOnHold = null;
            curPath = null;
            dists.clear();
        }
    }

    private Table getTable() {
        for (Table table: restaurant.getTables()) {
            if (Arrays.equals(table.getCoords(), orderOnHold))
                return table;
        }

        return null;
    }

    private void transmitDistance() {
        if (state != INSState.waiting) {
            ArrayList<Integer> trans = new ArrayList<Integer>();
            trans.add(curPath.getDistance());
            trans.add(orderOnHold[0]);
            trans.add(orderOnHold[1]);
            restaurant.getAir().radioTransmission(new RadioData(this, 1, trans));
        }

        if (state == INSState.taking || state == INSState.picking) {
            if (dists.size() == restaurant.getBots().size() - 1)
                comparateurDeDistanceSuperSympa();
        }
    }

    private void comparateurDeDistanceSuperSympa() {
        if (curPath == null)
            return;

        for (int dist: dists) {
            if (curPath.getDistance() >= dist) {
                cancelOrder();
                return;
            }
        }

        manageOrder();
    }

    protected boolean thirdPartyCheck(int[] test) {
        return true;
    }

    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            case 0:     // Une table veut commander
                if (state == INSState.waiting && thirdPartyCheck(new int[] {dat.getCommandData().get(2), dat.getCommandData().get(3)})) {
                    orderOnHold = new int[] {dat.getCommandData().get(2), dat.getCommandData().get(3)};
                    state = INSState.taking;

                    computePath(new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)});

                    if (curPath != null)
                        transmitDistance();
                } else {
                    ArrayList<Integer> trans = new ArrayList<Integer>();
                    trans.add(Integer.MAX_VALUE);
                    trans.add(dat.getCommandData().get(2));
                    trans.add(dat.getCommandData().get(3));
                    restaurant.getAir().radioTransmission(new RadioData(this, 1, trans));
                }
                break;

            case 1:    // Distance d'un autre INS
                if (state != INSState.waiting && Arrays.equals(orderOnHold, new int[] {dat.getCommandData().get(1), dat.getCommandData().get(2)})) {
                    dists.add(dat.getCommandData().get(0));

                    if (dists.size() == restaurant.getBots().size() - 1)
                        comparateurDeDistanceSuperSympa();
                }
                break;

            case 3:     // Une commande est prête
                if (state == INSState.waiting && thirdPartyCheck(new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)})) {
                    orderOnHold = new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)};
                    state = INSState.picking;

                    Random rng = new Random();
                    computePath(restaurant.getCollectPoints().get(rng.nextInt(restaurant.getCollectPoints().size())));

                    if (curPath != null)
                        transmitDistance();
                } else {
                    ArrayList<Integer> trans = new ArrayList<Integer>();
                    trans.add(Integer.MAX_VALUE);
                    trans.add(dat.getCommandData().get(0));
                    trans.add(dat.getCommandData().get(1));
                    restaurant.getAir().radioTransmission(new RadioData(this, 1, trans));
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
                        pathStep = 1;
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
        } else if (solver.freeNeighboringCoords(dest).isEmpty() || (restaurant.getEnv().getEnvironment().getCellContent(dest[0], dest[1]) != 0 && getLocation() != dest)) {
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
        pathStep = 1;
        if (state == INSState.waiting)
            follow = true;

        if (recompute)
            transmitDistance();
    }

    @Override
    public void move(int arg0) {
        if (follow) {
            if (pathStep < curPath.getDistance())
                followPath();

            if (Arrays.equals(curPath.coordsArray()[curPath.coordsArray().length - 1], getLocation())) {
                switch (state) {
                    case picking:
                        state = INSState.delivering;
                        
                        int[] tablepos = getTable().findAccessor(getLocation());
                        computePath(tablepos);
                        break;

                    case delivering:
                        getTable().changeTableState(TableState.occupied);
                        state = INSState.waiting;
                        follow = false;
                        orderOnHold = null;
                        curPath = null;
                        break;

                    case taking:
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

                pathStep = 1;
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
        int[] cur = getLocation();

        pathStep++;

        int[] next = curPath.coordsArray()[pathStep];

        if (restaurant.getEnv().getEnvironment().getCellContent(next[0], next[1]) == 0) {  
            setLocation(next);

            // pas foufou il faudrait réutiliser les moveLeft, moveRight etc... mais ils semblent buggés (cf historique des commits) surtout visuellement
            // parfois les bots se déplacenet (pos change bien) mais pas leur case
            restaurant.getEnv().moveComponent(cur, next, 6);
            restaurant.getEnv().addComponent(cur, 0, restaurant.typeColor(0));
        } else {
            Random rng = new Random();
            if (rng.nextBoolean())
                computePath(curPath.coordsArray()[curPath.coordsArray().length - 1]);
        }
    }
}