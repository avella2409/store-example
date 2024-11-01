package com.avella.store.merchant.integration.shared;

import java.util.function.Supplier;

public class Waiter {

    public static void waitUntil(Supplier<Boolean> isReady, int delayMs, int maxTotalTime) {
        waitUntil(isReady, delayMs, maxTotalTime, 0);
    }

    private static void waitUntil(Supplier<Boolean> isReady, long delayMs, long maxWaitTime, long waitTime) {
        if (waitTime > maxWaitTime) throw new RuntimeException("Never ready");
        else if (!isReady.get()) {
            try {
                Thread.sleep(delayMs);
                waitUntil(isReady, delayMs, maxWaitTime, waitTime + delayMs);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
