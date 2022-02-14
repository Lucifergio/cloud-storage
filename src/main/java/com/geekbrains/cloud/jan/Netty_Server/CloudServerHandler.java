package com.geekbrains.cloud.jan.Netty_Server;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandClass.*;
import com.geekbrains.cloud.jan.Netty_Server.AuthServiceDB.AuthService;
import com.geekbrains.cloud.jan.Netty_Server.AuthServiceDB.DbAuth;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path userDir;
    private Path rootDir;
    private AuthService authService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        if (Files.exists(Paths.get("root"))) { // Корневая директория
            rootDir = Paths.get("root");
        } else {
            rootDir = Files.createDirectories(Paths.get("root"));
        }

        authService = new DbAuth();
        authService.start();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        log.info("Received: " + cloudMessage);

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
                processDirResponse(ctx);
                break;
            case CLICK_DIR:
                processClickDir((ClickDir) cloudMessage, ctx);
                break;
            case SIGN_IN:
                processSingIn((SignIn) cloudMessage, ctx);
                break;
            case SIGN_UP:
                processSignUP((SignUp) cloudMessage, ctx);
                break;
            case CONNECTED:
                processConnected((UserConnected) cloudMessage, ctx);
                break;
            case FILE_DELETE:
                processFileDelete((FileDelete) cloudMessage, ctx);
                break;
            case FILE_RENAME:
                processFileRename((FileRename) cloudMessage, ctx);
                break;
        }
    }

    private void processFileRename(FileRename cloudMessage, ChannelHandlerContext ctx) {

        Path fileName = userDir.resolve(cloudMessage.getFileName()).toAbsolutePath();
        Path newFileName = Paths.get(fileName.getParent() + "/" + cloudMessage.getNewFilename());
        try {
            Files.move(fileName, newFileName);
            sendList(ctx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processFileDelete(FileDelete cloudMessage, ChannelHandlerContext ctx) {
        try {
            Path fileName = userDir.resolve(cloudMessage.getFile()).toAbsolutePath();
            Files.delete(fileName);
            sendList(ctx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processConnected(UserConnected cloudMessage, ChannelHandlerContext ctx) throws IOException {
        if (Files.exists(Paths.get("root/" + cloudMessage.getLogin()))) { // Корневая директория
            userDir = Paths.get("root/" + cloudMessage.getLogin());
        } else {
            userDir = Files.createDirectories(Paths.get("root/" + cloudMessage.getLogin()));
        }
        sendList(ctx);
        sendFolder(ctx);
    }

    private void processSignUP(SignUp cloudMessage, ChannelHandlerContext ctx) throws IOException {
        if (authService.checkLogin(cloudMessage.getLogin())) {
            ctx.writeAndFlush(new UnknownUser());
        } else {
            authService.insertUserBatch(cloudMessage.getLogin(), cloudMessage.getPass());
            if (Files.exists(Paths.get("root/" + cloudMessage.getLogin()))) { // Корневая директория
                userDir = Paths.get("root/" + cloudMessage.getLogin());
            } else {
                userDir = Files.createDirectories(Paths.get("root/" + cloudMessage.getLogin()));
                ctx.writeAndFlush(new ConfirmedUser());
            }
        }
    }

    private void processSingIn(SignIn cloudMessage, ChannelHandlerContext ctx) {
        if (authService.getLoginAndPass(cloudMessage.getLogin(), cloudMessage.getPass())) {
            ctx.writeAndFlush(new ConfirmedUser());
        } else {
            ctx.writeAndFlush(new UnknownUser());
        }
    }

    private void processClickDir(ClickDir cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = userDir.resolve(cloudMessage.getClickDir());
        if (Files.isDirectory(path)) {
            userDir = path;
            sendList(ctx);
            sendFolder(ctx);
        }
    }

    private void processDirResponse(ChannelHandlerContext ctx) throws IOException {
        if (!userDir.equals(rootDir)) {
            userDir = userDir.getParent();
            ctx.writeAndFlush(new DirResponse(userDir.toString()));
            sendFolder(ctx);
            sendList(ctx);
        }
    }

    private void sendList(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(new ListMessage(userDir));
    }

    private void sendFolder(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new GetFolder(userDir));
    }

    private void processFileMessage(FileMessage cloudMessage) throws IOException {
        Files.write(userDir.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }

    private void processFileRequest(FileRequest cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = userDir.resolve(cloudMessage.getFileName());
        ctx.writeAndFlush(new FileMessage(path));
    }
}
