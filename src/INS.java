import java.util.ArrayList;
import java.util.LinkedList;

import fr.emse.fayol.maqit.simulator.robot.GridTurtlebot;
import fr.emse.fayol.maqit.simulator.environment.Cell;
import fr.emse.fayol.maqit.simulator.environment.GridManagement;

public class INS extends GridTurtlebot {
    private GridManagement restaurantLayout;

    public INS(int id, String name, int field, int debug, int[] pos, int r, int c, GridManagement env) {
        super(id, name, field, debug, pos, r, c);

        restaurantLayout = env;
    }

    private GridPath findPath(int x, int y) {
        ArrayList<SituatedCell> open = new ArrayList<SituatedCell>();
        LinkedList<SituatedCell> close = new LinkedList<SituatedCell>();
        GridPath res = new GridPath();

        // COMMENT AVOIR LA PUTAIN DE CELL ??? DU GRID MANAGZEMNT ??? ENV PROTEC ???
        open.add(new SituatedCell(restaurantLayou, new int[2] = {this.x, this.y}));
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
