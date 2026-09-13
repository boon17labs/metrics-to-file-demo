package io.github.boon17labs.metricstofile.demo.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulates thread activity by starting a small batch of short-lived worker
 * threads on every tick (each doing a bit of CPU work then sleeping briefly
 * before finishing) so the thread-count metric visibly rises and falls.
 */
final class ThreadChurnSimulator {

    private static final int WORKERS_PER_BATCH = 5;

    private final AtomicInteger batchCounter = new AtomicInteger();
    private final List<Thread> spawned = new ArrayList<>();

    synchronized void tick() {
        spawned.removeIf(thread -> !thread.isAlive());

        final int batch = batchCounter.incrementAndGet();
        for (int i = 0; i < WORKERS_PER_BATCH; i++) {
            final Thread worker = new Thread(ThreadChurnSimulator::doWork, "demo-worker-" + batch + "-" + i);
            worker.setDaemon(true);
            worker.start();
            spawned.add(worker);
        }
    }

    synchronized void stopAll() {
        for (final Thread thread : spawned) {
            thread.interrupt();
        }
        spawned.clear();
    }

    private static void doWork() {
        try {
            final ThreadLocalRandom random = ThreadLocalRandom.current();
            final long busyDeadline = System.currentTimeMillis() + random.nextLong(200, 1500);
            double accumulator = 0;
            while (System.currentTimeMillis() < busyDeadline) {
                accumulator += Math.sqrt(accumulator + 1); // burn some CPU
            }
            Thread.sleep(random.nextLong(50, 300));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
