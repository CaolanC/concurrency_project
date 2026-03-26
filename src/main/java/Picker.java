package whorchestrator;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;

/**
 * Picker thread whose picks boxes from warehouse sections.
 * Each picker try to pick a box from a random selected section.
 * Pick attempts occur at more or less 100 per day across all pickers combined.
 */
class Picker extends Thread {
    
    private static AtomicInteger next_id = new AtomicInteger(0);
    private static AtomicInteger pick_counter = new AtomicInteger(0);
    
    private final int id;
    private final List<Section> sections;
    private final List<String> section_names;
    private final PickerSelectionStrategy selection_strategy;
    private final SimulationClock simulation_clock;
    
    // Configuration for pick rate: approximately 100 picks per day (1000 ticks)
    // So roughly 1 pick per 10 ticks. This will be adjusted based on number of pickers.
    private final double pick_probability;

    public Picker(
        List<Section> sections,
        List<String> section_names,
        PickerSelectionStrategy selection_strategy,
        SimulationClock simulation_clock,
        int total_pickers
    ) {
        this.id = next_id.getAndIncrement();
        this.sections = sections;
        this.section_names = section_names;
        this.selection_strategy = selection_strategy;
        this.simulation_clock = simulation_clock;
        // For around 100 picks per day across all pickers: (100 / 1000 ticks) / num_pickers
        this.pick_probability = 0.1 / total_pickers;
        setName("Picker-" + id);
        System.out.println(String.format("Picker ID: %d created.", id));
    }

    @Override
    public void run() {
        System.out.println(String.format("Picker ID: %d started.", id));
        java.util.Random random = new java.util.Random();
        
        try {
            while (!Thread.currentThread().isInterrupted()) {
                simulation_clock.sleepTicks(1);
                
                // Probabilistic pick attempt
                if (random.nextDouble() < pick_probability) {
                    attemptPick();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void attemptPick() {
        long pick_id = pick_counter.getAndIncrement();
        long pick_start_tick = simulation_clock.getCurrentTick();
        
        // Select section randomly (cannot be changed during the attempt)
        String selected_section = selection_strategy.selectSection(section_names);
        Section section = findSectionByName(selected_section);
        
        // Log pick_start event
        System.out.println(
            "tick=" + pick_start_tick
            + " tid=P" + id
            + " event=pick_start"
            + " pick_id=" + pick_id
            + " section=" + selected_section
        );
        
        long wait_start = pick_start_tick;
        
        // Try to pick from the section (this may require waiting if section is 0)
        boolean picked = section.pickBox(id, pick_id, selected_section, simulation_clock);
        
        long pick_end_tick = simulation_clock.getCurrentTick();
        long waited_ticks = pick_end_tick - wait_start;
        
        if (picked) {
            // Log pick_done event
            System.out.println(
                "tick=" + pick_end_tick
                + " tid=P" + id
                + " event=pick_done"
                + " pick_id=" + pick_id
                + " section=" + selected_section
                + " waited_ticks=" + waited_ticks
            );
        }
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
