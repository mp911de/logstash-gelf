package biz.paluch.logging.gelf;

import io.netty.channel.socket.SocketChannel;
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * Created by aleksandar on 3/12/16.
 */
public class GelfInboundHTTPInitializer extends ChannelInitializer<SocketChannel> {
    protected GelfInboundHTTPHandler handler;

    public GelfInboundHTTPInitializer() {
        handler = new GelfInboundHTTPHandler();
    }

    @Override protected void initChannel(SocketChannel socketChannel) throws Exception {
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
