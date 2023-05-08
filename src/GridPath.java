import java.util.LinkedList;

public class GridPath {
    private int distance;
    private LinkedList<SituatedCell> path;

    public GridPath() {
        distance = 0;
        path = new LinkedList<SituatedCell>();
    }
    
    public int[][] coordsArray() {
        int[][] res = new int[path.size()][2];
        int i = 0;

        for (SituatedCell cur: path) {
            res[i++] = cur.getCoords;
        }

        return res;
    }

    public void addCell(SituatedCell cell) {
        path.add(cell);
        computeDistance();
    }


    public void setPath(LinkedList<SituatedCell> path) {
        this.path = path;
    }

    public LinkedList<SituatedCell> getPath() {
        return path;
    }

    public int getDistance() {
        return distance;
    }

    private void computeDistance() {
        distance = path.size() - 1;
    }
}
