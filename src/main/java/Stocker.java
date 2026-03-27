package whorchestrator;

import java.util.concurrent.atomic.AtomicInteger;
import whorchestrator.StockerSelectionStrategy;
import whorchestrator.StagingArea;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


class Stocker extends Thread {

    private final SimulationClock simulation_clock;
    private Map<String, Integer> carrying = new HashMap<String, Integer>();
    private StagingArea staging_area;
    private List<String> section_names;
    private static AtomicInteger next_id = new AtomicInteger(0);
    private StockerSelectionStrategy selection_strategy; // This could potentially be static, but I'm not sure if potentially the optimal strategy COULD include multiple stockers running different strategies.
                                                         // I doubt it, but it could be an interesting thing to try out.
    private final int id;

    public void run() {
        System.out.println(String.format("Stocker ID: %d started.", id));
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Map<String, Integer> load = this.selection_strategy.TakeFromStagingArea(this.staging_area);
                setCarrying(load);
                processLoad();
                clearCarrying();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected stocker failure", e);
        }
    }

    public Stocker(StagingArea staging_area, StockerSelectionStrategy selection_strategy, List<String> section_names, SimulationClock simulation_clock) {
        this.id = next_id.getAndIncrement();
        this.simulation_clock = simulation_clock;
        this.selection_strategy = selection_strategy;
        this.staging_area = staging_area;
        this.section_names = section_names;
        for(String name : section_names) {
            carrying.put(name, 0);
        }
        System.out.println(String.format("Stocker ID: %d created.", id));
    }

    private void setCarrying(Map<String, Integer> load) {
        for (String section : section_names) {
            carrying.put(section, load.getOrDefault(section, 0));
        }
    }

    private void clearCarrying() {
        for (String section : section_names) {
            carrying.put(section, 0);
        }
    }

    private int totalCarrying() {
        int total = 0;
        for (int amount : carrying.values()) {
            total += amount;
        }
        return total;
    }

    private void processLoad() throws InterruptedException {
        List<String> visitOrder = getSectionsByDescendingLoad();

        String currentLocation = "staging";

        for (String sectionName : visitOrder) {
            int loadForSection = carrying.getOrDefault(sectionName, 0);
            if (loadForSection <= 0) continue;

            int remainingLoad = totalCarrying();
            int moveTicks = 10 + remainingLoad;

            simulation_clock.sleepTicks(moveTicks);

            System.out.println(
                "tick=" + simulation_clock.getCurrentTick()
                + " tid=S" + id
                + " event=move"
                + " from=" + currentLocation
                + " to=" + sectionName
                + " load=" + remainingLoad
            );

            simulation_clock.sleepTicks(loadForSection);

            System.out.println(
                "tick=" + simulation_clock.getCurrentTick()
                + " tid=S" + id
                + " event=stock_end"
                + " section=" + sectionName
                + " stocked=" + loadForSection
                + " remaining_load=" + (totalCarrying() - loadForSection)
            );

            carrying.put(sectionName, 0);
            currentLocation = sectionName;
        }

        if (!currentLocation.equals("staging")) {
            int remainingLoad = totalCarrying();
            simulation_clock.sleepTicks(10 + remainingLoad);

            System.out.println(
                "tick=" + simulation_clock.getCurrentTick()
                + " tid=S" + id
                + " event=move"
                + " from=" + currentLocation
                + " to=staging"
                + " load=" + remainingLoad
            );
        }
    }

    private List<String> getSectionsByDescendingLoad() {
        List<String> ordered = new ArrayList<>(section_names);
        ordered.removeIf(name -> carrying.getOrDefault(name, 0) <= 0);
        ordered.sort((a, b) -> Integer.compare(
            carrying.getOrDefault(b, 0),
            carrying.getOrDefault(a, 0)
        ));
        return ordered;
    }
}