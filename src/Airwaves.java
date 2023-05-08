import java.util.List;

public class Airwaves {
    List<INS> bots;

    public Airwaves(List<INS> bots) {
        this.bots = bots;
    }

    public void radioTransmission(RadioData dat) {
        for (INS bot : bots)
            bot.radioReception(dat);
    }
}