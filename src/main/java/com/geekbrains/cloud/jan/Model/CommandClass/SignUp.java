package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

@Data
public class SignUp implements CloudMessage {

    private final String login;
    private final String pass;

    public SignUp(String login, String pass) {
        this.login = login;
        this.pass = pass;
    }

    @Override
    public CommandType getType() {
        return CommandType.SIGN_UP;
    }
}
