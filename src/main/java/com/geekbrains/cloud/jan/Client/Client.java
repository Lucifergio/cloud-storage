package com.geekbrains.cloud.jan.Client;

import com.geekbrains.cloud.jan.Server.Handler;
import com.geekbrains.cloud.jan.Server.Sender;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;


import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Client implements Initializable {

    private static final int SIZE = 256;

    private Path clientDir;
    public ListView <String> clientList;
    public ListView <String> serverList;

    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buffer;

    /**
     * Поток чтения с сервера.
     */
    private void readLoop() {
        try {
            while (true) {
                String command = is.readUTF();
                System.out.println("Received: " + command);

                if (command.equals("#list#")) {

                    Platform.runLater(() -> serverList.getItems().clear());
                    int fileCount = is.readInt();

                    for (int i = 0; i < fileCount; i++) {
                        String fileName = is.readUTF();
                        Platform.runLater(() -> serverList.getItems().add(fileName));
                    }

                }else if (command.equals("#file#")) {
                    Sender.getFile(is, clientDir, SIZE, buffer);
                    Platform.runLater(this::updateClientList);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обновление файлового окна клиента.
     */
    private void updateClientList() {
        try {
            clientList.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientList.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            buffer = new byte[SIZE];
            clientDir = Paths.get(System.getProperty("user.home"));
            updateClientList();
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Network created...");

            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());

            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Скачивание файла с сервера.
     */
    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverList.getSelectionModel().getSelectedItem();
        os.writeUTF("#get_file#");
        os.writeUTF(fileName);
        os.flush();
    }

    /**
     * Отправка файла на сервер.
     */
    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = clientList.getSelectionModel().getSelectedItem();
        Sender.sendFile(fileName, os, clientDir);
    }
}

