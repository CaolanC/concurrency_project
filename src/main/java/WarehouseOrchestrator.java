import whorchestrator.Warehouse;
import whorchestrator.SimulationClock;

public class WarehouseOrchestrator {
    public static void main(String[] args) {
        SimulationClock simulationClock = new SimulationClock(100L);
        Warehouse warehouse1 = Warehouse.fromConfigurationPath("Warehouse1.json", simulationClock);
        warehouse1.start();
    }
}
