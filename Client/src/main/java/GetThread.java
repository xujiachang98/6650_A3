import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GetThread implements Runnable {
    private List<String[]> gets;
    private boolean running;
    private long[] latencies;
    private int count;

    public GetThread(List<String[]> gets) {
        this.gets = gets;
        this.running = true;
        this.latencies = new long[5];
        this.count = 0;
    }

    @Override
    public void run() {
        try {
            while (running) {
                for (int i = 0; i < 5; i++) {
                    String[] arr = new String[4];
                    long start = System.currentTimeMillis();
                    arr[0] = String.valueOf(start);
                    arr[1] = "get";
                    int rand = ThreadLocalRandom.current().nextInt(0, 2);
                    int status = sendGetRequest(rand);
                    long end = System.currentTimeMillis();
                    long interval = end - start;
                    arr[2] = String.valueOf(interval);
                    arr[3] = String.valueOf(status);
                    latencies[i] = interval;
                    count++;
                    gets.add(arr);
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        } finally {
            long minLatency = Long.MAX_VALUE;
            long maxLatency = Long.MIN_VALUE;
            long totalLatency = 0;
            for (long latency : latencies) {
                if (latency < minLatency) {
                    minLatency = latency;
                }
                if (latency > maxLatency) {
                    maxLatency = latency;
                }
                totalLatency += latency;
            }
            double meanLatency = totalLatency / (double) latencies.length;
            System.out.println("Min latency: " + minLatency + " ms");
            System.out.println("Mean latency: " + meanLatency + " ms");
            System.out.println("Max latency: " + maxLatency + " ms");
        }
    }

    public void stop() {
        this.running = false;
    }

    private int sendGetRequest(int rand) throws IOException {
        int responseCode = 400;
        String request = rand == 0 ? "stats" : "matches";
        String randId = String.valueOf(ThreadLocalRandom.current().nextInt(1, 5001));
        String urlString = "http://35.90.11.66:8080/Servlet_war/" + "/" + request + "/" + randId;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        responseCode = conn.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return responseCode;
    }
}
