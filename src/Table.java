import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import fr.emse.fayol.maqit.simulator.environment.ColorGridEnvironment;

enum TableState {
    free(2),
    customerArriving(2),
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
    private TableState state;
    private int[] coords;
    private Restaurant restaurant;
    private int count;
    private boolean waiterAssigned;
    private RadioData tmpDat;

    public Table(int[] coords, Restaurant restaurant) {
        state = TableState.free;
        this.coords = coords;
        this.restaurant = restaurant;
        count = 0;

        Collections.shuffle(restaurant.getTables());
    }

    public int[] findAccessor(int[] origin) {
        PathFinding solver = new PathFinding(restaurant);

        ArrayList<CellNode> accessors = new ArrayList<CellNode>();
        accessors = solver.freeNeighboringCells(coords);

        if (accessors.size() == 0) {
            accessors = solver.neighboringCells(coords);

            for (CellNode accessor: accessors) {
                if (accessor.getCell().getColor() == restaurant.typeColor(5) || accessor.getCell().getColor() == restaurant.typeColor(6))
                    return accessor.getCoords();
            }
        }

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
        changeTableState(TableState.customerArriving);

        return findAccessor(orig);
    }

    public void radioReception(RadioData dat) {
        if (dat.getCommandId() == 5) {
            int[] target = new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)};

            if (Arrays.equals(target, coords))
                waiterAssigned = true;
        }
    }

    private void askForWaiter() {
        if (waiterAssigned)
            return;

        restaurant.getAir().radioTransmission(tmpDat);
    }

    public void sit(RadioData dat) {
        changeTableState(TableState.waitingToOrder);
        waiterAssigned = false;
        tmpDat = dat;
    }

    private void leave() {
        int[] leavingPos = findAccessor(coords);

        if (leavingPos == null)
            return;

        restaurant.getCustomers().add(new Customer(leavingPos, restaurant, true));
        restaurant.getEnv().addComponent(leavingPos, 5, restaurant.typeColor(5));

        ((ColorGridEnvironment) restaurant.getEnv().getEnvironment()).changeCell(coords[0], coords[1], 2, restaurant.typeColor(2));
        restaurant.getEnv().moveComponent(coords, coords, 2);

        changeTableState(TableState.free);
        tmpDat = null;
    }

    public void changeTableState(TableState step) {
        state = step;
        count = 0;

        ((ColorGridEnvironment) restaurant.getEnv().getEnvironment()).changeCell(coords[0], coords[1], step.getValue(), restaurant.typeColor(step.getValue()));
        restaurant.getEnv().moveComponent(coords, coords, step.getValue());
    }

    public int[] getCoords() {
        return coords;
    }

    public boolean isTaken() {
        return (state != TableState.free);
    }

    public TableState getTableState() {
        return state;
    }

    public void update() {
        count++;
    
        if (state == TableState.waitingToOrder && ! waiterAssigned && count % 10 == 0)
            askForWaiter();
    
        if (state == TableState.occupied && count % 50 == 0)
            leave();
    }
}