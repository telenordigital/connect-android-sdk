package com.telenor;

import com.telenor.connect.WellKnownAPI;

import org.robolectric.Robolectric;

import java.util.HashMap;
import java.util.Map;

public class TestHelper {

    public static final Map<String, WellKnownAPI> WELL_KNOWN_API_MAP = new HashMap<>();

    private static final long SLEEPING_TIME_IN_MILLIES = 10;

    public interface BooleanSupplier {
        boolean getAsBoolean();
    }

    public static boolean flushForegroundTasksUntilCallerIsSatisifed(
            long maxMillies,
            BooleanSupplier isCallerSatisfied) {
        while (maxMillies > 0) {
            if (isCallerSatisfied.getAsBoolean()) {
                return true;
            }
            maxMillies -= SLEEPING_TIME_IN_MILLIES;
            Robolectric.flushForegroundThreadScheduler();
            try {
                Thread.sleep(SLEEPING_TIME_IN_MILLIES);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted!", e);
            }
        }
        return false;
    }
}
