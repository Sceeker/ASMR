import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import fr.emse.fayol.maqit.simulator.configuration.IniFile;
import fr.emse.fayol.maqit.simulator.configuration.SimProperties;
import fr.emse.fayol.maqit.simulator.environment.GridManagement;

public class Main {
    public static void main(String[] args) throws IOException{
		IniFile configFile;
		
		try {
			configFile = new IniFile(args[0]);
		} catch (Exception e) {
			System.out.print(e.getMessage());
			return;
		}
		
		SimProperties sp = new SimProperties(configFile);

        Path path = Paths.get(args[1]);
        List<String> file = Files.readAllLines(path);

        int width = Integer.parseInt(file.remove(0));
        int height = Integer.parseInt(file.remove(0));

        Random rng = new Random();
        GridManagement env = new GridManagement(rng.nextInt(), width, height, "ASMR : Automatic Service Miaou Restaurant", 0, 0, width * 24, height * 24, 0);

        Restaurant asmr = new Restaurant(sp, env, file);
    }
}
