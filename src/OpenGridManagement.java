import fr.emse.fayol.maqit.simulator.environment.GridEnvironment;
import fr.emse.fayol.maqit.simulator.environment.GridManagement;

public class OpenGridManagement extends GridManagement {

    public OpenGridManagement(int seed, int rows, int columns, int debug) {
        super(seed, rows, columns, debug);
    }

    public OpenGridManagement(int seed, int rows, int columns, String title, int posx, int posy, int width, int height, int debug) {
        super(seed, rows, columns, title, posx, posy, width, height, debug);
    }

    public GridEnvironment getEnvironment() {
        return this.env;
    }
}