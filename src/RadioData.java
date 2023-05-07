import java.util.List;

public class RadioData {
    private INS origin;
    private int commandId;
    private List<Integer> commandData;

    public RadioData(INS bot, int commandId, List<Integer> commandData) {
        origin = bot;
        this.commandId = commandId;
        this.commandData = commandData;
    }

    public INS getOrigin() {
        return origin;
    }

    public int getCommandId() {
        return commandId;
    }

    public List<Integer> getCommandData() {
        return commandData;
    }
}