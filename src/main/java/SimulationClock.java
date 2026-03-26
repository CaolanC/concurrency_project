package whorchestrator;

import java.util.concurrent.atomic.AtomicLong;

public class SimulationClock extends Thread {
    private final long tickDurationMs;
    private final AtomicLong currentTick;

    public SimulationClock(long tickDurationMs) {
        this.tickDurationMs = tickDurationMs;
        this.currentTick = new AtomicLong(0);
        setName("SimulationClock");
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(tickDurationMs);
                currentTick.incrementAndGet();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public long getCurrentTick() {
        return currentTick.get();
    }

    public void sleepTicks(long ticks) throws InterruptedException {
        Thread.sleep(ticks * tickDurationMs);
    }
}