import java.util.LinkedList;

import fr.emse.fayol.maqit.simulator.environment.ColorCell;

public class GridPath {
    private LinkedList<CellNode> path;

    public GridPath() {
        path = new LinkedList<CellNode>();
    }
    
    public int[][] coordsArray() {
        int[][] res = new int[path.size()][2];
        int i = 0;

        for (CellNode cur: path) {
            res[i++] = cur.getCoords();
        }

        return res;
    }

    public void addCoords(int[] coords) {
        path.add(new CellNode(new ColorCell(0, new int[] {0, 0, 0}), coords, null));
    }

    public void addCell(CellNode cell) {
        path.addFirst(cell);
    }


    public void setPath(LinkedList<CellNode> path) {
        this.path = path;
    }

    public LinkedList<CellNode> getPath() {
        return path;
    }

    public int getDistance() {
        return path.size() - 1;
    }
}
