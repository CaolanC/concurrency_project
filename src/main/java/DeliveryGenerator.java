package whorchestrator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Dedicated thread that simulates incoming deliveries over time.
public class DeliveryGenerator extends Thread {
    private final StagingArea stagingArea;
    private final List<Section> sections;
    private final int boxesPerDelivery;
    private final double deliveryPerTickProbability;
    private final long tickDurationMs;
    private final Random random;

    public DeliveryGenerator(
        StagingArea stagingArea,
        List<Section> sections,
        int boxesPerDelivery,
        double deliveryPerTickProbability,
        long tickDurationMs
    ) {
        this.stagingArea = stagingArea;
        this.sections = sections;
        this.boxesPerDelivery = boxesPerDelivery;
        this.deliveryPerTickProbability = deliveryPerTickProbability;
        this.tickDurationMs = tickDurationMs;
        this.random = new Random();
        setName("DeliveryGenerator");
    }

    @Override
    public void run() {
        long tick = 0;
        // One loop iteration represents one simulation tick.
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(tickDurationMs);
                tick++;
                // Probabilistic arrival: a delivery may or may not appear each tick.
                if (random.nextDouble() < deliveryPerTickProbability) {
                    Map<String, Integer> delivery = generateDelivery();
                    stagingArea.addDelivery(delivery, tick, "DEL");
                }
            } catch (InterruptedException interruptedException) {
                // Preserve interruption status so higher-level shutdown logic can stop cleanly.
                Thread.currentThread().interrupt();
            }
        }
    }

    private Map<String, Integer> generateDelivery() {
        Map<String, Integer> delivery = new LinkedHashMap<>();
        // Initialize all known sections at zero so logs are consistent across events.
        for (Section section : sections) {
            delivery.put(section.name, 0);
        }

        // Distribute exactly boxesPerDelivery boxes randomly across sections.
        for (int index = 0; index < boxesPerDelivery; index++) {
            int sectionIndex = random.nextInt(sections.size());
            String sectionName = sections.get(sectionIndex).name;
            delivery.put(sectionName, delivery.get(sectionName) + 1);
        }
        return delivery;
    }
}