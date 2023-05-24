import java.util.concurrent.Executors;

public class Airwaves {
    private Restaurant restaurant;

    public Airwaves(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void radioTransmission(RadioData dat) {
        for (INS bot : restaurant.getBots()) {
            if (dat.getOrigin() != bot)
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        bot.radioReception(dat);
                    }});
        }

        for (Table table: restaurant.getTables()) {
            table.radioReception(dat);
        }

        restaurant.getKitchen().radioReception(dat);

        System.out.println("[RADIO] Command " + dat.getCommandId());
    }
}