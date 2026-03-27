import whorchestrator.Warehouse;
import whorchestrator.SimulationClock;

public class WarehouseOrchestrator {
    public static void main(String[] args) {
        String configurationPath = "Warehouse1.json";
        long tickDurationMs = Warehouse.getTickDurationMsFromConfigurationPath(configurationPath);
        SimulationClock simulationClock = new SimulationClock(tickDurationMs);
        Warehouse warehouse1 = Warehouse.fromConfigurationPath(configurationPath, simulationClock);
        warehouse1.start();
    }
}
