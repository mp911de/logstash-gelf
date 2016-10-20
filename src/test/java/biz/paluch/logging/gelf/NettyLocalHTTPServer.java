package biz.paluch.logging.gelf;

import java.util.List;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author Aleksandar Stojadinovic
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

        if (f != null) {
            f.channel().close();
            f = null;
        }
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

    public HttpMethod getLastHttpRequest() {
        return handlerInitializer.getHandler().getHttpRequest().getMethod();
    }

    public HttpHeaders getLastHttpHeaders() {
        return handlerInitializer.getHandler().getHttpRequest().headers();
    }
}
