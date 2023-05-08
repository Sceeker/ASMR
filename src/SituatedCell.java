import fr.emse.fayol.maqit.simulator.environment.Cell;

public class SituatedCell {
    private Cell cl;
    private int[] coords;

    public SituatedCell(Cell cell, int[] coords) {
        cl = cell;
        this.coords = coords;
    }

    public void setCell(Cell cell) {
        cl = cell;
    }

    public Cell getCell() {
        return cl;
    }

    public void setCoords(int[] coords) {
        this.coords = coords;
    }

    public int[] getCoords() {
        return coords;
    }
}
