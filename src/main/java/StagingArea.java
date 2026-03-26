package whorchestrator;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

//import whorchestrator.StagingArea;
public class StagingArea {
    private boolean active;
    private int capacity;
    private int max_capacity; // <- Need to handle for integer limits regardless of whether implementing max_capacity. Wait this is Java.
    private Map<String, Integer> capacity_per_box = new HashMap<String, Integer>(); // TODO: HashMap is not thread safe, we need to account for this.

    StagingArea(int starting_capacity, List<String> section_names) {
        this.capacity = starting_capacity;
        for (String name : section_names) {
            capacity_per_box.put(name, 0); // TODO: Add some math here, because we have a starting_capacity but we don't account for that when initing the box counts to 0.
        }

        System.out.println(String.format("Staging Area (capacity %d) created.", this.capacity));
    }

    public void toggle_active() {
        active = !active;
    }

    public boolean get_status() {
        return active;
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    // Synchronized so concurrent delivery threads cannot race when updating capacity.
    // Output uses key=value pairs for consistent log parsing.
    public synchronized void addDelivery(Map<String, Integer> delivery, long tick, String threadId) {
        int totalBoxes = 0;
        StringBuilder details = new StringBuilder();
        for (Map.Entry<String, Integer> entry : delivery.entrySet()) {
            totalBoxes += entry.getValue();
            capacity_per_box.merge(entry.getKey(), entry.getValue(), Integer::sum);
            details.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
        }
        this.capacity += totalBoxes;
        System.out.println(
            "tick=" + tick
            + " tid=" + threadId
            + " event=delivery_arrived"
            + " total_boxes=" + totalBoxes
            + " staging_capacity=" + this.capacity
            + details
            + capacity_per_box
        );
    }
}