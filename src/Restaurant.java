import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.emse.fayol.maqit.simulator.SimFactory;
import fr.emse.fayol.maqit.simulator.configuration.SimProperties;
import fr.emse.fayol.maqit.simulator.robot.GridTurtlebot;

public class Restaurant extends SimFactory {
    private Airwaves air;
    private List<String> file;
    private int height, width;
    private ArrayList<Customer> customers;
    private ArrayList<Table> tables;

    public int[] typeColor(int x) {
        int[] col;

        switch (x) {
            case 0:     // Ground
                col = new int[] {255, 255, 255};
                break;

            case 1:     // Wall
                col = new int[] {0, 0, 0};
                break;

            case 2:     // Table
                col = new int[] {128, 128, 128};
                break;

            case 3:     // Collect point
                col = new int[] {170, 48, 48};
                break;

            case 4:     // Doors
                col = new int[] {64, 150, 42};
                break;

            case 5:     // Customer
                col = new int[] {196, 32, 196};
                break;

            case 6:     // INS
                col = new int[] {12, 134, 222};
                break;

            default:    // Error cell
                col = new int[] {255, 0, 0};
                break;
        }

        return col;
    }

    public Restaurant(SimProperties sp, OpenGridManagement env, List<String> file) {
        super(sp, env);     

        this.file = file;

        customers = new ArrayList<Customer>();
        tables = new ArrayList<Table>();
        air = new Airwaves();

        createObstacle();
        createTurtlebot();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return height;
    }

    public OpenGridManagement getEnv() {
        return (OpenGridManagement) environment;
    }

    public Airwaves getAir() {
        return air;
    }

    public ArrayList<Table> getTables() {
        return tables;
    }

    @Override
    public void createObstacle() {
        for (int y = 0; y < environment.getRows(); y++) {
            char[] cur = this.file.get(y).toCharArray();

            for (int x = 0; x < environment.getColumns(); x++) {
                char ch = cur[x];
                int val = ch - 48;

                if (val != 6) {
                    environment.addComponent(new int[] {y, x}, val, typeColor(val));

                    if (val == 2)
                        tables.add(new Table(new int[] {y, x}, this));
                }
            }
        }
    }

    @Override
    public void createObstacle(int[] arg0) {
        createObstacle();
    }

    @Override
    public void createTurtlebot() {
        for (int y = 0; y < this.environment.getRows(); y++) {
            char[] cur = this.file.get(y).toCharArray();

            for (int x = 0; x < this.environment.getColumns(); x++) {
                char ch = cur[x];
                int val = ch - 48;

                if (val == 6) {
                    this.environment.addComponent(new int[] {y, x}, val, typeColor(val));
                    
                    air.addINS(new INS(0, "INS " + String.valueOf(0), 2, 0, new int[] {y, x}, height, width, this));
                }
            }
        }
    }

    @Override
    public void createTurtlebot(int[] arg0) {
        createTurtlebot();
    }

    @Override
    public void schedule() {
        for (int i = 0; i < 1000; i++) {
            

            if (! customers.isEmpty()) {
                ArrayList<Integer> toRemove = new ArrayList<Integer>();
                for (Customer customer: customers) {
                    if (customer.move())
                        toRemove.add(customers.indexOf(customer));
                }

                for (int idx: toRemove) {
                    customers.remove(idx);
                }
            } else if (i % 100 == 0) {
                customers.add(new Customer(new int[] {9, 3}, this));
                environment.addComponent(new int[] {9, 3}, 5, typeColor(5));
            }

            for (GridTurtlebot bot: air.getBots()) {
                bot.move(0);
            }
    
            System.out.println("Step " + i);
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
