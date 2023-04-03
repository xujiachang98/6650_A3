import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Executor {
    final static int POOL_SIZE = 100;
    final static int NUM_REQUEST = 500000;
    public static void main(String[] args) {
        RequestCounter counter = new RequestCounter();
        ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
        List<String[]> getResults = new ArrayList<>();
        GetThread getThread = new GetThread(getResults);
        executor.execute(getThread);
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_REQUEST; i++) {
            executor.execute(new SwipeThread(counter));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getThread.stop();
        long end = System.currentTimeMillis();
        long totalTime = (end - start) / 1000;
        long throughput = NUM_REQUEST / totalTime;

        System.out.println("Number of successful requests sent: " + counter.getSuccessCount());
        System.out.println("Number of unsuccessful requests: " + counter.getUnsuccessfulCount());
        System.out.println("Total run time (wall time): " + totalTime + " seconds");
        System.out.println("Total throughput: " + throughput + " requests per second");
    }
}