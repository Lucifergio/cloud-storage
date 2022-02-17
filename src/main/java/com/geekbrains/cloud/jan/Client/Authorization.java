package com.geekbrains.cloud.jan.Client;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandClass.SignIn;
import com.geekbrains.cloud.jan.Model.CommandClass.SignUp;
import com.geekbrains.cloud.jan.Model.CommandType;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Authorization implements Initializable {

    static Socket socket;

    public TextField loginField;
    public TextField passField;
    public AnchorPane ssss;

    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;

    private static String login;

    protected static String getLogin() {
        return login;
    }

    public void LoginButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {

        if (!loginField.getText().isEmpty() && !loginField.getText().isEmpty()) {

            os.writeObject(new SignIn(loginField.getText(), passField.getText()));

            while (true) {
                CloudMessage message = (CloudMessage) is.readObject();
                if (message.getType() == CommandType.CONFIRM_USER) {
                    is.close();
                    os.close();
                    socket.close();
                    processConfirmUser();
                    break;

                } else if (message.getType() == CommandType.UNKNOWN_USER) {
                    loginField.clear();
                    passField.clear();
                    loginField.setPromptText("Invalid login or password");
                    passField.setPromptText("Invalid username or password");
                    break;
                }
            }
        } else {
            loginField.clear();
            passField.clear();
            loginField.setPromptText("Login and password fields cannot be empty.");
            passField.setPromptText("Login and password fields cannot be empty.");
        }
    }

    public void RegButton(ActionEvent actionEvent) throws IOException, ClassNotFoundException {

        if (!loginField.getText().isEmpty() && !loginField.getText().isEmpty()) {
            os.writeObject(new SignUp(loginField.getText(), passField.getText()));

            while (true) {
                CloudMessage message = (CloudMessage) is.readObject();
                if (message.getType() == CommandType.CONFIRM_USER) {
                    is.close();
                    os.close();
                    socket.close();
                    processConfirmUser();
                    break;

                } else if (message.getType() == CommandType.UNKNOWN_USER) {
                    loginField.clear();
                    passField.clear();
                    loginField.setPromptText("Such a user already exists.");
                    passField.setPromptText("Such a user already exists.");
                    break;
                }
            }
        } else {
            loginField.clear();
            passField.clear();
            loginField.setPromptText("Login and password fields cannot be empty.");
            passField.setPromptText("Login and password fields cannot be empty.");
        }
    }

    private void processConfirmUser() throws IOException {
        login = loginField.getText();
        StartApp app = new StartApp();
        app.loginBut();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
