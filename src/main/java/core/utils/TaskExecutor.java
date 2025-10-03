package core.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskExecutor {
    private static final ThreadPoolExecutor executor =
            new ThreadPoolExecutor(
                    10, 50, 60L, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(1000),
                    new ThreadPoolExecutor.CallerRunsPolicy()
            );

    public static void submit(Runnable task) {
        executor.submit(task);
    }
}
