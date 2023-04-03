import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Main {
    private final static String RABBIT_HOST = "35.145.143.31";
    private final static String REDIS_HOST = "35.160.148.12";

    /**
     * Connect to RabbitMQ and run threads.
     *
     * @param args
     */
    public static void main(String[] args) {
        try (JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), REDIS_HOST)) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBIT_HOST);
            factory.setUsername("admin");
            factory.setPassword("password");

            final Connection connection = factory.newConnection();

            for (int i = 0; i < 10; i++) {
                new Thread(new DBThread(connection, jedisPool)).start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
