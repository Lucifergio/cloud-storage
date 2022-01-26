package com.geekbrains.cloud.jan.Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.geekbrains.cloud.jan.Server.Sender.getFile;
import static com.geekbrains.cloud.jan.Server.Sender.sendFile;

public class Handler implements Runnable{

    private static final int SIZE = 256;

    private Path clientDir;
    private DataInputStream is;
    private DataOutputStream os;

    private final byte[] buffer;

    public Handler (Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        clientDir = Paths.get("root");
        buffer = new byte[SIZE];
    }

    public void sendServerFiles() throws IOException {
       List<String> files = Files.list(clientDir)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
       os.writeUTF("#list#");
       os.writeInt(files.size());
        for (String file : files) {
            os.writeUTF(file);
        }
        os.flush();
    }

    @Override
    public void run() {

        InputStream in = null;
        OutputStream out = null;

        try {

            while (true) {
                String command = is.readUTF();
                System.out.println("Received: " + command);
                if (command.equals("#file#")) {
                    getFile(is, clientDir, SIZE, buffer);
                    sendServerFiles();
                }else if (command.equals("#get_file#")) {
                    String fileName = is.readUTF();
                    sendFile(fileName, os, clientDir);
                }
            }
        }catch (Exception e)  {
            e.printStackTrace();
        }
    }
}
