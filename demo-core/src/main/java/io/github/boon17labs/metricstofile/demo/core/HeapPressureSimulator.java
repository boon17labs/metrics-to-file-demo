package io.github.boon17labs.metricstofile.demo.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Creates heap pressure by retaining a growing list of byte-array chunks
 * (visible as rising "used"/"committed" in the heap metric) and periodically
 * discarding the whole list so the next GC reclaims it, while also
 * allocating short-lived garbage on every tick to keep young-gen busy.
 */
final class HeapPressureSimulator {

    private static final int MAX_RETAINED_CHUNKS = 200;
    private static final int RETAINED_CHUNK_BYTES = 256 * 1024;
    private static final int GARBAGE_ALLOCATIONS_PER_TICK = 20;

    private final List<byte[]> retained = new ArrayList<>();

    void tick() {
        retained.add(new byte[RETAINED_CHUNK_BYTES]);

        for (int i = 0; i < GARBAGE_ALLOCATIONS_PER_TICK; i++) {
            final byte[] garbage = new byte[ThreadLocalRandom.current().nextInt(1024, 8192)];
            garbage[0] = 1; // touch it so the allocation can't be optimized away
        }

        if (retained.size() >= MAX_RETAINED_CHUNKS) {
            retained.clear();
        }
    }
}
