import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@WebServlet(name = "SwipeServlet", value = "/SwipeServlet")
public class SwipeServlet extends HttpServlet {
    private ChannelPool channelPool;
    private final int MAX_POOL_SIZE = 15;
    private final static String QUEUE_NAME = "queue";
    private final static String RABBITMQ_HOST = "35.160.148.1";
    private final static String RABBITMQ_USERNAME = "admin";
    private final static String RABBITMQ_PASSWORD = "password";

    /**
     * Initialize the servlet with a fixed-sized RabbitMQ channel pool.
     * The channels in the pool are connected and won't be disconnected,
     * so that each doPost request can use a channel without reconnecting.
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_HOST);
            factory.setUsername(RABBITMQ_USERNAME);
            factory.setPassword(RABBITMQ_PASSWORD);
            final Connection conn = factory.newConnection();
            ChannelFactory channelFactory = new ChannelFactory(conn);
            channelPool = new ChannelPool(MAX_POOL_SIZE, channelFactory);
        } catch (IOException | TimeoutException e) {
            throw new ServletException("Failed to initialize the channel pool", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // Not implemented for this servlet
        res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "GET method not implemented");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            res.setContentType("text/plain");
            String urlPath = req.getPathInfo();
            if (urlPath == null || urlPath.isEmpty()) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("Missing URL parameters");
                return;
            }
            String[] urlParts = urlPath.split("/");
            if (!isValidUrlPath(urlParts)) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("Invalid URL path: " + urlPath);
                return;
            }
            String postData = req.getReader().lines().collect(Collectors.joining());
            SwipeDetails swipeDetails;
            try {
                JsonObject post = new Gson().fromJson(postData, JsonObject.class);
                int swiperId = Integer.parseInt(post.get("swiper").getAsString());
                int swipeeId = Integer.parseInt(post.get("swipee").getAsString());
                String comment = post.get("comment").getAsString();
                if (post.keySet().size() != 3 || swiperId < 1 || swiperId > 5000
                        || swipeeId < 1 || swipeeId > 1000000 || comment.length() > 256) {
                    swipeDetails = null;
                }
                swipeDetails =  new SwipeDetails(String.valueOf(swiperId), String.valueOf(swipeeId), comment);
            } catch (Exception e) {
                swipeDetails = null;
            }

            if (swipeDetails == null) {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.getWriter().write("Invalid post data format: " + postData);
                return;
            }
            swipeDetails.setSwipeDirection(urlParts[1]);
            sendMessageToQueue(swipeDetails);
            res.setStatus(HttpServletResponse.SC_CREATED);
            res.getWriter().write("Swipe data sent successfully!");
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("Error occurred: " + e.getMessage());
        }
    }


    /**
     * Validate the URL path, which should have the form "/swipe/left" or "/swipe/right".
     * @param urlParts the parts of the URL path
     * @return true if the URL path is valid, false otherwise
     */
    private boolean isValidUrlPath(String[] urlParts) {
        if (urlParts.length != 2) {
            return false;
        }
        String swipeDirection = urlParts[1];
        return swipeDirection.equals("left") || swipeDirection.equals("right");
    }

    /**
     * Borrow a channel from the pool, declare the exchange, and send a message to the queue.
     * Then return the channel to the pool.
     * @param swipeDetails the swipeDetails to send
     * @throws Exception if any error occurs while sending the message
     */
    private void sendMessageToQueue(SwipeDetails swipeDetails) throws Exception {
        Channel channel = null;
        try {
            channel = channelPool.borrowChannel();
            String message = "LeftOrRight: " + swipeDetails.getSwipeDirection() + " SwiperId: " + swipeDetails.getSwiper() + " SwipeeId: "+
                    swipeDetails.getSwipee() + " comment: " + swipeDetails.getComment();
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        } finally {
            if (channel != null) {
                channelPool.returnChannel(channel);
            }
        }
    }

}