package io.github.boon17labs.metricstofile.demo.full;

import io.github.boon17labs.metricstofile.Metrics;

import java.time.Duration;

/**
 * Intended to exercise every metrics-to-file module together. Today only
 * {@code metrics-to-file-core} exists (the prometheus, spring and
 * autoinstrument modules are "not started" per the parent project's
 * README), so this currently just starts core with every metric enabled.
 * As each sibling module ships, add its dependency alongside
 * {@code metrics-to-file-core} in this module's {@code pom.xml} and wire
 * it in here.
 */
public final class DemoApp {

    public static void main(final String[] args) {
        System.setProperty("metrics.implementation", "file");

        Metrics.builder()
                .appName("demo-full")
                .logDir("./metrics")
                .interval(Duration.ofSeconds(30))
                .keepDays(7)
                .withDirectMemory()
                .withClassLoading()
                .withCpu()
                .withCodeCache()
                .start();

        System.out.println("[demo-full] started with metrics-to-file-core only "
                + "(prometheus/spring/autoinstrument modules not yet available)");

        Runtime.getRuntime().addShutdownHook(new Thread(Metrics::stop, "demo-full-shutdown"));
    }

    private DemoApp() {
    }
}
