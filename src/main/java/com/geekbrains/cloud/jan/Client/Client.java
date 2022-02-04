
package com.geekbrains.cloud.jan.Client;

import com.geekbrains.cloud.jan.Model.*;
import com.geekbrains.cloud.jan.Model.CommandClass.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

@Slf4j
public class Client implements Initializable {

    public ListView<String> clientList;
    public ListView<String> serverList;

    public TextField FolderClient;
    public TextField FolderServer;

    private Path clientDir;

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    /**
     * Инициализация клиента
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            clientDir = Paths.get(System.getProperty("user.home"));

            updateClientView(); //Обновление клиентской вьюшки.
            initMouseListeners(); // Читалка кликов по директориям.

            Socket socket = new Socket("localhost", 8189);
            log.info("Network created...");

            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start(); // Запуск потока на чтение.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод чтения команд от сервера
     */
    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = (CloudMessage) is.readObject();
                log.info("received: {}", message);

                switch (message.getType()) {
                    case FOLDER:
                        processFolderMessage((GetFolder) message);
                        break;
                    case FILE:
                        processFileMessage((FileMessage) message);
                        break;
                    case LIST:
                        processListMessage((ListMessage) message);
                        break;
                    case BACK_OK:
                        processBackOk((BackOk) message);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Обработка кнопки "Назад" с серверной стороны.
     */
    private void processBackOk(BackOk message) {
        FolderServer.setText(message.getDirBack());
    }

    /**
     * Обработка сообщения с текущей директорией.
     */
    private void processFolderMessage(GetFolder message) {
        FolderServer.setText(message.getFolder());
    }

    /**
     * Получение списка файлов.
     */
    private void processListMessage(ListMessage message) {
        Platform.runLater(() -> {
            serverList.getItems().clear();
            serverList.getItems().addAll(message.getFiles());
        });
    }

    /**
     * Обработка скачивания файла.
     */
    private void processFileMessage(FileMessage message) throws IOException {
        Files.write(clientDir.resolve(message.getFileName()), message.getBytes());
        Platform.runLater(this::updateClientView);
    }

    /**
     * Обновление списка файлов клиента.
     */
    private void updateClientView() {
        try {
            clientList.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientList.getItems().add(f));
            Platform.runLater(() ->{
                FolderClient.setText(clientDir.toString());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Обработка кликов по директориям.
     */
    private void initMouseListeners() {
        clientList.setOnMouseClicked(e -> { //Сторона клиента.
            if (e.getClickCount() == 2) {
                Path current = clientDir.resolve(getItem());
                if (Files.isDirectory(current)) {
                    clientDir = current;
                    Platform.runLater(this::updateClientView);
                }
            }
        });

        serverList.setOnMouseClicked(e -> { //Сторона сервера.
            if (e.getClickCount() == 2) {
                String fileName = serverList.getSelectionModel().getSelectedItem();
                try {
                    os.writeObject(new ClickDir(fileName));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

    }

    /**
     * Загрузка файлов на сервер.
     */
    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = getItem();
        os.writeObject(new FileMessage(clientDir.resolve(fileName)));
    }

    /**
     * Скачивание файлов с сервера.
     */
    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverList.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
    }

    /**
     * Заготовка выбранного на вьюшке объекта
     */
    private String getItem() {
        return clientList.getSelectionModel().getSelectedItem();
    }

    /**
     * Кнопка назад - клиент.
     */
    public void BackClient(ActionEvent actionEvent) {
        clientDir = clientDir.getParent();
        Platform.runLater(this::updateClientView);
        FolderClient.setText(clientDir.toString());

    }

    /**
     * Кнопка назад - сервер.
     */
    public void BackServer(ActionEvent actionEvent) throws IOException {
        os.writeObject(new ClickBack());
    }
}