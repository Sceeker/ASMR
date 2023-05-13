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
                    restaurant.getAir().radioTransmission(new RadioData(null, 0, trans));
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

                PathFinding solver = new PathFinding(restaurant);
                curPath = solver.findPath(new int[] {pos[0], pos[1]}, goal);
            } else {
                for (Table table: restaurant.getTables()) {
                    if (! table.isTaken()) {
                        goal = table.takeTable(pos);
                        goalTable = table;
    
                        PathFinding solver = new PathFinding(restaurant);
                        curPath = solver.findPath(new int[] {pos[0], pos[1]}, goal);
    
                        break;
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

        restaurant.getEnv().moveComponent(pos, next, 5);

        restaurant.getEnv().addComponent(pos, 0, restaurant.typeColor(0));

        pos = next;

        pathStep++;
    }
}