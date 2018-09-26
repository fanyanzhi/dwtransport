package ksyun.dwtransport.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 *
 * @author FANJIAQI
 *
 */
public class ThreadUtil {
    public static ExecutorService executors = Executors.newFixedThreadPool(50);

    public static void execute(Runnable runnable) {
        executors.execute(runnable);
    }
}
