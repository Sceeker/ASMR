import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

class CookingTimerTask extends TimerTask  {
    int[] tableLocation;
    private Restaurant restaurant;

    public CookingTimerTask(int[] tableLocation, Restaurant restaurant) {
        this.tableLocation = tableLocation;
        this.restaurant = restaurant;
    }

    @Override
    public void run() {
        ArrayList<Integer> trans = new ArrayList<Integer>();
        trans.add(tableLocation[0]);
        trans.add(tableLocation[1]);
        restaurant.getAir().radioTransmission(new RadioData(null, 3, trans));
    }
}

public class Kitchen {
    private Restaurant restaurant;
    
    public Kitchen(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            case 2:     // Une commande est reçue
                CookingTimerTask task = new CookingTimerTask(new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)}, restaurant);
                Timer timer = new Timer();
                timer.schedule(task, 4000);
                break;

            default :
                break;
        }
    }
}
