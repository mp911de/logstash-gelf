package biz.paluch.logging.gelf;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 10.11.13 10:30
 */
public class NettyLocalServer {

    private int port = 19392;
    private EventLoopGroup group = new NioEventLoopGroup();
    private GelfInboundHandler handler = new GelfInboundHandler();
    private Class<? extends Channel> channelClass;

    private ChannelFuture f;

    public NettyLocalServer(Class<? extends Channel> channelClass) {
        this.channelClass = channelClass;
    }

    public void run() throws Exception {
        if (ServerChannel.class.isAssignableFrom(channelClass)) {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group);
            b.channel((Class) channelClass)
                    .childHandler(handler)
                    .childOption(ChannelOption.RCVBUF_ALLOCATOR,
                            new AdaptiveRecvByteBufAllocator(8192, 8192, Integer.MAX_VALUE));

            // Bind and start to accept incoming connections.
            f = b.bind(port).sync();
        } else {
            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel((Class) channelClass).handler(handler);

            // Bind and start to accept incoming connections.
            f = b.bind(port).sync();
        }

    }

    public void close() {
        f.channel().close();
        f = null;
    }

    public List<Object> getJsonValues() {
        return handler.getJsonValues();
    }

    public void clear() {
        handler.clear();
    }
}
