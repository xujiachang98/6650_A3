import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelPool {

    private final BlockingQueue<Channel> availableChannels;
    private final int poolCapacity;
    private final ChannelFactory channelFactory;

    public ChannelPool(int poolCapacity, ChannelFactory channelFactory) {
        this.poolCapacity = poolCapacity;
        this.channelFactory = channelFactory;
        availableChannels = new LinkedBlockingQueue<>(poolCapacity);
        for (int i = 0; i < poolCapacity; i++) {
            try {
                Channel channel = channelFactory.create();
                availableChannels.put(channel);
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(ChannelPool.class.getName())
                        .log(Level.SEVERE, "Failed to create channel for the pool", ex);
            }
        }
    }

    public Channel borrowChannel() throws IOException {
        try {
            return availableChannels.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("Error: no channels available in the pool", e);
        }
    }

    public void returnChannel(Channel channel) {
        if (channel != null) {
            availableChannels.offer(channel);
        }
    }
}
