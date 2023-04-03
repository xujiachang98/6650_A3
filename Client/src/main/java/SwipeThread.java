import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SwipeThread extends Thread {
    RequestCounter counter;

    public SwipeThread(RequestCounter counter) {
        this.counter = counter;
    }

    public void run() {
        ApiClient client = new ApiClient();
        client.setBasePath("http://35.90.11.66:8080/Servlet_war/");
        SwipeApi swipeApi = new SwipeApi(client);
        SwipeDetails swipeDetails = new SwipeDetails();

        String leftOrRight = ThreadLocalRandom.current().nextInt(0, 2) == 0 ? "left" : "right";
        String swiperId = String.valueOf(ThreadLocalRandom.current().nextInt(1, 5001));
        String swipeeId = String.valueOf(ThreadLocalRandom.current().nextInt(1, 1000001));
        String comment = generateRandomString(20);
        swipeDetails.setSwiper(swiperId);
        swipeDetails.setSwipee(swipeeId);
        swipeDetails.setComment(comment);

        for (int i = 0; i < 5; i++) {
            try {
                ApiResponse res = swipeApi.swipeWithHttpInfo(swipeDetails, leftOrRight);
                if (res.getStatusCode() == 201) {
                    counter.incrementSuccessCount();
                    break;
                }
            } catch (ApiException e) {
                if (i == 4) {
                    counter.incrementUnsuccessfulCount();
                    System.out.println(e);
                    System.out.println(e.getCode());
                }
            }
        }
    }

    private static String generateRandomString(int length) {
        StringBuilder builder = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            char c = (char) (rand.nextInt(26) + 'a');
            builder.append(c);
        }
        return builder.toString();
    }
}