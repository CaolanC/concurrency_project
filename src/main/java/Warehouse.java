package whorchestrator;

import whorchestrator.StagingArea;
import java.util.ArrayList;
import java.util.List;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.io.FileNotFoundException;
import com.github.cliftonlabs.json_simple.JsonException;
import java.io.FileReader;
import java.util.concurrent.atomic.AtomicInteger;
import java.math.BigDecimal;

class Stocker extends Thread {

    static AtomicInteger next_id = new AtomicInteger(0);
    private final int id;
    public void run() {
        System.out.println(String.format("Stocker ID: %d started.", id));
    }

    public Stocker() {
         this.id = next_id.getAndIncrement();
         System.out.println(String.format("Stocker ID: %d created.", id));
    }
}

public class Warehouse {

    StagingArea staging_area;
    List<String> sections;
    List<Stocker> stockers;

    private Warehouse(String configuration_path) {
        System.out.println("Creating warehouse.");
        sections = new ArrayList<String>();
        stockers = new ArrayList<Stocker>();
        ProcessConfig(configuration_path);
    }

    private void ProcessConfig(String path) {
        try (FileReader reader = new FileReader(path)) {
            JsonObject obj = (JsonObject) Jsoner.deserialize(reader);
            int no_stockers = ((BigDecimal) obj.get("stockers")).intValueExact();
            for(int i = 0; i < no_stockers; i++) {
                Stocker s = new Stocker();
                stockers.add(s);
            };
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    public static Warehouse fromConfigurationPath(String configuration_path) {
        return new Warehouse(configuration_path);
    }
}