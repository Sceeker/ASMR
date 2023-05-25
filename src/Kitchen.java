import java.util.ArrayList;
import java.util.Arrays;

class KitchenOrder {
    private int[] tableLocation;
    private Restaurant restaurant;
    private int count;

    public KitchenOrder(int[] tableLocation, Restaurant restaurant) {
        this.tableLocation = tableLocation;
        this.restaurant = restaurant;
        count = 0;
    }

    public int[] getLoc() {
        return tableLocation;
    }

    public void update() {
        count++;

        if (count % 25 == 0)
            askForBot();
    }
    
    private void askForBot() {
        ArrayList<Integer> trans = new ArrayList<Integer>();
        trans.add(tableLocation[0]);
        trans.add(tableLocation[1]);
        restaurant.getAir().radioTransmission(new RadioData(null, 3, trans));
    }
}

public class Kitchen {
    private Restaurant restaurant;
    ArrayList<KitchenOrder> orders;
    
    public Kitchen(Restaurant restaurant) {
        this.restaurant = restaurant;
        orders = new ArrayList<KitchenOrder>();
    }

    public void update() {
        for (KitchenOrder order: orders) {
            order.update();
        }
    }

    public void radioReception(RadioData dat) {
        int cmdId = dat.getCommandId();

        switch (cmdId) {
            case 2:     // Une commande est reçue
                orders.add(new KitchenOrder(new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)}, restaurant));
                break;

            case 5:
                KitchenOrder toRemove = null;
                for (KitchenOrder order: orders) {
                    if (Arrays.equals(order.getLoc(), new int[] {dat.getCommandData().get(0), dat.getCommandData().get(1)}))
                        toRemove = order;
                }
                orders.remove(toRemove);
                break;

            default :
                break;
        }
    }
}
