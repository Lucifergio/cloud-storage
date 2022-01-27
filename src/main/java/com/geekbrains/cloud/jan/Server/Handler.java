package com.geekbrains.cloud.jan.Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.geekbrains.cloud.jan.Server.Sender.*;

public class Handler implements Runnable{

    private static final int SIZE = 256;

    private Path clientDir;
    private DataInputStream is;
    private DataOutputStream os;

    private final byte[] buffer;

    public Handler (Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        clientDir = Paths.get("root/user");
        buffer = new byte[SIZE];
        sendServerFiles();
    }

    /**
     * Метод отправки списка файлов.
     */

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

        try {
            while (true) {

                String command = is.readUTF();
                System.out.println("Received: " + command);

                /**
                 * Запрос на получение файлов от клиента
                 */

                if (command.equals("#file#")) {
                    getFile(is, clientDir, SIZE, buffer);
                    sendServerFiles();
                }
                /**
                 * Зарос на отправку файла.
                 */

                else if (command.equals("#get_file#")) {
                    String fileName = is.readUTF();
                    sendFile(fileName, os, clientDir);
                }
                /**
                 * Запрос текущей директории.
                 */

                else if (command.equals("#get_folder#")) {
                    System.out.println(clientDir.toString());
                    os.writeUTF("#resp_folder#");
                    os.writeUTF(clientDir.toString());

                }
                /**
                 * Запрос на обработку нажатия кнопки(со стороны сервера) назад.
                 */

                else if (command.equals("#back_dir#")) {
                    if (!clientDir.toString().endsWith("root")) {
                        System.out.println("LOG: " + clientDir.toString());
                        clientDir = clientDir.getParent();
                        sendFolder(clientDir.toString(), os);
                        sendServerFiles();
                    } else {
                        os.writeUTF("root");
                        sendServerFiles();
                    }
                }
                /**
                 * Запрос на падение в директорию.
                 */

                else if (command.equals("#FocusServer#")) {
                    String focus = is.readUTF();
                    File focusFile = new File(clientDir.toString() + "/" + focus);
                    if (focusFile.isDirectory()) {
                        clientDir = Paths.get(focusFile.getAbsolutePath());
                        sendFolder(clientDir.toString(), os);
                        sendServerFiles();
                    }
                }
            }
        }catch (Exception e)  {
            e.printStackTrace();
        }
    }
}