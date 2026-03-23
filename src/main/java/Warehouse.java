package whorchestrator;

import whorchestrator.StagingArea;
import whorchestrator.Section;
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

    private static AtomicInteger next_id = new AtomicInteger(0);
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
    List<Section> sections;
    List<Stocker> stockers;
    String name;

    private Warehouse(String configuration_path) {
        System.out.println("Creating warehouse.");
        sections = new ArrayList<Section>();
        stockers = new ArrayList<Stocker>();
        ProcessConfig(configuration_path);
        System.out.println(String.format("Warehouse: %s created.", this.name));
    }

    private void ProcessConfig(String path) {
        try (FileReader reader = new FileReader(path)) {
            JsonObject json = (JsonObject) Jsoner.deserialize(reader);
            this.name = (String) json.get("name");

            InitStagingArea(json);
            InitSections(json);
            InitStockers(json);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    private void InitStagingArea(JsonObject json) {
        JsonObject _staging_area = (JsonObject) json.get("staging_area");
        this.staging_area = new StagingArea( ((BigDecimal) _staging_area.get("starting_capacity")).intValueExact());
    }

    private void InitSections(JsonObject json) {
        JsonArray sections_arr = (JsonArray) json.get("sections");
        for (Object section : sections_arr) {
            JsonObject sec = (JsonObject) section;
            sections.add(new Section((String) sec.get("name")));
        }
    }

    private void InitStockers(JsonObject json) {
        int no_stockers = ((BigDecimal) json.get("stockers")).intValueExact();
        for(int i = 0; i < no_stockers; i++) {
            Stocker s = new Stocker();
            stockers.add(s);
        };
    }

    public static Warehouse fromConfigurationPath(String configuration_path) {
        return new Warehouse(configuration_path);
    }
}