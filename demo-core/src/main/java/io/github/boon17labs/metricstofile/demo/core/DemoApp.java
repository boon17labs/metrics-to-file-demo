package io.github.boon17labs.metricstofile.demo.core;

import io.github.boon17labs.metricstofile.Metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Exercises metrics-to-file-core end to end: starts it with every built-in
 * metric enabled (the four defaults plus all four opt-ins), then drives
 * synthetic heap pressure, thread churn, and periodic custom metrics for a
 * fixed run, so the resulting log file has something interesting in it.
 */
public final class DemoApp {

    private static final Duration RUN_DURATION = Duration.ofMinutes(10);
    private static final Duration METRICS_COLLECTION_INTERVAL = Duration.ofSeconds(30);
    private static final Duration HEAP_PRESSURE_INTERVAL = Duration.ofSeconds(2);
    private static final Duration THREAD_CHURN_INTERVAL = Duration.ofSeconds(5);
    private static final Duration CUSTOM_METRICS_TICK_INTERVAL = Duration.ofSeconds(1);

    private static final AtomicBoolean stopped = new AtomicBoolean(false);

    public static void main(final String[] args) throws InterruptedException {
        // metrics-to-file defaults to a no-op logger until this is set.
        System.setProperty("metrics.implementation", "file");

        Metrics.builder()
                .appName("demo-core")
                .logDir("./metrics")
                .interval(METRICS_COLLECTION_INTERVAL)
                .keepDays(7)
                .withDirectMemory()
                .withClassLoading()
                .withCpu()
                .withCodeCache()
                .start();

        System.out.println("[demo-core] started - heap, metaspace, threads, gc, direct memory, "
                + "class loading, cpu and code cache metrics written to ./metrics every "
                + METRICS_COLLECTION_INTERVAL.getSeconds() + "s");

        final HeapPressureSimulator heapPressure = new HeapPressureSimulator();
        final ThreadChurnSimulator threadChurn = new ThreadChurnSimulator();
        final CustomMetricsSimulator customMetrics = new CustomMetricsSimulator();

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3, DemoApp::daemonThread);
        scheduler.scheduleAtFixedRate(heapPressure::tick,
                0, HEAP_PRESSURE_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(threadChurn::tick,
                0, THREAD_CHURN_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
        scheduler.scheduleAtFixedRate(customMetrics::tick,
                0, CUSTOM_METRICS_TICK_INTERVAL.toMillis(), TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> shutdown(scheduler, threadChurn), "demo-core-shutdown"));

        final Instant deadline = Instant.now().plus(RUN_DURATION);
        System.out.println("[demo-core] running for " + RUN_DURATION.toMinutes()
                + " minutes, stopping around " + deadline);

        Thread.sleep(RUN_DURATION.toMillis());

        shutdown(scheduler, threadChurn);
        System.out.println("[demo-core] stopped cleanly");
    }

    private static void shutdown(final ScheduledExecutorService scheduler, final ThreadChurnSimulator threadChurn) {
        if (!stopped.compareAndSet(false, true)) {
            return;
        }
        scheduler.shutdownNow();
        threadChurn.stopAll();
        Metrics.stop();
    }

    private static Thread daemonThread(final Runnable task) {
        final Thread thread = new Thread(task, "demo-core-scheduler");
        thread.setDaemon(true);
        return thread;
    }

    private DemoApp() {
    }
}
