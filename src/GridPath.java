import java.util.LinkedList;

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
