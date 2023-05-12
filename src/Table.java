import java.util.List;

public class Table {
    
    public void WantToOrder (List<Integer> commandData) {

        RadioData RadioData = new RadioData(null, 0, commandData);
        Main.air.radioTransmission(RadioData);

    }

}
