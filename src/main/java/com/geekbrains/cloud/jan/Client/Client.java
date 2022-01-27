package com.geekbrains.cloud.jan.Client;

import com.geekbrains.cloud.jan.Server.Sender;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;


import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Client implements Initializable {

    private static final int SIZE = 256;
    public TextField clientFolderField;
    public TextField serverFolderField;

    private Path clientDir;
    public ListView <String> clientList;
    public ListView <String> serverList;

    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buffer;
    private File focusFile;

    /**
     * Поток чтения с сервера.
     */
    private void readLoop() {
        try {
            os.writeUTF("#get_folder#"); //ОТПРАВКА ЗАПРОСА НА ПОЛУЧЕНИЕ ТЕКУЩЕЙ ДИРЕКТОРИИ.
            while (true) {

                Thread.sleep(100);
                clientFolderField.setText(clientDir.toString()); // ПОЛУЧАЕМ ТЕКУЩУЮ ДИРЕКТОРИЮ СО СТОРОНЫ КЛИЕНТА.

                String command = is.readUTF(); //ЗАПИСЬ ОТВЕТА ОТ СЕРВЕРА.
                System.out.println("Received: " + command);

                //ОТВЕТ СЕРА НА ПОЛУЧЕНИЕ СПИСКА ФАЙЛОВ.
                if (command.equals("#list#")) {
                    Platform.runLater(() -> serverList.getItems().clear());
                    int fileCount = is.readInt();
                    for (int i = 0; i < fileCount; i++) {
                        String fileName = is.readUTF();
                        Platform.runLater(() -> serverList.getItems().add(fileName));
                    }

                //ОТВЕТ СЕРВЕРА НА СКАЧИВАНИЕ ФАЙЛА.
                }else if (command.equals("#file#")) {
                    Sender.getFile(is, clientDir, SIZE, buffer);
                    Platform.runLater(this::updateClientList);
                }

                // ОТВЕТ СЕРВЕРА НА ТЕКУЩУЮ ДИРЕКТОРИЮ (ДЛЯ МЕЛКОГО ОКОШКА).
                else if (command.equals("#resp_folder#")) {
                    serverFolderField.setText(is.readUTF());
                }

                // ОТВЕТ СЕРВЕРА НА КНОПКУ "Назад"
                else if (command.equals("#resp_folderBack#")) {
                    serverFolderField.setText(is.readUTF());
                    os.writeUTF("#get_folder#");
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

            //ЗАПУСК ПОТОКА-ДЕМОНА НА ЧТЕНИЕ.
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();

            //ЗАПУСК ПОТОКА-ДЕМОНА НА НАВИГАЦИЮ.
            Thread focusView = new Thread(this::navigationFolder);
            focusView.setDaemon(true);
            focusView.start();

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

    /**
     * Кнопка назад со стороны клиента.
     */
    public void folderBackClient(ActionEvent actionEvent) {
        try {
            clientFolderField.setText(clientDir.getParent().toString());
            clientDir = clientDir.getParent();
            updateClientList();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Кнопка назад с серверной стороны.
     */
    public void folderBackServer(ActionEvent actionEvent) {

        try {
                os.writeUTF("#back_dir#");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Навигация
     */
    private void navigationFolder() {

        while (true) {

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (clientList.isFocused()) {

                focusFile = new File(clientDir + "/" + clientList.getFocusModel().getFocusedItem());

                if (focusFile.isDirectory()) {
                    System.out.println(focusFile.getAbsolutePath());
                    clientDir = Paths.get(focusFile.getAbsolutePath());
                    clientFolderField.setText(clientDir.toString());
                    Platform.runLater(this::updateClientList);
                    continue;
                }
            }

            if (serverList.isFocused()) {

                try {
                    os.writeUTF("#FocusServer#");
                    System.out.println(serverList.getFocusModel().getFocusedItem());
                    if (serverList.getFocusModel().getFocusedItem() != null ) {
                        os.writeUTF(serverList.getFocusModel().getFocusedItem());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}