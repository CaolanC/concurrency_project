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
    int boxes_per_delivery;
    double delivery_per_tick_probability;
    long tick_duration_ms;
    DeliveryGenerator deliveryGenerator;

    public void start() {
        // Start delivery flow first so stockers can consume from a live staging area.
        deliveryGenerator.start();
        for (Stocker stocker : stockers) {
//             stocker.start(); // Not starting this yet as we need to clean up the threads when they die.
        } // So either the CLI might have some delay, or this can continue running and warehouse started can print BEFORE the last stocker prints its start line. So might need to take a look at this later.
        System.out.println(String.format("Warehouse %s started.", this.name));
    }

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
            InitDeliveryConfig(json);
            InitStockers(json);
            InitDeliveryGenerator();

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
            sections.add(
                new Section(
                    (String) sec.get("name"),
                    ((BigDecimal) sec.get("starting_capacity")).intValueExact()
                )
            );
        }
    }

    private void InitStockers(JsonObject json) {
        int no_stockers = ((BigDecimal) json.get("stockers")).intValueExact();
        for(int i = 0; i < no_stockers; i++) {
            Stocker s = new Stocker();
            stockers.add(s);
        };
    }

    private void InitDeliveryConfig(JsonObject json) {
        this.boxes_per_delivery = ((BigDecimal) json.get("boxes_per_delivery")).intValueExact();
        this.delivery_per_tick_probability = ((BigDecimal) json.get("delivery_per_tick_probability")).doubleValue();
        BigDecimal tickDurationFromConfig = (BigDecimal) json.get("tick_time_ms");
        // min tick duration of 50ms to give stockers a chance to consume deliveries.
        this.tick_duration_ms = tickDurationFromConfig == null
            ? 100L
            : Math.max(50L, tickDurationFromConfig.longValue());
    }

    private void InitDeliveryGenerator() {
        this.deliveryGenerator = new DeliveryGenerator(
            this.staging_area,
            this.sections,
            this.boxes_per_delivery,
            this.delivery_per_tick_probability,
            this.tick_duration_ms
        );
    }

    public static Warehouse fromConfigurationPath(String configuration_path) {
        return new Warehouse(configuration_path);
    }
}