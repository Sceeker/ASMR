import java.util.ArrayList;

import fr.emse.fayol.maqit.simulator.environment.ColorCell;

public class Table {
    private boolean taken;
    private int[] coords;
    private Restaurant restaurant;

    public Table(int[] coords, Restaurant restaurant) {
        taken = false;
        this.coords = coords;
        this.restaurant = restaurant;
    }

    public int[] takeTable(int[] orig) {
        taken = true;

        PathFinding solver = new PathFinding(restaurant);

        ArrayList<CellNode> accessors = new ArrayList<CellNode>();
        accessors = solver.freeNeighboringCells(new CellNode(new ColorCell(2, restaurant.typeColor(2)), coords), orig);


        int minDist = Integer.MAX_VALUE;
        CellNode res = accessors.get(0);
        for (CellNode accessor: accessors) {
            if (accessor.getHeuristic() < minDist) {
                minDist = accessor.getHeuristic();
                res = accessor;
            }
        }

        return new int[] {res.getCoords()[0], res.getCoords()[1]};
    }

    public int[] getCoords() {
        return coords;
    }

    public boolean isTaken() {
        return taken;
    }
}