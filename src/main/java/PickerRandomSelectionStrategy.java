package whorchestrator;

import java.util.List;
import java.util.Random;

class PickerRandomSelectionStrategy implements PickerSelectionStrategy {
    private Random random = new Random();

    @Override
    public String selectSection(List<String> section_names) {
        int randomIndex = random.nextInt(section_names.size());
        return section_names.get(randomIndex);
    }
}
