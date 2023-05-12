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

    public ArrayList<CellNode> freeNeighboringCells(CellNode cell, int[] dest) {
        int x = cell.getCoords()[1];
        int y = cell.getCoords()[0];
        ArrayList<CellNode> res = new ArrayList<CellNode>();

        int cx = 0;
        int cy = 0;

        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    cx = x;
                    cy = y + 1;
                    break;

                case 1:
                    cx = x - 1;
                    cy = y;
                    break;

                case 2:
                    cx = x + 1;
                    cy = y;
                    break;

                default:
                    cx = x;
                    cy = y - 1;
                    break;
            }
    
            if (restaurant.getEnv().validPosition(cy, cx)) {
                int content = restaurant.getEnv().getEnvironment().getCellContent(cy, cx);
                CellNode cur = new CellNode(new ColorCell(content, restaurant.typeColor(content)), new int[]{cy, cx});

                cur.setDistance(cell.getDistance() + 1);
                cur.setHeuristic(manhattanDistance(cy, cx, dest[0], dest[1]));

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

    public GridPath findPath(CellNode start, int[] dest) {
        ArrayList<CellNode> open = new ArrayList<CellNode>();
        LinkedList<CellNode> close = new LinkedList<CellNode>();

        start.setDistance(0);
        start.setHeuristic(manhattanDistance(start.getCoords()[0], start.getCoords()[1], dest[0], dest[1]));
        open.add(start);

        while (! open.isEmpty()) {
            CellNode cur = chooseCell(open);
            open.remove(cur);
            close.add(cur);

            if (Arrays.equals(cur.getCoords(), dest)) {
                break;
            } else {
                ArrayList<CellNode> children = freeNeighboringCells(cur, dest);

                for (CellNode child: children) {
                    if (! (cellPresent(close, child) > 0  || cellPresent(open, child) > 0)) {
                        child.setDistance(cur.getDistance() + 1);
                        child.setHeuristic(manhattanDistance(child.getCoords()[0], child.getCoords()[1], dest[0], dest[1]));
                        open.add(child);
                    } else {
                        boolean inClose = false;

                        int tmp = cellPresent(close, child);
                        if (tmp > 0) {
                            inClose = true;
                            child = close.get(tmp);
                        }

                        tmp = cellPresent(open, child);
                        if (tmp > 0) {
                            child = open.get(tmp);
                        }

                        if (child.getDistance() > cur.getDistance() + 1) {
                            CellNode newChild = child;
                            newChild.setDistance(cur.getDistance() + 1);

                            if (inClose) {
                                close.remove(child);
                            } else {
                                open.remove(child);
                            }

                            open.add(child);
                        }
                    }
                }
            }
        }

        GridPath res = new GridPath();
        for (CellNode cell: close)
            res.addCell(cell);
            
        return res;
    }
}
