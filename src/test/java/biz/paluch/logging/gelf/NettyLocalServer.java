package biz.paluch.logging.gelf;

import java.util.List;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 10.11.13 10:30
 */
public class NettyLocalServer {

    private int port = 19392;
    private EventLoopGroup group = new NioEventLoopGroup();
    private GelfInboundHandler handler = new GelfInboundHandler();

    private ChannelFuture f;

    public NettyLocalServer() {
    }

    public void run() throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(group);
        b.channel(NioDatagramChannel.class).handler(handler).option(ChannelOption.SO_BROADCAST, true);

        // Bind and start to accept incoming connections.
        f = b.bind(port).sync();

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
