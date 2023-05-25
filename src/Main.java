import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import fr.emse.fayol.maqit.simulator.configuration.IniFile;
import fr.emse.fayol.maqit.simulator.configuration.SimProperties;
import fr.emse.fayol.maqit.simulator.environment.ColorCell;

public class Main {
    public static void main(String[] args) throws IOException{
		IniFile configFile;
		
		try {
			configFile = new IniFile("configuration.ini");
		} catch (Exception e) {
			System.out.print(e.getMessage());
			return;
		}
		
		SimProperties sp = new SimProperties(configFile);

        sp.simulationParams();

        if (sp.display == 1) {
            sp.displayParams();
            ColorCell.defaultcolor = new int[3];   
            ColorCell.defaultcolor[0] = sp.colorunknown.getRed();
            ColorCell.defaultcolor[1] = sp.colorunknown.getGreen();
            ColorCell.defaultcolor[2] = sp.colorunknown.getBlue();
        }

        // Permet de faire tourner beaucoup de simulation à grande cadence afin de moyenner les résultats et avoir quelque chose de plus potable
        boolean multipleRuns = false;        // ATTENTION, à cause de comment est fait la biblio TurtleBot, cela empêche la visualisation graphique de la simulation. Pour voir, mettre à false

        Path path = Paths.get("restaurant.txt");
        List<String> file = Files.readAllLines(path);

        int width = Integer.parseInt(file.remove(0));
        int height = Integer.parseInt(file.remove(0));

        Random rng = new Random();

        if (multipleRuns) {
            int nbRuns = 10;        // Pour changer le nombre de simulations à faire

            OpenGridManagement env = new OpenGridManagement(rng.nextInt(), height, width, sp.display_title, sp.display_x, sp.display_y, height * 32,  width * 32, 0);
            int avg = 0;
            int last = 100;
            int cur = 0;

            for (int i = 0; i < nbRuns * 2; i ++) {
                env.clearEnv();
                
                if (i == nbRuns) {
                    path = Paths.get("segregatedRestaurant.txt");
                    file = Files.readAllLines(path);

                    width = Integer.parseInt(file.remove(0));
                    height = Integer.parseInt(file.remove(0));

                    avg /= nbRuns;

                    System.out.println("Nombre de pas moyen d'attente en mode dynamique: " + avg);

                    avg = 0;
                }
                
                Restaurant asmr = new Restaurant(sp, env, file, 10, 1000);

                try {
                    cur = asmr.run();
                } catch (Exception e) {
                    System.out.println("ERREUR -> Réutilisation de la simulation précédente pour la moyenne.");

                    cur = last;
                }

                if (asmr.getError()) {
                    System.out.println("ERREUR -> Réutilisation de la simulation précédente pour la moyenne.");

                    cur = last;
                }

                avg += cur;

                last = cur;
            }

            avg /= nbRuns;

            System.out.println("Nombre de pas moyen d'attente en mode zones: " + avg);
        } else {
            {
                OpenGridManagement env = new OpenGridManagement(rng.nextInt(), height, width, sp.display_title, sp.display_x, sp.display_y, height * 32,  width * 32, 0);

                Restaurant asmr = new Restaurant(sp, env, file, 100, 1000);

                System.out.println("Nombre de pas moyen d'attente en mode dynamique: " + asmr.run());
            }

            path = Paths.get("segregatedRestaurant.txt");
            file = Files.readAllLines(path);

            width = Integer.parseInt(file.remove(0));
            height = Integer.parseInt(file.remove(0));

            {
                OpenGridManagement env = new OpenGridManagement(rng.nextInt(), height, width, sp.display_title, sp.display_x, sp.display_y, height * 32,  width * 32, 0);

                Restaurant asmr = new Restaurant(sp, env, file, 100, 1000);

                System.out.println("Nombre de pas moyen d'attente en mode zones: " + asmr.run());
            }
        }

        try {
            TimeUnit.MILLISECONDS.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }
}
