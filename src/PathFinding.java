import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import fr.emse.fayol.maqit.simulator.environment.ColorCell;

public class PathFinding {
    private Restaurant restaurant;

    public PathFinding(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    private int manhattanDistance(int ox, int oy, int x, int y) {
        return Math.abs(y - oy) + Math.abs(ox - x);
    }

    public ArrayList<CellNode> freeNeighboringCells(CellNode cell) {
        int x = cell.getCoords()[1];
        int y = cell.getCoords()[0];
        ArrayList<CellNode> res = new ArrayList<CellNode>();

        int cx = 0;
        int cy = 0;

        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    cx = x + 1;
                    cy = y;
                    break;

                case 1:
                    cx = x;
                    cy = y - 1;
                    break;

                case 2:
                    cx = x;
                    cy = y + 1;
                    break;

                default:
                    cx = x - 1;
                    cy = y;
                    break;
            }
    
            if (restaurant.getEnv().validPosition(cy, cx)) {
                int content = restaurant.getEnv().getEnvironment().getCellContent(cy, cx);
                CellNode cur = new CellNode(new ColorCell(content, restaurant.typeColor(content)), new int[]{cy, cx}, null);

                if (content == 0)
                    res.add(cur);
            }
        }

        return res;
    }

    private CellNode chooseCell(ArrayList<CellNode> cells) {
        int minf = Integer.MAX_VALUE;
        CellNode res = cells.get(0);

        for (CellNode cell: cells) {
            int curf = cell.getDistance() + cell.getHeuristic();

            if (curf < minf) {
                minf = curf;
                res = cell;
            }
        }

        return res;
    }

    private int cellPresent(List<CellNode> lst, CellNode cell) {
        for (int i = 0; i < lst.size(); i++) {
            if (Arrays.equals(lst.get(i).getCoords(), cell.getCoords()))
                return i;
        }

        return -1;
    }

    public GridPath findPath(int[] start, int[] dest) {
        ArrayList<CellNode> open = new ArrayList<CellNode>();
        LinkedList<CellNode> close = new LinkedList<CellNode>();
        GridPath res = new GridPath();

        int content = restaurant.getEnv().getEnvironment().getCellContent(start[0], start[1]);
        CellNode cur = new CellNode(new ColorCell(content, restaurant.typeColor(content)), new int[] {start[0], start[1]}, null);
        cur.setDistance(0);
        cur.setHeuristic(manhattanDistance(start[0], start[1], dest[0], dest[1]));

        open.add(cur);

        while (! open.isEmpty()) {
            cur = chooseCell(open);

            if (Arrays.equals(cur.getCoords(), dest)) {
                res.addCell(cur);
                break;
            } else {
                close.add(cur);
                open.remove(cur);

                ArrayList<CellNode> children = freeNeighboringCells(cur);

                for (CellNode child: children) {
                    int tmp = cellPresent(close, child);
                    if (tmp != -1) {
                        child = close.get(tmp);
                    }

                    if (cur.getDistance() + 1 < child.getDistance()) {
                        child.setParent(cur);
                        child.setDistance(cur.getDistance() + 1);
                        child.setHeuristic(manhattanDistance(child.getCoords()[0], child.getCoords()[1], dest[0], dest[1]));

                        if (tmp != -1 ) {
                            close.remove(tmp);
                            close.add(child);
                        } else if (cellPresent(open, child) == -1) {
                            open.add(child);
                        }
                    }
                }
            }
        }
        
        cur = close.getLast();
        while (! Arrays.equals(cur.getCoords(), start)) {
            res.addCell(cur);
            cur = cur.getParent();
        }
        res.addCell(cur);        
            
        return res;
    }
}
