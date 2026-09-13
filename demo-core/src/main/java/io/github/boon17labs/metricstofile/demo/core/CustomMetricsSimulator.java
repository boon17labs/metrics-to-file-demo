package io.github.boon17labs.metricstofile.demo.core;

import io.github.boon17labs.metricstofile.Metrics;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simulates application-level activity (message processing, a cache, a work
 * queue) and periodically logs it as custom metrics through {@link Metrics}.
 * Counters are accumulated on every {@link #tick()} but only written out
 * every {@link #LOG_EVERY_N_TICKS} ticks, matching the library's guidance to
 * aggregate rather than call {@code Metrics.log()} once per event.
 */
final class CustomMetricsSimulator {

    private static final int LOG_EVERY_N_TICKS = 15; // ticks are 1s apart -> log every ~15s
    private static final long QUEUE_CAPACITY = 500;
    private static final long CACHE_CAPACITY = 1000;

    private final AtomicLong messagesReceived = new AtomicLong();
    private final AtomicLong messagesProcessed = new AtomicLong();
    private final AtomicLong messagesFailed = new AtomicLong();
    private final AtomicLong cacheHits = new AtomicLong();
    private final AtomicLong cacheMisses = new AtomicLong();
    private final AtomicLong cacheSize = new AtomicLong();
    private final AtomicLong queueSize = new AtomicLong();

    private int tickCount;

    void tick() {
        simulateActivity();
        tickCount++;
        if (tickCount % LOG_EVERY_N_TICKS == 0) {
            logMetrics();
        }
    }

    private void simulateActivity() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();

        final long received = random.nextLong(5, 25);
        final long failed = random.nextLong(0, Math.max(1, received / 10) + 1);
        final long processed = received - failed;
        messagesReceived.addAndGet(received);
        messagesProcessed.addAndGet(processed);
        messagesFailed.addAndGet(failed);

        if (random.nextInt(10) < 8) {
            cacheHits.incrementAndGet();
        } else {
            cacheMisses.incrementAndGet();
            cacheSize.updateAndGet(size -> Math.min(size + 1, CACHE_CAPACITY));
        }
        if (random.nextInt(20) == 0) {
            cacheSize.updateAndGet(size -> Math.max(0, size - random.nextLong(1, 50)));
        }

        final long queueDelta = random.nextLong(-10, 15);
        queueSize.updateAndGet(size -> Math.min(Math.max(0, size + queueDelta), QUEUE_CAPACITY));
    }

    private void logMetrics() {
        Metrics.log("messages", Map.of(
                "received", messagesReceived.get(),
                "processed", messagesProcessed.get(),
                "failed", messagesFailed.get()));

        Metrics.log("cache", Map.of(
                "hits", cacheHits.get(),
                "misses", cacheMisses.get(),
                "size", cacheSize.get()));

        Metrics.log("queue", Map.of(
                "size", queueSize.get(),
                "capacity", QUEUE_CAPACITY));
    }
}
