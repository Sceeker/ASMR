import java.util.ArrayList;
import java.util.LinkedList;

import org.junit.internal.runners.InitializationError;

import fr.emse.fayol.maqit.simulator.robot.GridTurtlebot;
import fr.emse.fayol.maqit.simulator.environment.ColorCell;

public class INS extends GridTurtlebot {
    private OpenGridManagement restaurantLayout;

    public INS(int id, String name, int field, int debug, int[] pos, int r, int c, OpenGridManagement env) {
        super(id, name, field, debug, pos, r, c);

        restaurantLayout = env;
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

    private ArrayList<CellNode> freeNeighboringCells(CellNode cell) {
        int x = cell.getCoords()[0];
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

                case 3:
                    cx = x;
                    cy = y - 1;
                    break;
            }
                
            CellNode cur = new CellNode(new ColorCell(restaurantLayout.getEnvironment().getCellContent(x, y)), new int[]{x, y});

            if (cur.getCell().getColor() == Restaurant.typeColor(0))
                res.add(cur);
        }

        return res;
    }

    private int manhattanDistance(int ox, int oy, int x, int y) {
        return Math.abs(y - oy) + Math.abs(ox - x);
    }

    private GridPath findPath(int x, int y) {
        ArrayList<CellNode> open = new ArrayList<CellNode>();
        LinkedList<CellNode> close = new LinkedList<CellNode>();
        GridPath res = new GridPath();

        open.add(new CellNode(new ColorCell(restaurantLayout.getEnvironment().getCellContent(x, y)), new int[]{x, y}));

        while (! open.isEmpty()) {
            CellNode cur = chooseCell(open);
            open.remove(cur);
            close.add(cur);
            res.addCell(cur);

            if (cur.getCoords()[0] == x && cur.getCoords()[1] == y) {
                return res;
            } else {
                ArrayList<CellNode> children = freeNeighboringCells(cur);

                for (CellNode child: children) {
                    if (! (open.contains(child) || close.contains(child))) {
                        child.setDistance(cur.getDistance() + 1);
                        child.setHeuristic(manhattanDistance(child.getCoords()[0], child.getCoords()[1], x, y));
                        open.add(child);
                    } else {
                        if (child.getDistance() > cur.getDistance() + 1) {
                            child.setDistance(cur.getDistance() + 1);
                            
                            if (close.contains(child)) {
                                close.remove(child);
                                close.add(child);
                            }
                        }
                    }
                }
            }
        }

        return res;
    }

    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            default :
                break;
        }
    }

    @Override
    public void move(int arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }


}
