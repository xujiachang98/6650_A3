public class RequestCounter {
    private int successful_requests = 0;
    private int unsuccessful_requests = 0;

    public synchronized void incrementSuccessCount() {
        successful_requests++;
    }

    public synchronized void incrementUnsuccessfulCount() {
        unsuccessful_requests++;
    }

    public synchronized int getSuccessCount() {
        return successful_requests;
    }
    public synchronized int getUnsuccessfulCount() {
        return unsuccessful_requests;
    }

}