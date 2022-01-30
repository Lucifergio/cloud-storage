package com.geekbrains.cloud.jan.Netty_Cloud;

import com.geekbrains.cloud.jan.Model.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Осталось допилить клик по директории
 * Подключить к гит
 */

public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // init client dir
        currentDir = Paths.get("root/user");
        sendFolder(ctx);
        sendList(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        switch (cloudMessage.getType()) {
            case FILE_REQUEST:
                processFileRequest((FileRequest) cloudMessage, ctx);
                break;
            case FILE:
                processFileMessage((FileMessage) cloudMessage);
                sendList(ctx);
                sendFolder(ctx);
                break;
            case CLICK_BACK:
                System.out.println("Ok");
                processBackOk(ctx);
                break;
        }
    }

    private void processBackOk(ChannelHandlerContext ctx) throws IOException {
        if (!currentDir.endsWith("root")) {
            currentDir = currentDir.getParent();
            ctx.writeAndFlush(new BackOk(currentDir.toString()));
            sendFolder(ctx);
            sendList(ctx);
        }
    }

    private void sendList(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(new ListMessage(currentDir));
    }

    private void sendFolder(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new GetFolder(currentDir));
    }

    private void processFileMessage(FileMessage cloudMessage) throws IOException {
        Files.write(currentDir.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }

    private void processFileRequest(FileRequest cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = currentDir.resolve(cloudMessage.getFileName());
        ctx.writeAndFlush(new FileMessage(path));
    }
}