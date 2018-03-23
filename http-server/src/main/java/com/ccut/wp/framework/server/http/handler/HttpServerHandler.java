package com.ccut.wp.framework.server.http.handler;

import com.ccut.wp.framework.server.http.core.URLDispatcher;
import com.ccut.wp.framework.server.http.dto.HttpRequestKit;
import com.ccut.wp.framework.server.http.dto.HttpResponseKit;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

/**
 * Created by lixiaoqing on 2018/3/22.
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerHandler.class);

    URLDispatcher urlDispatcher;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        if (!ctx.channel().isActive()) {
            LOG.error("[HttpServerHandler] [channelRead0] [isNotActive] [close]");
            ctx.channel().close();
            return;
        }
        messageReceived(ctx, msg);
    }


    protected  void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {

        final DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add("Cache-Control", "must-revalidate");
        response.headers().add("Cache-Control", "no-cache");
        response.headers().add("Cache-Control", "no-store");
        response.headers().add("Expires", 0);

        final HttpRequestKit request = new HttpRequestKit(ctx.channel(), msg);

        HttpRequestContextHolder.setRequestHeader(request.getHeaders());

        HttpResponseKit httpResponseKit = new HttpResponseKit(response);

        urlDispatcher.doDispatcher(request,httpResponseKit);

        writeResponse(ctx.channel(),request.getHttpRequest(),httpResponseKit.getHttpResponse());

        HttpRequestContextHolder.resetRequestHeader();
    }


    private void writeResponse(Channel channel, HttpRequest fullHttpRequest, FullHttpResponse response) {
        // Decide whether to close the connection or not.
        boolean close = fullHttpRequest.headers().contains(CONNECTION, HttpHeaders.Values.CLOSE, true)
                || fullHttpRequest.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !fullHttpRequest.headers().contains(CONNECTION, HttpHeaders.Values.KEEP_ALIVE, true);

        // Build the response object.
        //response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        }
        // Write the response.
        ChannelFuture future = channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.error("[HttpServerHandler] [channelInactive] [close]");
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error(cause.getMessage(), cause);
        ctx.channel().close();
    }

    public void setDispatcher(URLDispatcher dispatcher) {
        this.urlDispatcher = dispatcher;
    }
}
