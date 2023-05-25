import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import fr.emse.fayol.maqit.simulator.SimFactory;
import fr.emse.fayol.maqit.simulator.configuration.SimProperties;
import fr.emse.fayol.maqit.simulator.robot.GridTurtlebot;

public class Restaurant extends SimFactory {
    private Airwaves air;
    private List<String> file;
    private int height, width;
    private int timeStep;
    private Kitchen kitchen;
    private ArrayList<Customer> customers;
    private ArrayList<Table> tables;
    private ArrayList<INS> bots;
    private ArrayList<int[]> collectPoints;
    private ArrayList<int[]> doors;

    private int indicator;
    private int cumCustomer;

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
                col = new int[] {200, 200, 200};
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

            case 7:     // Taken table
                col = new int[] {150, 150, 150};
                break;

            case 8:     // Table wanting to order
                col = new int[] {100, 100, 100};
                break;

            case 9:     // Table waiting for its order
                col = new int[] {50, 50, 50};
                break;


            default:    // Error cell
                col = new int[] {255, 0, 0};
                break;
        }

        return col;
    }

    public Restaurant(SimProperties sp, OpenGridManagement env, List<String> file, int timeStep) {
        super(sp, env);     

        this.file = file;
        this.timeStep = timeStep;

        customers = new ArrayList<Customer>();
        tables = new ArrayList<Table>();
        air = new Airwaves(this);
        bots = new ArrayList<INS>();
        kitchen = new Kitchen(this);

        indicator = 0;
        cumCustomer = 0;

        createTurtlebot();
        createObstacle();
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

    public ArrayList<INS> getBots() {
        return bots;
    }

    public Kitchen getKitchen() {
        return kitchen;
    }

    public ArrayList<int[]> getCollectPoints() {
        return collectPoints;
    }

    public ArrayList<int[]> getDoors() {
        return doors;
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public int getTimeStep() {
        return timeStep;
    }

    private ArrayList<int[]> createAccessors(ArrayList<int[]> lst) {
        ArrayList<int[]> res = new ArrayList<int[]>();

        for (int[] el: lst) {
            PathFinding solver = new PathFinding(this);
            int[] loc = solver.freeNeighboringCells(el).get(0).getCoords();
            res.add(loc);
        }

        return res;
    }

    @Override
    public void createObstacle() {
        ArrayList<int[]> fcps = new ArrayList<int[]>();
        ArrayList<int[]> fd = new ArrayList<int[]>();

        for (int y = 0; y < environment.getRows(); y++) {
            char[] cur = this.file.get(y).toCharArray();

            for (int x = 0; x < environment.getColumns(); x++) {
                char ch = cur[x];
                int val = ch - 48;

                if (val != 6) {
                    environment.addComponent(new int[] {y, x}, val, typeColor(val));

                    if (val == 2)
                        tables.add(new Table(new int[] {y, x}, this));

                    if (val == 3) 
                        fcps.add(new int[] {y, x});

                    if (val == 4)
                        fd.add(new int[] {y, x});
                }
            }
        }

        doors = createAccessors(fd);
        collectPoints = createAccessors(fcps);
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
                    
                    bots.add(new INS(0, "INS " + String.valueOf(0), 2, 0, new int[] {y, x}, height, width, this));
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
                ArrayList<Customer> toRemove = new ArrayList<Customer>();
                for (Customer customer: customers) {
                    if (customer.move())
                        toRemove.add(customer);
                }

                for (Customer idx: toRemove) {
                    customers.remove(idx);
                }
            }
            if (i % 40 == 0) {
                Random rng = new Random();
                int[] entry = doors.get(rng.nextInt(doors.size()));
                customers.add(new Customer(entry, this));
                environment.addComponent(entry, 5, typeColor(5));

                cumCustomer++;
            }

            for (GridTurtlebot bot: bots) {
                bot.move(0);
            }

            for (Table table : tables) {
                table.update();

                if (table.getTableState() == TableState.waitingToOrder || table.getTableState() == TableState.waitingForOrder)
                    indicator++;
            }

            kitchen.update();

            System.out.println("Step " + i);
            try {
                TimeUnit.MILLISECONDS.sleep(timeStep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("===========================");
        System.out.println("====SIMULATION TERMINEE====");
        String tmp = "====INDICATEUR: " + indicator / cumCustomer;
        tmp += new String(new char[27 - tmp.length()]).replace("\0", "=");
        System.out.println(tmp);
        System.out.println("===========================");
        System.exit(0);
    }
}
