import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

import fr.emse.fayol.maqit.simulator.environment.ColorGridEnvironment;

enum TableState {
    waitingToOrder(8),
    waitingForOrder(9),
    occupied(7);

    private final int value;

    private TableState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

public class Table {
    private boolean taken;
    private int[] coords;
    private Restaurant restaurant;
    private boolean waiterAssigned;

    public Table(int[] coords, Restaurant restaurant) {
        taken = false;
        this.coords = coords;
        this.restaurant = restaurant;
        waiterAssigned = false;

        Collections.shuffle(restaurant.getTables());
    }

    public int[] findAccessor(int[] origin) {
        PathFinding solver = new PathFinding(restaurant);

        ArrayList<CellNode> accessors = new ArrayList<CellNode>();
        accessors = solver.freeNeighboringCells(coords);

        if (accessors.size() == 0)
            return null;

        int minDist = Integer.MAX_VALUE;
        CellNode res = accessors.get(0);
        for (CellNode accessor: accessors) {
            int curDist = PathFinding.manhattanDistance(coords[0], coords[1], origin[0], origin[1]);
            if (curDist < minDist) {
                curDist = accessor.getHeuristic();
                res = accessor;
            }
        }

        return new int[] {res.getCoords()[0], res.getCoords()[1]};
    }

    public int[] takeTable(int[] orig) {
        taken = true;

        return findAccessor(orig);
    }

    private void scheduleLeaving() {
        TimerTask task = new TimerTask() {
            public void run() {
                leave();
            }
        };

        Timer timer = new Timer();
        
        timer.schedule(task, 5000);
    }

    public void radioReception(RadioData dat) {
        if (dat.getCommandId() == 5) {
            int[] target = new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)};

            if (Arrays.equals(target, coords))
                waiterAssigned = true;
        }
    }

    private void askForWaiter(RadioData dat) {
        if (waiterAssigned)
            return;

        restaurant.getAir().radioTransmission(dat);

        TimerTask task = new TimerTask() {
            public void run() {
                askForWaiter(dat);
            }
        };

        Timer timer = new Timer();
        
        timer.schedule(task, 2000);
    }

    public void sit(RadioData dat) {
        waiterAssigned = false;

        askForWaiter(dat);
    }

    private void leave() {
        int[] leavingPos = findAccessor(coords);

        if (leavingPos == null) {
            scheduleLeaving();
            return;
        }

        restaurant.getCustomers().add(new Customer(leavingPos, restaurant, true));
        restaurant.getEnv().addComponent(leavingPos, 5, restaurant.typeColor(5));

        ((ColorGridEnvironment) restaurant.getEnv().getEnvironment()).changeCell(coords[0], coords[1], 2, restaurant.typeColor(2));
        restaurant.getEnv().moveComponent(coords, coords, 2);
    }

    public void changeTableState(TableState step) {
        ((ColorGridEnvironment) restaurant.getEnv().getEnvironment()).changeCell(coords[0], coords[1], step.getValue(), restaurant.typeColor(step.getValue()));
        restaurant.getEnv().moveComponent(coords, coords, step.getValue());

        if (step == TableState.occupied) {
            scheduleLeaving();
        }
    }

    public int[] getCoords() {
        return coords;
    }

    public boolean isTaken() {
        return taken;
    }
}