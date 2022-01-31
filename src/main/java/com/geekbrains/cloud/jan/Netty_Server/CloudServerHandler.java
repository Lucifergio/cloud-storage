package com.geekbrains.cloud.jan.Netty_Server;

import com.geekbrains.cloud.jan.Model.*;
import com.geekbrains.cloud.jan.Model.CommandClass.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        currentDir = Paths.get("root/user"); //Директория клиента
        sendFolder(ctx); // Отправка текущей директории.
        sendList(ctx); // Отправка списка файлов в текущей директории.
    }

    /**
     * Канал чтения команд от клиента.
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        log.info("Received: " + cloudMessage);

        switch (cloudMessage.getType()) {
            case FILE_REQUEST: // Запрос файла клиентом.
                processFileRequest((FileRequest) cloudMessage, ctx);
                break;
            case FILE: // Скачивание файла клиентом с сервера.
                processFileMessage((FileMessage) cloudMessage);
                sendList(ctx);
                sendFolder(ctx);
                break;
            case CLICK_BACK: // Сообщение от клиента о нажатии конрки "Назад".
                processBackOk(ctx);
                break;
            case CLICK_DIR: // Сообщение от клиента о двойном клике по директории.
                processClickDir((ClickDir) cloudMessage,ctx);
        }
    }



    private void processClickDir(ClickDir cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = currentDir.resolve(cloudMessage.getClickDir());
        if (Files.isDirectory(path)) {
            currentDir = path;
            sendList(ctx);
            sendFolder(ctx);
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