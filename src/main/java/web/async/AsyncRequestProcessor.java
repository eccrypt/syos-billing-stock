package web.async;

import javax.servlet.AsyncContext;
import java.util.concurrent.*;

public class AsyncRequestProcessor {
    private static AsyncRequestProcessor instance;
    private ExecutorService executor;
    private BlockingQueue<Runnable> queue;

    private AsyncRequestProcessor() {
        int corePoolSize = 10;
        int maxPoolSize = 50;
        long keepAliveTime = 60L;
        TimeUnit unit = TimeUnit.SECONDS;
        queue = new LinkedBlockingQueue<>();
        executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, queue);
    }

    public static synchronized AsyncRequestProcessor getInstance() {
        if (instance == null) {
            instance = new AsyncRequestProcessor();
        }
        return instance;
    }

    public void submitTask(Runnable task) {
        executor.submit(task);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}