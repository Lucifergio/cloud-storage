package com.geekbrains.cloud.jan.Netty_Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseServer {

    public BaseServer(ChannelHandler ... handlers) {
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(handlers);
                        }
                    });
            ChannelFuture future = bootstrap.bind(8189).sync();
            log.info("Server started...");

            future.channel().closeFuture().sync(); // block

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
