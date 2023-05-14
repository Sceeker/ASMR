import java.util.ArrayList;
import java.util.Arrays;

import fr.emse.fayol.maqit.simulator.environment.ColorGridEnvironment;

public class Customer {
    private Restaurant restaurant;
    private GridPath curPath;
    private int pathStep;
    private int[] pos;
    private int[] goal;
    private Table goalTable;
    private boolean leaving;

    public Customer(int[] pos, Restaurant restaurant) {
        this.restaurant = restaurant;
        curPath = null;
        pathStep = 1;
        this.pos = pos;
    }

    public Customer(int[] pos, Restaurant restaurant, boolean leaving) {
        this(pos, restaurant);
        this.leaving = leaving;
    }

    private void computePath() {
        PathFinding solver = new PathFinding(restaurant);
        curPath = solver.findPath(new int[] {pos[0], pos[1]}, goal);
    }

    public boolean move() {
        if (curPath != null) {
            int[] goal = curPath.getPath().getLast().getCoords();
            if (Arrays.equals(goal, pos)) {
                ((ColorGridEnvironment) restaurant.getEnv().getEnvironment()).changeCell(pos[0], pos[1], 0, restaurant.typeColor(0));
                restaurant.getEnv().moveComponent(pos, pos, 0);

                if (! leaving) {
                    goalTable.changeTableState(TableState.waitingToOrder);
                    ArrayList<Integer> trans = new ArrayList<Integer>();
                    trans.add(goal[0]);
                    trans.add(goal[1]);
                    trans.add(goalTable.getCoords()[0]);
                    trans.add(goalTable.getCoords()[1]);
                    goalTable.sit(new RadioData(null, 0, trans));
                }

                return true;
            }

            followPath();
        } else {
            if (leaving) {
                int minDist = Integer.MAX_VALUE;
                for (int[] door: restaurant.getDoors()) {
                    int curDist = PathFinding.manhattanDistance(door[0], door[1], pos[0], pos[1]);

                    if (curDist < minDist) {
                        minDist = curDist;
                        goal = door;
                    }
                }

                computePath();
            } else {
                for (Table table: restaurant.getTables()) {
                    table = restaurant.getTables().get(13);
                    if (! table.isTaken()) {
                        goal = table.takeTable(pos);
                        
                        if (! (goal == null)) {
                            goalTable = table;
    
                            computePath();
        
                            break;
                        }
                    }
                }
            }
        }

        return false;
    }

    public int[] getPos() {
        return pos;
    }

    public void followPath() {
        int[] next = curPath.coordsArray()[pathStep];

        PathFinding solver = new PathFinding(restaurant);
        ArrayList<int[]> free = solver.freeNeighboringCoords(pos);

        boolean gogogo = false;

        for (int[] coord: free) {
            if (Arrays.equals(coord, next)) {
                gogogo = true;
                break;
            }
        }

        if (gogogo) {
            restaurant.getEnv().moveComponent(pos, next, 5);

            restaurant.getEnv().addComponent(pos, 0, restaurant.typeColor(0));

            pos = next;

            pathStep++;
        }
    }
}