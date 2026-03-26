package whorchestrator;

import whorchestrator.StagingArea;

enum SelectionStrategy {

}

interface StockerSelectionStrategy {
    public void TakeFromStagingArea(StagingArea staging_area);
}

class StockerRandomSelectionStrategy implements StockerSelectionStrategy {
    public void TakeFromStagingArea(StagingArea staging_area) {

    };
}