package com.geekbrains.cloud.jan.Netty_Server;

import com.geekbrains.cloud.jan.Netty_Server.AuthServiceDB.AuthService;
import com.geekbrains.cloud.jan.Netty_Server.AuthServiceDB.DbAuth;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class BaseServer {

    private AuthService authService;

    public static void main(String[] args) {
        new BaseServer();
    }

    public BaseServer() {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(), new CloudServerHandler());
                            log.info("Client connected...");
                        }
                    });

            ChannelFuture future = bootstrap.bind(8189).sync();

            authService = DbAuth.getInstance();
            authService.start();

            log.info("Server started...");

            future.channel().closeFuture().sync(); // block

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
