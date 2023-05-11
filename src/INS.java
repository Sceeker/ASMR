import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import fr.emse.fayol.maqit.simulator.components.Orientation;
import fr.emse.fayol.maqit.simulator.robot.GridTurtlebot;
import fr.emse.fayol.maqit.simulator.environment.ColorCell;

public class INS extends GridTurtlebot {
    private OpenGridManagement restaurantLayout;
    private boolean orderOnHold;
    private GridPath curPath;

    public INS(int id, String name, int field, int debug, int[] pos, int r, int c, OpenGridManagement env) {
        super(id, name, field, debug, pos, r, c);

        restaurantLayout = env;
        orderOnHold = false;
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
                
            CellNode cur = new CellNode(new ColorCell(restaurantLayout.getEnvironment().getCellContent(cx, cy)), new int[]{cx, cy});

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

    private void manageOrder() {
        if (orderOnHold) {

        }
    }

    private void registerOrder() {
        orderOnHold = true;

        TimerTask task = new TimerTask() {
            public void run() {
                manageOrder();
            }
        };

        Timer timer = new Timer();
        
        timer.schedule(task, 1000);
    }

    private void cancelOrder() {
        if (orderOnHold)
            orderOnHold = false;
    }

    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            case 0:     // Une table veut commander
                registerOrder();

                curPath = findPath(dat.getCommandData().get(0), dat.getCommandData().get(1));
                Main.air.radioTransmission(new RadioData(this, 1, new ArrayList<Integer>(curPath.getDistance())));
                break;

            case 1:     // Distance d'un autre INS
                if (orderOnHold && curPath.getDistance() > dat.getCommandData().get(0)) {
                    cancelOrder();
                }
                break;

            default :
                break;
        }
    }

    @Override
    public void move(int arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'move'");
    }

    public void followPath (int[][] coord) {
        for (int i = 0; i < coord.length; i++) {
            if (coord[i + 1][0] - coord[i][0] > 0) {
                if (getCurrentOrientation() == Orientation.up) {
                    this.moveRight();
                }
                if (getCurrentOrientation() == Orientation.down) {
                    this.moveLeft();
                }
                if (getCurrentOrientation() == Orientation.left) {
                    this.moveRight();
                    this.moveRight();
                }
            }

            if (coord[i + 1][0] - coord[i][0] < 0) {
                if (getCurrentOrientation() == Orientation.up) {
                    this.moveLeft();
                }
                if (getCurrentOrientation() == Orientation.down) {
                    this.moveRight();
                }
                if (getCurrentOrientation() == Orientation.right) {
                    this.moveRight();
                    this.moveRight();
                }
            }

            if (coord[i + 1][1] - coord[i][1] > 0) {
                if (getCurrentOrientation() == Orientation.left) {
                    this.moveRight();
                }
                if (getCurrentOrientation() == Orientation.right) {
                    this.moveLeft();
                }
                if (getCurrentOrientation() == Orientation.down) {
                    this.moveRight();
                    this.moveRight();
                }
            }

                if (coord[i + 1][1] - coord[i][1] < 0) {
                    if (getCurrentOrientation() == Orientation.left) {
                        this.moveLeft();
                    }
                    if (getCurrentOrientation() == Orientation.right) {
                        this.moveRight();
                    }
                    if (getCurrentOrientation() == Orientation.up) {
                        this.moveRight();
                        this.moveRight();
                    }
            }
            this.moveForward();
        }
    }
}