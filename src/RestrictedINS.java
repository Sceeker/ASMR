import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class RestrictedINS extends INS {
    private int[] boundaryTL;
    private int[] boundaryBR;

    public RestrictedINS(int id, String name, int field, int debug, int[] pos, int r, int c, Restaurant restaurant, int[] boudaryTL, int[] boundaryBR) {
        super(id, name, field, debug, pos, r, c, restaurant);

        this.boundaryBR = boundaryBR;
        this.boundaryTL = boudaryTL;
    }

    private boolean inBoundary(int[] coords) {
        int relR = coords[0] - boundaryTL[0];
        int relC = coords[1] - boundaryTL[1];

        if (relR < 0 || relC < 0)
            return false;

        if (relR > boundaryBR[0] || relC > boundaryBR[1])
            return false;

        return true;
    }

    @Override
    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            case 0:     // Une table veut commander
                orderOnHold = new int[] {dat.getCommandData().get(2), dat.getCommandData().get(3)};
                if (state == INSState.waiting && inBoundary(orderOnHold)) {
                    state = INSState.taking;

                    computePath(new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)});

                    if (curPath != null)
                        transmitDistance();
                } else {
                    orderOnHold = null;

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
                orderOnHold = new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)};

                if (state == INSState.waiting && inBoundary(orderOnHold)) {
                    state = INSState.picking;

                    Random rng = new Random();
                    computePath(restaurant.getCollectPoints().get(rng.nextInt(restaurant.getCollectPoints().size())));

                    if (curPath != null)
                        transmitDistance();
                } else {
                    orderOnHold = null;
                    
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
}