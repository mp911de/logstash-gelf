package biz.paluch.logging.gelf;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aleksandar on 3/12/16.
 */
public class GelfInboundHTTPHandler extends SimpleChannelInboundHandler<Object> {
    private List<Object> values = new ArrayList<Object>();
    private HttpRequest httpRequest;
    private HttpContent httpContent;
    private HttpResponseStatus responseStatus = HttpResponseStatus.ACCEPTED;

    private final StringBuilder contentBuffer = new StringBuilder();

    @Override protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object message) throws Exception {

        resetState();
        if (message instanceof HttpRequest) {
            httpRequest = (HttpRequest) message;
        }
        if (message instanceof HttpContent) {
            httpContent = (HttpContent) message;
            contentBuffer.append(httpContent.content().toString(CharsetUtil.UTF_8));
            if (message instanceof LastHttpContent) {
                Object parsedContent = JSONValue.parse(contentBuffer.toString());
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

    public HttpMethod getMethod() {
        return httpRequest.getMethod();
    }

    public void setReturnStatus(HttpResponseStatus status) {
        this.responseStatus = status;
    }
}
