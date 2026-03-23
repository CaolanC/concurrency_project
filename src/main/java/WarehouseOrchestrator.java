import whorchestrator.Warehouse;

public class WarehouseOrchestrator {
    public static void main(String[] args) {
        Warehouse warehouse1 = Warehouse.fromConfigurationPath("Warehouse1.json");
        warehouse1.start();
    }
}
