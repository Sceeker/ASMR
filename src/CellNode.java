import fr.emse.fayol.maqit.simulator.environment.ColorCell;

public class CellNode {
    private ColorCell cl;
    private int[] coords;
    private CellNode parent;
    private int g;
    private int h;

    public CellNode(ColorCell cell, int[] coords, CellNode parent) {
        this(cell, coords, parent, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public CellNode(ColorCell cell, int[] coords, CellNode parent, int g, int h) {
        cl = cell;
        this.coords = coords;
        this.parent = parent;
        this.g = g;
        this.h = h;
    }

    public void setCell(ColorCell ColorCell) {
        cl = ColorCell;
    }

    public ColorCell getCell() {
        return cl;
    }

    public void setCoords(int[] coords) {
        this.coords = coords;
    }

    public void setParent(CellNode parent) {
        this.parent = parent;
    }

    public CellNode getParent() {
        return parent;
    }

    public int[] getCoords() {
        return coords;
    }

    public void setHeuristic(int h) {
        this.h = h;
    }

    public int getHeuristic() {
        return h;
    }

    public void setDistance(int g) {
        this.g = g;
    }

    public int getDistance() {
        return g;
    }
}
