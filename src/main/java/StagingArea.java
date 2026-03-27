package whorchestrator;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

//import whorchestrator.StagingArea;
public class StagingArea {
    private boolean active;
    private int current_boxes;
    private int max_capacity; // <- Need to handle for integer limits regardless of whether implementing max_capacity. Wait this is Java.
    private Map<String, Integer> stock = new HashMap<String, Integer>(); // TODO: HashMap is not thread safe, we need to account for this.

    StagingArea(int starting_capacity, List<String> section_names) {
        this.current_boxes = 0; // TODO: Fix this so that the number of starting boxes is handled by the config, not hardcoded.
        for (String name : section_names) {
            stock.put(name, 0); // TODO: Add some math here, because we have a starting_capacity but we don't account for that when initing the box counts to 0.
        }

        System.out.println(String.format("Staging Area (capacity %d) created.", this.current_boxes));
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

public synchronized Map<String, Integer> TakeBoxes(int no_boxes) throws InterruptedException {
    while (current_boxes == 0) {
        wait();
    }

    Map<String, Integer> taken = new HashMap<>();
    int remaining = no_boxes;

    for (Map.Entry<String, Integer> entry : stock.entrySet()) {
        if (remaining == 0) {
            break;
        }

        String section = entry.getKey();
        int available = entry.getValue();

        if (available <= 0) {
            continue;
        }

        int amountTaken = Math.min(available, remaining);

        entry.setValue(available - amountTaken);
        taken.put(section, amountTaken);
        current_boxes -= amountTaken;
        remaining -= amountTaken;
    }

    return taken;
}

    // Synchronized so concurrent delivery threads cannot race when updating capacity.
    // Output uses key=value pairs for consistent log parsing.
    public synchronized void addDelivery(Map<String, Integer> delivery, long tick, String threadId) {
        int totalBoxes = 0;
        StringBuilder details = new StringBuilder();
        for (Map.Entry<String, Integer> entry : delivery.entrySet()) {
            totalBoxes += entry.getValue();
            stock.merge(entry.getKey(), entry.getValue(), Integer::sum);
            details.append(" ").append(entry.getKey()).append("=").append(entry.getValue());
        }
        this.current_boxes += totalBoxes;
        System.out.println(
            "tick=" + tick
            + " tid=" + threadId
            + " event=delivery_arrived"
            + " total_boxes=" + totalBoxes
            + " staging_capacity=" + this.current_boxes
            + details
            + stock
        );

        notifyAll();
    }

    public synchronized int TakeFromSection(String section, int amount) {
        int available = stock.getOrDefault(section, 0);
        if (available == 0) {
            return 0;
        }

        int taken = Math.min(available, amount);
        stock.put(section, available - taken);
        current_boxes -= taken;
        return taken;
    }
}