package whorchestrator;

import whorchestrator.StagingArea;
public class StagingArea {
    private boolean active;
    private int capacity;
    private int max_capacity; // <- Need to handle for integer limits regardless of whether implementing max_capacity. Wait this is Java.

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
}