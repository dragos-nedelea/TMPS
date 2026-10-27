package tmps.portfolio.domain.pool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Object Pool Pattern: High-throughput Valuation Worker Pool.
 *
 * Problem Solved:
 * Avoids repeated expensive allocation and Garbage Collection pressure when running
 * frequent asset appraisals, risk simulations, and stress tests.
 * Manages a fixed set of reusable ValuationWorker instances in a thread-safe manner.
 */
public class ValuationWorkerPool {

    private final int capacity;
    private final BlockingQueue<ValuationWorker> availableWorkers;

    public ValuationWorkerPool(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Pool capacity must be greater than zero.");
        }
        this.capacity = capacity;
        this.availableWorkers = new LinkedBlockingQueue<>(capacity);

        // Pre-allocate workers up to capacity
        for (int i = 0; i < capacity; i++) {
            availableWorkers.offer(new ValuationWorker());
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public int getAvailableCount() {
        return availableWorkers.size();
    }

    public int getInUseCount() {
        return capacity - availableWorkers.size();
    }

    /**
     * Acquires a worker from the pool, waiting up to timeoutMs if necessary.
     */
    public ValuationWorker acquire(long timeoutMs) throws InterruptedException {
        ValuationWorker worker = availableWorkers.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (worker == null) {
            throw new IllegalStateException("Object Pool exhausted: No available ValuationWorker within " + timeoutMs + "ms timeout.");
        }
        worker.setInUse(true);
        return worker;
    }

    /**
     * Non-blocking acquire; returns null if none immediately available.
     */
    public ValuationWorker acquireNow() {
        ValuationWorker worker = availableWorkers.poll();
        if (worker != null) {
            worker.setInUse(true);
        }
        return worker;
    }

    /**
     * Releases a worker back into the pool after resetting its state.
     */
    public void release(ValuationWorker worker) {
        if (worker == null) return;
        if (!worker.isInUse()) {
            throw new IllegalStateException("Worker " + worker.getWorkerId() + " was not checked out or was already returned.");
        }

        // Clean internal state for subsequent client reuse
        worker.reset();
        worker.setInUse(false);

        boolean returned = availableWorkers.offer(worker);
        if (!returned) {
            throw new IllegalStateException("Failed to return worker to pool (pool overflow anomaly).");
        }
    }
}
