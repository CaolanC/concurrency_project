package whorchestrator;

import java.util.List;

interface PickerSelectionStrategy {
    /**
     * Select a section name for the picker to pick from.
     * @param section_names 
     * @return 
     */
    public String selectSection(List<String> section_names);
}
