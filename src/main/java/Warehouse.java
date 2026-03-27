package whorchestrator;

import whorchestrator.StagingArea;
import whorchestrator.Section;
import whorchestrator.SimulationClock;
import whorchestrator.StockerSelectionStrategy;
import whorchestrator.StockerRandomSelectionStrategy;
import whorchestrator.PickerSelectionStrategy;
import whorchestrator.PickerRandomSelectionStrategy;

import java.util.ArrayList;
import java.util.List;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import java.io.FileNotFoundException;
import com.github.cliftonlabs.json_simple.JsonException;
import java.io.FileReader;
import java.math.BigDecimal;

public class Warehouse {

    StagingArea staging_area;
    List<Section> sections;
    List<String> section_names = new ArrayList<String>();
    List<Stocker> stockers;
    List<Picker> pickers;
    String name;
    int boxes_per_delivery;
    double delivery_per_tick_probability;
    long tick_duration_ms;
    SimulationClock simulationClock;
    DeliveryGenerator deliveryGenerator;
    StockerSelectionStrategy stocker_selection_strategy;
    PickerSelectionStrategy picker_selection_strategy;

    public void start() {
        simulationClock.start();
        deliveryGenerator.start();
        for (Stocker stocker : stockers) {
            stocker.start(); // Not starting this yet as we need to clean up the threads when they die.
        } // So either the CLI might have some delay, or this can continue running and warehouse started can print BEFORE the last stocker prints its start line. So might need to take a look at this later.
        for (Picker picker : pickers) {
            picker.start();
        }
        System.out.println(String.format("Warehouse %s started.", this.name));
    }

    private Warehouse(String configuration_path, SimulationClock simulationClock) {
        this.simulationClock = simulationClock;
        System.out.println("Creating warehouse.");
        this.sections = new ArrayList<Section>();
        this.stockers = new ArrayList<Stocker>();
        this.pickers = new ArrayList<Picker>();
        this.stocker_selection_strategy = new StockerRandomSelectionStrategy(this.section_names, 10);
        this.picker_selection_strategy = new PickerRandomSelectionStrategy();
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
            this.stocker_selection_strategy = new StockerRandomSelectionStrategy(this.section_names, 10);
            InitStockers(json);
            InitDeliveryGenerator();
            InitPickers(json);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    private void InitStagingArea(JsonObject json) {
        JsonObject _staging_area = (JsonObject) json.get("staging_area");
        this.staging_area = new StagingArea( ((BigDecimal) _staging_area.get("starting_capacity")).intValueExact(), section_names);
    }

    private void InitSections(JsonObject json) {
        JsonArray sections_arr = (JsonArray) json.get("sections");
        for (Object section : sections_arr) {
            JsonObject sec = (JsonObject) section;
            String section_name = (String) sec.get("name");
            sections.add(
                new Section(
                    section_name,
                    ((BigDecimal) sec.get("starting_capacity")).intValueExact()
                )
            );
            section_names.add(section_name);
        }
    }

    private void InitStockers(JsonObject json) {
        int no_stockers = ((BigDecimal) json.get("stockers")).intValueExact();
        for(int i = 0; i < no_stockers; i++) {
            Stocker stocker = new Stocker(this.staging_area, this.stocker_selection_strategy, this.sections, this.simulationClock);
            stockers.add(stocker);
        };
    }

    private void InitPickers(JsonObject json) {
        Object pickersObj = json.get("pickers");
        int no_pickers = pickersObj != null
            ? ((BigDecimal) pickersObj).intValueExact()
            : 5; // Default to 5 pickers if not specified in config

        for(int i = 0; i < no_pickers; i++) {
            Picker picker = new Picker(
                this.sections,
                this.section_names,
                this.picker_selection_strategy,
                this.simulationClock,
                no_pickers
            );
            pickers.add(picker);
        }
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
        this.simulationClock = new SimulationClock(this.tick_duration_ms);
        this.deliveryGenerator = new DeliveryGenerator(
            this.staging_area,
            this.sections,
            this.boxes_per_delivery,
            this.delivery_per_tick_probability,
            this.simulationClock
        );
    }

    public static Warehouse fromConfigurationPath(String configuration_path, SimulationClock simulationClock) {
        return new Warehouse(configuration_path, simulationClock);
    }
}