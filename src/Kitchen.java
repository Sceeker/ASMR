import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

class CookingTimerTask extends TimerTask  {
    private int[] tableLocation;
    private Restaurant restaurant;

    public CookingTimerTask(int[] tableLocation, Restaurant restaurant) {
        this.tableLocation = tableLocation;
        this.restaurant = restaurant;
    }

    public int[] getLoc() {
        return tableLocation;
    }

    @Override
    public void run() {
        ArrayList<Integer> trans = new ArrayList<Integer>();
        trans.add(tableLocation[0]);
        trans.add(tableLocation[1]);
        restaurant.getAir().radioTransmission(new RadioData(null, 3, trans));
    }
}

class OrderWrapper {
    public Timer timer;
    public CookingTimerTask task;

    public OrderWrapper(Timer timer, CookingTimerTask task) {
        this.timer = timer;
        this.task = task;
    }
}

public class Kitchen {
    private Restaurant restaurant;
    ArrayList<OrderWrapper> orders;
    
    public Kitchen(Restaurant restaurant) {
        this.restaurant = restaurant;
        orders = new ArrayList<OrderWrapper>();
    }

    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            case 2:     // Une commande est reçue
                Timer timer = new Timer();
                CookingTimerTask torder = new CookingTimerTask(new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)}, restaurant);
                timer.schedule(torder, restaurant.getTimeStep() * 20, restaurant.getTimeStep() * 10);
                orders.add(new OrderWrapper(timer, torder));
                break;

            case 5:
                OrderWrapper toRemove = null;
                for (OrderWrapper order: orders) {
                    if (Arrays.equals(order.task.getLoc(), new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)})) {
                        order.timer.cancel();
                        order.timer.purge();
                        toRemove = order;
                    }
                }
                orders.remove(toRemove);
                break;

            default :
                break;
        }
    }
}
