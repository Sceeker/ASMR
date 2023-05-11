import java.util.List;
import java.util.ArrayList;

public class Airwaves {
    private List<INS> bots;

    public Airwaves(List<INS> bots) {
        this.bots = bots;
    }

    public Airwaves() {
        this(new ArrayList<INS>());   
    }

    public void addINS(INS bot) {
        bots.add(bot);
    }

    public void removeINS(INS bot) {
        bots.remove(bot);
    }

    public void radioTransmission(RadioData dat) {
        for (INS bot : bots) {
            if (dat.getOrigin() != bot)
                bot.radioReception(dat);
        }
    }
}