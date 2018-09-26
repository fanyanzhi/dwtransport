package ksyun.dwtransport.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 *
 *
 * @author FANJIAQI
 *
 */
public class BoundedExecutor {

    private static final int THREAD_BOUND = 10;
    private static ExecutorService exec = Executors.newFixedThreadPool(THREAD_BOUND);
    private static Semaphore semaphore = new Semaphore(THREAD_BOUND);

    public static void submitTask(final Runnable runnable)
            throws InterruptedException {
        semaphore.acquire();
        try {
            exec.execute(() -> {
                try {
                    runnable.run();
                } finally {
                    semaphore.release();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            semaphore.release();
        }
    }
}