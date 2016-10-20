package biz.paluch.logging.gelf;

import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * @author Mark Paluch
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
        run(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(handler);
            }
        });
    }

    public void run(ChannelInitializer<Channel> initializer) throws Exception {
        if (ServerChannel.class.isAssignableFrom(channelClass)) {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group);
            b.channel((Class) channelClass).childHandler(initializer).childOption(ChannelOption.RCVBUF_ALLOCATOR,
                    new AdaptiveRecvByteBufAllocator(8192, 8192, Integer.MAX_VALUE));

            // Bind and start to accept incoming connections.
            f = b.bind(port).sync();
        } else {
            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel(channelClass).handler(initializer);

            // Bind and start to accept incoming connections.
            f = b.bind(port).sync();
        }

    }

    public void close() {

        if (f != null) {
            f.channel().close();
            f = null;
        }
    }

    public List<Object> getJsonValues() {
        return handler.getJsonValues();
    }

    public GelfInboundHandler getHandler() {
        return handler;
    }

    public void clear() {
        handler.clear();
    }

    public int getPort() {
        return port;
    }
}
