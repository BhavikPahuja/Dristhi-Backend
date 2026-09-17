package com.project.tekathon.drishti.util;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

    private IdGenerator() {
    }

    public static String next(String prefix, long sequence) {
        return prefix + String.format(Locale.ROOT, "%03d", sequence);
    }

    public static long nextSequence(long currentCount) {
        return currentCount + 1;
    }

    public static AtomicLong counter(long start) {
        return new AtomicLong(start);
    }
}
