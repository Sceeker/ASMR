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
                        try {
                            bot.radioReception(dat);
                        } catch (Exception e) {
                            restaurant.threadError();
                        }
                    }});
        }

        for (Table table: restaurant.getTables()) {
            table.radioReception(dat);
        }

        restaurant.getKitchen().radioReception(dat);

        if (((OpenGridManagement) restaurant.getEnv()).debugLevel() > 0)
                System.out.println("[RADIO] Command " + dat.getCommandId());
    }
}