package net.openhft.chronicle.threads;

import net.openhft.chronicle.core.Jvm;
import net.openhft.chronicle.core.onoes.ExceptionKey;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DiskSpaceMonitorTest extends ThreadsTestCommon {

    @Test
    public void pollDiskSpace() {
        // todo investigate why this fails on arm
        Assume.assumeTrue(!Jvm.isArm());
        Map<ExceptionKey, Integer> map = Jvm.recordExceptions();
        DiskSpaceMonitor.INSTANCE.setThresholdPercentage(100);
        for (int i = 0; i < 500; i++) {
            DiskSpaceMonitor.INSTANCE.pollDiskSpace(new File("."));
            Jvm.pause(5);
        }
        DiskSpaceMonitor.INSTANCE.clear();
        map.entrySet().forEach(System.out::println);
        long count = map.values().stream().mapToInt(i -> i).sum();
        Jvm.resetExceptionHandlers();
        // look for 5 disk space checks and some debug messages about slow disk checks.
        assertEquals(5 - 1, count, 1);
    }
}