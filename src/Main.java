import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

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

        Path path = Paths.get("restaurant.txt");
        List<String> file = Files.readAllLines(path);

        int width = Integer.parseInt(file.remove(0));
        int height = Integer.parseInt(file.remove(0));

        Random rng = new Random();
        OpenGridManagement env = new OpenGridManagement(rng.nextInt(), height, width, sp.display_title, sp.display_x, sp.display_y, height * 32,  width * 32, 0);

        Restaurant asmr = new Restaurant(sp, env, file, 200);

        asmr.schedule();
    }
}
