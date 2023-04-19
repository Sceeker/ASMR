import java.util.List;

import fr.emse.fayol.maqit.simulator.SimFactory;
import fr.emse.fayol.maqit.simulator.configuration.SimProperties;
import fr.emse.fayol.maqit.simulator.environment.GridManagement;

public class Restaurant extends SimFactory {
    private int[] typeColor(int x) {
        int[] col;

        switch (x) {
            case 0:     // Ground
                col = new int[] {255, 255, 255};
                break;

            case 1:     // Wall
                col = new int[] {0, 0, 0};
                break;

            case 2:     // Table
                col = new int[] {128, 128, 128};
                break;

            case 3:     // Collect point
                col = new int[] {170, 48, 48};
                break;

            case 4:     // Doors
                col = new int[] {64, 150, 42};
                break;

            case 5:     // Customer
                col = new int[] {196, 32, 196};
                break;

            case 6:     // INS
                col = new int[] {12, 134, 222};
                break;

            default:
                col = new int[] {255, 0, 0};
                break;
        }

        return col;
    }
    public Restaurant(SimProperties sp, GridManagement env, List<String> file) {
        super(sp, env);     
    
        for (int y = 0; y < env.getColumns(); y++) {
            char[] cur = file.get(y).toCharArray();

            for (int x = 0; x < env.getRows(); x++) {
                char ch = cur[x];
                int val = ch - 48;

                env.addComponent(new int[] {x, y}, val, typeColor(val));
            }
        }
    }

    @Override
    public void createObstacle() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createObstacle'");
    }

    @Override
    public void createObstacle(int[] arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createObstacle'");
    }

    @Override
    public void createTurtlebot() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createTurtlebot'");
    }

    @Override
    public void createTurtlebot(int[] arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createTurtlebot'");
    }

    @Override
    public void schedule() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'schedule'");
    }
}
