package whorchestrator;

import java.util.concurrent.atomic.AtomicInteger;
import whorchestrator.StockerSelectionStrategy;
import whorchestrator.StagingArea;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

class Stocker extends Thread {

    private Map<String, Integer> carrying = new HashMap<String, Integer>();
    private StagingArea staging_area;
    private List<String> section_names;
    private static AtomicInteger next_id = new AtomicInteger(0);
    private StockerSelectionStrategy selection_strategy; // This could potentially be static, but I'm not sure if potentially the optimal strategy COULD include multiple stockers running different strategies.
                                                         // I doubt it, but it could be an interesting thing to try out.
    private final int id;

    public void run() { // TODO: Add cleanup for shutdown/termination
        System.out.println(String.format("Stocker ID: %d started.", id));
        while (!Thread.currentThread().isInterrupted()) {
            this.selection_strategy.TakeFromStagingArea(this.staging_area);
        }
    }

    public Stocker(StagingArea staging_area, StockerSelectionStrategy selection_strategy, List<String> section_names) {
        this.id = next_id.getAndIncrement();
        this.selection_strategy = selection_strategy;
        this.staging_area = staging_area;
        this.section_names = section_names;
        for(String name : section_names) {
            carrying.put(name, 0);
        }
        System.out.println(String.format("Stocker ID: %d created.", id));
    }
}