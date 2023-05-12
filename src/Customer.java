import java.util.ArrayList;
import java.util.Arrays;

import fr.emse.fayol.maqit.simulator.environment.ColorCell;
import fr.emse.fayol.maqit.simulator.environment.ColorGridEnvironment;

public class Customer {
    private Restaurant restaurant;
    private GridPath curPath;
    private int pathStep;
    private int[] pos;
    private Table goalTable;

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

                ArrayList<Integer> trans = new ArrayList<Integer>();
                trans.add(goalTable.getCoords()[0]);
                trans.add(goalTable.getCoords()[1]);
                restaurant.getAir().radioTransmission(new RadioData(null, 0, trans));

                return true;
            }

            followPath();
        } else {
            for (Table table: restaurant.getTables()) {
                if (! table.isTaken()) {
                    goalTable = table;
                    int[] coords = table.takeTable(pos);

                    PathFinding solver = new PathFinding(restaurant);
                    int content = restaurant.getEnv().getEnvironment().getCellContent(pos[0], pos[1]);
                    CellNode start = new CellNode(new ColorCell(content, restaurant.typeColor(content)), pos);
                    curPath = solver.findPath(start, coords);

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