package com.ccut.wp.framework.server.http;

import com.ccut.wp.framework.server.http.core.Action;
import com.ccut.wp.framework.server.http.core.AnnotationParser;
import com.ccut.wp.framework.server.http.core.URLDispatcher;
import com.ccut.wp.framework.server.http.handler.HttpServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by lixiaoqing on 2018/3/22.
 */
public class HttpServer {
    private static Logger LOG = LoggerFactory.getLogger(HttpServer.class);

    private AnnotationParser annotationParser = new AnnotationParser();
    private int port;
    private int bossThreadSize;
    private int workThreadSize;
    private List<String> scanPackageList;

    private HttpServer(int port, int bossThreadSize, int workThreadSize, List<String> scanPackageList) {
        this.port = port;
        this.bossThreadSize = bossThreadSize;
        this.workThreadSize = workThreadSize;
        this.scanPackageList = scanPackageList;
    }


    public void start(){
        EventLoopGroup bossGroup = null;
        EventLoopGroup workerGroup = null;
        try {
            bossGroup = new NioEventLoopGroup(bossThreadSize, new DefaultThreadFactory("bossGroupThread"));
            workerGroup = new NioEventLoopGroup(workThreadSize, new DefaultThreadFactory("workerGroupThread"));
            
            //build DisPatcher
            final URLDispatcher dispatcher = new URLDispatcher();
            try {
                Map<String,Action> actionMap = annotationParser.parseAction(scanPackageList);
                dispatcher.setActionMap(actionMap);
            } catch (Exception e) {
                LOG.error(e.getMessage(),e);
                System.exit(1);
            }


            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64, 10240, 65536));

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new HttpRequestDecoder());
                    ch.pipeline().addLast(new HttpResponseEncoder());
                    ch.pipeline().addLast("aggregator", new HttpObjectAggregator(8*1024*1024));
                    HttpServerHandler httpServerHandler = new HttpServerHandler();
                    httpServerHandler.setDispatcher(dispatcher);
                    ch.pipeline().addLast(httpServerHandler);
                }
            });

            ChannelFuture future = serverBootstrap.bind(port).sync();
            LOG.info("------Netty http server has been started at port: {} ------", port);
            future.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static Builder create() {
        return new Builder();
    }

    public static final class Builder{
        private int port = 8080;
        private int bossThreadSize = 1;
        private int workerThreadSize = 20;
        private List<String> scanPackageList;

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder bossThreadSize(int bossThreadSize) {
            this.bossThreadSize = bossThreadSize;
            return this;
        }

        public Builder workerThreadSize(int workerThreadSize) {
            this.workerThreadSize = workerThreadSize;
            return this;
        }

        public Builder scanPackageList(List<String> scanPackageList) {
            this.scanPackageList = scanPackageList;
            return this;
        }


        public HttpServer build() {
            return new HttpServer(port,bossThreadSize,workerThreadSize,scanPackageList);
        }

    }


}
