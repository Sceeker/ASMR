public class Airwaves {
    private Restaurant restaurant;

    public Airwaves(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void radioTransmission(RadioData dat) {
        for (INS bot : restaurant.getBots()) {
            if (dat.getOrigin() != bot)
                bot.radioReception(dat);
        }
        
        System.out.println("[RADIO] Command " + dat.getCommandId());
    }
}