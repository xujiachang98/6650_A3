import com.rabbitmq.client.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DBThread implements Runnable {
    private final static String QUEUE_NAME = "queue";

    private Connection connection;
    private JedisPool jedisPool;

    /**
     * Create a thread with a JedisPool.
     * @param connection Connection to RabbitMQ
     * @param jedisPool JedisPool to Redis
     */
    public DBThread(Connection connection, JedisPool jedisPool) {
        this.connection = connection;
        this.jedisPool = jedisPool;
    }

    @Override
    public void run() {
        try {
            final Channel channel = connection.createChannel();
            Map<String, Object> args = new HashMap<>();
            channel.queueDeclare(QUEUE_NAME, true, false, false, args);

            channel.basicConsume(QUEUE_NAME, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body);
                    String[] parts = message.split(" ");
                    String leftOrRight = parts[1];
                    String swiperId = parts[3];
                    String swipeeId = parts[5];

                    try (Jedis jedis = jedisPool.getResource()) {
                        jedis.hincrBy(swiperId, leftOrRight, 1);
                        if (leftOrRight.equals("right")) {
                            String matches = swiperId + "-Matches";
                            jedis.lpush(matches, swipeeId);
                        }
                    }
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
