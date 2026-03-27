package whorchestrator;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Section {
    public final String name;
    private int capacity;
    private int current_boxes;
    private final int max_capacity;
    
    // Lock for synchronizing access between pickers and stockers
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    Section(String name, int starting_capacity) {
        this.name = name;
        this.current_boxes = starting_capacity;
        this.max_capacity = 50; // Default max capacity, could be changed
        this.capacity = starting_capacity;
        System.out.println(String.format("Section: %s created with %d boxes.", name, starting_capacity));
    }

    /**
     * Pick a box from this section. Waits if the section is empty until a box becomes available.
     * Takes 1 tick to pick the box.
     * 
     * @param picker_id 
     * @param pick_id 
     * @param section_name 
     * @param clock simulation clock
     * @return true if pick was successful, false if interrupted
     */
    public boolean pickBox(int picker_id, long pick_id, String section_name, SimulationClock clock) {
        try {
            lock.readLock().lock();
            
            // Wait until section has boxes
            while (current_boxes == 0) {
                lock.readLock().unlock();
                try {
                    // Wait 1 tick and check again
                    clock.sleepTicks(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
                lock.readLock().lock();
            }
            
            // Now there is at least one box, but we need write access to take the box
            lock.readLock().unlock();
            lock.writeLock().lock();
            
            try {
                if (current_boxes > 0) {
                    current_boxes--;
                    clock.sleepTicks(1); // Takes 1 tick to pick
                    return true;
                }
            // If we got here, it means the section became empty before we could pick, so we return false.
            } finally {
                lock.writeLock().unlock();
            }
            
            return false;
        } catch (InterruptedException e) {
            // If interrupted while waiting for the lock.
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Get the current number of boxes in this section.
     */
    public int getCurrentBoxCount() {
        lock.readLock().lock();
        try {
            return current_boxes;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Add boxes to this section (used by stockers).
     * Returns number of boxes added (capped by max capacity).
     */
    public int addBoxes(int num_boxes) {
        lock.writeLock().lock();
        try {
            int space_available = max_capacity - current_boxes;
            // Add as many boxes as possible
            int boxes_to_add = Math.min(num_boxes, space_available);
            current_boxes += boxes_to_add;
            return boxes_to_add;
        // lock is released in the calling code after stocking is done, allowing pickers to access the section while stocking is in progress.
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Check if this section has space for more boxes.
     */
    public boolean hasSpace() {
        lock.readLock().lock();
        try {
            return current_boxes < max_capacity;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Acquire exclusive access to this section (for stocking).
     */
    public void lockForStocking() {
        lock.writeLock().lock();
    }

    /**
     * Release exclusive access to this section.
     */
    public void unlockForStocking() {
        lock.writeLock().unlock();
    }
}