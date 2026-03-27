package whorchestrator;

import whorchestrator.StagingArea;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;

enum SelectionStrategy {

}

interface StockerSelectionStrategy {
    public Map<String, Integer> TakeFromStagingArea(StagingArea staging_area) throws InterruptedException;
}

class StockerRandomSelectionStrategy implements StockerSelectionStrategy {

    private final Random random = new Random();
    private final List<String> sectionNames;
    private final int maxCapacity;

    public StockerRandomSelectionStrategy(List<String> sectionNames, int maxCapacity) {
        this.sectionNames = new ArrayList<>(sectionNames);
        this.maxCapacity = maxCapacity;
    }

    @Override
    public Map<String, Integer> TakeFromStagingArea(StagingArea staging_area) throws InterruptedException {
        Map<String, Integer> load = new HashMap<>();

        int remaining = maxCapacity;

        // shuffle section order randomly
        List<String> shuffled = new ArrayList<>(sectionNames);
        Collections.shuffle(shuffled, random);

        for (String section : shuffled) {
            if (remaining == 0) break;

            int attempt = 1 + random.nextInt(remaining);

            int taken = staging_area.TakeFromSection(section, attempt);

            if (taken > 0) {
                load.put(section, taken);
                remaining -= taken;
            }
        }

        return load;
    }
}