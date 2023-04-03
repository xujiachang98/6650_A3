import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;

public class ChannelFactory extends BasePooledObjectFactory<Channel> {
    private final Connection connection;

    public ChannelFactory(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Channel create() throws IOException {
        // Create a new Channel instance
        Channel channel = connection.createChannel();
        return channel;
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
        // Return a new MyPooledObject wrapping the provided MyObject instance
        return new DefaultPooledObject<>(channel);
    }

    @Override
    public void destroyObject(PooledObject<Channel> pooledObject) throws Exception {
        // Destroy the MyObject instance wrapped by the provided MyPooledObject
        Channel channel = connection.createChannel();
        channel.close();
    }
}
