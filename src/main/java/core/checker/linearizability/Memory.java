package core.checker.linearizability;

import lombok.extern.slf4j.Slf4j;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Slf4j
public class Memory {
    final static private double frac = 0.9;

    public static void onLowMemory(Callable<Object> callable) {
        List<MemoryPoolMXBean> ps = ManagementFactory.getMemoryPoolMXBeans().stream().filter(p -> p.getType() == MemoryType.HEAP && p.isUsageThresholdSupported()).collect(Collectors.toList());
        MemoryPoolMXBean pool = ps.get(ps.size() - 1);
        pool.setCollectionUsageThreshold(((Double) Math.floor(frac * pool.getUsage().getMax())).longValue());
        NotificationEmitter mem = (NotificationEmitter) ManagementFactory.getMemoryMXBean();
        NotificationListener l = (notification, handback) -> {
            if (Objects.equals(notification.getType(), MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED)) {
                try {
                    callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error(e.getMessage());
                }

            }
        };

        mem.addNotificationListener(l, null, null);
        try {
            mem.removeNotificationListener(l);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

    }
}
