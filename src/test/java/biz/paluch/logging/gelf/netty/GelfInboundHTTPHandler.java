package biz.paluch.logging.gelf.netty;

import java.util.ArrayList;
import java.util.List;

import biz.paluch.logging.gelf.JsonUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

/**
 * @author Aleksandar Stojadinovic
 */
public class GelfInboundHTTPHandler extends SimpleChannelInboundHandler<Object> {

    private List<Object> values = new ArrayList<Object>();
    private HttpRequest httpRequest;
    private HttpContent httpContent;
    private HttpResponseStatus responseStatus = HttpResponseStatus.ACCEPTED;

    private final StringBuilder contentBuffer = new StringBuilder();

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object message) throws Exception {

        resetState();
        if (message instanceof HttpRequest) {
            httpRequest = (HttpRequest) message;
        }
        if (message instanceof HttpContent) {
            httpContent = (HttpContent) message;
            contentBuffer.append(httpContent.content().toString(CharsetUtil.UTF_8));

            if (message instanceof LastHttpContent) {
                Object parsedContent = JsonUtil.parseToMap(contentBuffer.toString());
                synchronized (values) {
                    values.add(parsedContent);
                }
                writeResponse(channelHandlerContext);
                closeConnection(channelHandlerContext);
            }
        }

    }

    private void resetState() {
        this.values.clear();
        this.httpContent = null;
        this.httpRequest = null;
    }

    private void closeConnection(ChannelHandlerContext channelHandlerContext) {
        channelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    private void writeResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus);
        ctx.write(response);
    }

    public List<Object> getValues() {
        return values;
    }

    public String getUri() {
        return httpRequest.getUri();
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setReturnStatus(HttpResponseStatus status) {
        this.responseStatus = status;
    }
}
