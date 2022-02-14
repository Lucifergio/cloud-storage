package com.geekbrains.cloud.jan.Client;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandClass.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public class Client implements Initializable {

    public ListView<String> clientList;
    public ListView<String> serverList;

    public TextField FolderClient;
    public TextField FolderServer;

    private Path clientDir;
    private Path rootClientDir;

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        try {
            clientDir = Paths.get(System.getProperty("user.home"));
            rootClientDir = clientDir;

            Socket socket = new Socket("localhost", 8189);

            log.info("Network created...");

            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            updateClientView();
            initMouseListeners();

            os.writeObject(new UserConnected(Authorization.getLogin()));

            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                    case DIRECTORY_RESPONSE:
                        processDirResponse((DirResponse) message);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processDirResponse(DirResponse message) {
        FolderServer.setText(message.getDirBack());
    }

    private void processFolderMessage(GetFolder message) {
        FolderServer.setText(message.getFolder());
    }

    private void processListMessage(ListMessage message) {
        Platform.runLater(() -> {
            serverList.getItems().clear();
            serverList.getItems().addAll(message.getFiles());
        });
    }

    private void processFileMessage(FileMessage message) throws IOException {
        Files.write(clientDir.resolve(message.getFileName()), message.getBytes());
        Platform.runLater(this::updateClientView);
    }

    private void updateClientView() {
        try {
            clientList.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientList.getItems().add(f));
            Platform.runLater(() -> FolderClient.setText(clientDir.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMouseListeners() {
        clientList.setOnMouseClicked(e -> { //Client
            if (e.getClickCount() == 2) {
                Path current = clientDir.resolve(getItemClient());
                if (Files.isDirectory(current)) {
                    clientDir = current;
                    Platform.runLater(this::updateClientView);
                }
            }
        });

        serverList.setOnMouseClicked(e -> { //Server
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

    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = getItemClient();
        os.writeObject(new FileMessage(clientDir.resolve(fileName)));
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverList.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
    }

    public void BackButtonClient(ActionEvent actionEvent) {
        if (!clientDir.equals(rootClientDir)) {
            clientDir = clientDir.getParent();
            Platform.runLater(this::updateClientView);
            FolderClient.setText(clientDir.toString());
        }
    }

    public void BackButtonServer(ActionEvent actionEvent) throws IOException {
        os.writeObject(new ClickBack());
    }

    public void renameButton(ActionEvent actionEvent) {
        if (getItemClient() != null) { //Client
            Path fileName = clientDir.resolve(getItemClient()).toAbsolutePath();
            TextInputDialog dialog = new TextInputDialog(getItemClient());

            dialog.setTitle("Rename file");
            dialog.setHeaderText("Changing the file name");
            dialog.setContentText("Please enter new file name:");

            Platform.runLater(() -> {
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    Path newFileName = Paths.get(fileName.getParent() + "/" + result.get());
                    try {
                        Files.move(fileName, newFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateClientView();
                }
            });
        } else if (getItemServer() != null) { //Server

            TextInputDialog dialog = new TextInputDialog(getItemServer());

            dialog.setTitle("Rename file");
            dialog.setHeaderText("Changing the file name");
            dialog.setContentText("Please enter new file name:");

            Platform.runLater(() -> {
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    try {
                        os.writeObject(new FileRename(getItemServer(), result.get()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("The file could not be renamed.");
            alert.setContentText("You have not selected a file.");
            alert.showAndWait();
        }
    }

    public void deleteButton(ActionEvent actionEvent) throws IOException {
        if (getItemClient() != null) { //Client

            Path fileName = clientDir.resolve(getItemClient()).toAbsolutePath();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle("Confirmation delete");
            alert.setHeaderText("Are you sure you want to delete the file " + getItemClient());
            alert.setContentText("Are you ok with this?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                Files.delete(fileName);
                updateClientView();
            }
        } else if (getItemServer() != null) { //Server

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

            alert.setTitle("Confirmation delete");
            alert.setHeaderText("Are you sure you want to delete the file " + getItemServer());
            alert.setContentText("Are you ok with this?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                os.writeObject(new FileDelete(getItemServer()));
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("The file could not be deleted.");
            alert.setContentText("You have not selected a file.");
            alert.showAndWait();
        }
    }

    private String getItemClient() {
        return clientList.getSelectionModel().getSelectedItem();
    }

    private String getItemServer() {
        return serverList.getSelectionModel().getSelectedItem();
    }
}