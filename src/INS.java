import fr.emse.fayol.maqit.simulator.robot.GridTurtlebot;

public class INS extends GridTurtlebot {

    public INS(int id, String name, int field, int debug, int[] pos, int r, int c) {
        super(id, name, field, debug, pos, r, c);
        //TODO Auto-generated constructor stub
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
