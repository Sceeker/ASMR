import fr.emse.fayol.maqit.simulator.environment.ColorCell;

public class CellNode {
    private ColorCell cl;
    private int[] coords;
    private int g;
    private int h;

    public CellNode(ColorCell ColorCell, int[] coords) {
        cl = ColorCell;
        this.coords = coords;
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
