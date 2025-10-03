package core.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class LockManager {
    private static final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public static ReentrantLock getLock(String key) {
        return locks.computeIfAbsent(key, k -> new ReentrantLock());
    }
}
