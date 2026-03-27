package whorchestrator;

import java.util.concurrent.atomic.AtomicInteger;
import whorchestrator.StockerSelectionStrategy;
import whorchestrator.StagingArea;
import whorchestrator.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


class Stocker extends Thread {

    private final SimulationClock simulation_clock;
    private Map<String, Integer> carrying = new HashMap<String, Integer>();
    private StagingArea staging_area;
    private List<String> section_names = new ArrayList<String>();
    private List<Section> sections;
    private static AtomicInteger next_id = new AtomicInteger(0);
    private StockerSelectionStrategy selection_strategy; // This could potentially be static, but I'm not sure if potentially the optimal strategy COULD include multiple stockers running different strategies.
                                                         // I doubt it, but it could be an interesting thing to try out.
    private final int id;

    public void run() {
        System.out.println(String.format("Stocker ID: %d started.", id));
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (totalCarrying() == 0) {
                    Map<String, Integer> load = this.selection_strategy.TakeFromStagingArea(this.staging_area);
                    setCarrying(load);

                    // Strategy found nothing useful to take right now.
                    if (totalCarrying() == 0) {
                        simulation_clock.sleepTicks(1);
                        continue;
                    }
                }

                processLoad();

                if (totalCarrying() > 0) {
                    simulation_clock.sleepTicks(1);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected stocker failure", e);
        }
    }

    public Stocker(StagingArea staging_area, StockerSelectionStrategy selection_strategy, List<Section> sections, SimulationClock simulation_clock) {
        this.id = next_id.getAndIncrement();
        this.simulation_clock = simulation_clock;
        this.selection_strategy = selection_strategy;
        this.staging_area = staging_area;
        this.sections = sections;
        for(Section s : sections) {
            this.section_names.add(s.name);
        }
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
    String currentLocation = "staging";

    while (totalCarrying() > 0) {
        List<String> visitOrder = getSectionsByDescendingLoad();
        boolean madeProgressThisPass = false;

        for (String sectionName : visitOrder) {
            int loadForSection = carrying.getOrDefault(sectionName, 0);
            if (loadForSection <= 0) {
                continue;
            }

            int remainingLoadBeforeMove = totalCarrying();

            // If coming from staging, use load for THIS section
            int moveTicks;
            if (currentLocation.equals("staging")) {
                moveTicks = 10 + loadForSection;
            } else {
                moveTicks = 10 + remainingLoadBeforeMove;
            }
            simulation_clock.sleepTicks(moveTicks);

            System.out.println(
                "tick=" + simulation_clock.getCurrentTick()
                + " tid=S" + id
                + " event=move"
                + " from=" + currentLocation
                + " to=" + sectionName
                + " load=" + remainingLoadBeforeMove
            );

            Section section = findSectionByName(sectionName);

            int stocked = 0;
            section.lockForStocking();
            try {
                System.out.println(
                    "tick=" + simulation_clock.getCurrentTick()
                    + " tid=S" + id
                    + " event=stock_begin"
                    + " section=" + sectionName
                    + " amount=" + loadForSection
                );

                stocked = section.addBoxes(loadForSection);

                if (stocked > 0) {
                    simulation_clock.sleepTicks(stocked);
                    madeProgressThisPass = true;
                }

                carrying.put(sectionName, loadForSection - stocked);

                System.out.println(
                    "tick=" + simulation_clock.getCurrentTick()
                    + " tid=S" + id
                    + " event=stock_end"
                    + " section=" + sectionName
                    + " stocked=" + stocked
                    + " remaining_load=" + totalCarrying()
                );
            } finally {

                section.unlockForStocking();
            }

            currentLocation = sectionName;
        }

        if (!madeProgressThisPass) {
            break;
        }
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

    private Section findSectionByName(String name) {
        for (Section section : sections) {
            if (section.name.equals(name)) {
                return section;
            }
        }
        throw new IllegalArgumentException("Section not found: " + name);
    }
}