import java.util.ArrayList;
import java.util.Arrays;

import fr.emse.fayol.maqit.simulator.environment.ColorGridEnvironment;

public class Customer {
    private Restaurant restaurant;
    private GridPath curPath;
    private int pathStep;
    private int[] pos;
    private int[] goal;

    public Customer(int[] pos, Restaurant restaurant) {
        this.restaurant = restaurant;
        curPath = null;
        pathStep = 1;
        this.pos = pos;
    }

    public boolean move() {
        if (curPath != null) {
            int[] goal = curPath.getPath().getLast().getCoords();
            if (Arrays.equals(goal, pos)) {
                ((ColorGridEnvironment) restaurant.getEnv().getEnvironment()).changeCell(pos[0], pos[1], 0, restaurant.typeColor(0));
                restaurant.getEnv().moveComponent(pos, pos, 0);

                ((ColorGridEnvironment) restaurant.getEnv().getEnvironment()).changeCell(goal[0], goal[1], 0, restaurant.typeColor(7));
                restaurant.getEnv().moveComponent(goal, goal, 7);

                ArrayList<Integer> trans = new ArrayList<Integer>();
                trans.add(goal[0]);
                trans.add(goal[1]);
                restaurant.getAir().radioTransmission(new RadioData(null, 0, trans));

                return true;
            }

            followPath();
        } else {
            for (Table table: restaurant.getTables()) {
                if (! table.isTaken()) {
                    goal = table.takeTable(pos);

                    PathFinding solver = new PathFinding(restaurant);
                    curPath = solver.findPath(new int[] {pos[0], pos[1]}, goal);

                    break;
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