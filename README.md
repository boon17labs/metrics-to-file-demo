# metrics-to-file-demo

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Build-Maven-blue?logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-green)

Demo applications exercising [metrics-to-file](https://github.com/boon17labs/metrics-to-file),
consumed from `~/dev/metrics-to-file` as a local Maven dependency
(`mvn install` it there first — see below).

## Modules

```
demo-core             → demonstrates and exercises metrics-to-file-core
demo-prometheus        → placeholder for future metrics-to-file-prometheus testing
demo-spring            → placeholder for future metrics-to-file-spring testing
demo-autoinstrument    → placeholder for future metrics-to-file-autoinstrument testing
demo-full              → uses all metrics-to-file modules together
```

## Prerequisites

Install `metrics-to-file` into your local Maven repository:

```bash
cd ~/dev/metrics-to-file
mvn install -DskipTests
```

## Running demo-core

`demo-core`'s `DemoApp` starts metrics-to-file-core with every metric
enabled (heap, metaspace, threads, gc, direct memory, class loading, cpu,
code cache), then for 10 minutes simulates heap pressure, thread churn, and
periodic custom metrics (`messages`, `cache`, `queue`) before stopping
cleanly:

```bash
cd metrics-to-file-demo
mvn -pl demo-core -am compile exec:java
```

Metrics are written to `demo-core/metrics/demo-core-<yyyy-MM-dd>.log`.
Press Ctrl+C to stop early — shutdown is handled cleanly either way.

## Running demo-full

```bash
mvn -pl demo-full -am compile exec:java
```

Currently equivalent to `demo-core` (see that module's Javadoc) since the
prometheus/spring/autoinstrument modules don't exist yet upstream.
