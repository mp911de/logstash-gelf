package biz.paluch.logging.gelf;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 10.11.13 10:30
 */
public class NettyLocalHTTPServer {

    private int port = 19393;
    private EventLoopGroup group = new NioEventLoopGroup();
    private GelfInboundHTTPInitializer handlerInitializer = new GelfInboundHTTPInitializer();
    private Class<? extends Channel> channelClass = NioServerSocketChannel.class;

    private ChannelFuture f;

    public NettyLocalHTTPServer() {
    }

    public void run() throws Exception {

        ServerBootstrap b = new ServerBootstrap();
        b.group(group);
        b.channel((Class) channelClass).childHandler(handlerInitializer);
        f = b.bind(port).sync();
    }

    public void close() {
        f.channel().close();
        f = null;
    }

    public List<Object> getJsonValues() {
        return handlerInitializer.getHandler().getValues();
    }

    public void setReturnStatus(HttpResponseStatus status) {
        handlerInitializer.getHandler().setReturnStatus(status);
    }

    public GelfInboundHTTPInitializer getHandlerInitializer() {
        return handlerInitializer;
    }
}
