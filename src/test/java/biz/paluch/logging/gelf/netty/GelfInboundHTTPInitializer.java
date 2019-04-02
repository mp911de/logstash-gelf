package biz.paluch.logging.gelf.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * @author Aleksandar Stojadinovic
 */
public class GelfInboundHTTPInitializer extends ChannelInitializer<SocketChannel> {

    private GelfInboundHTTPHandler handler;

    public GelfInboundHTTPInitializer() {
        handler = new GelfInboundHTTPHandler();
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpObjectAggregator(1048576));
        pipeline.addLast(new HttpResponseEncoder());
        pipeline.addLast(handler);
    }

    public GelfInboundHTTPHandler getHandler() {
        return handler;
    }
}
