package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

@Data
public class UserConnected implements CloudMessage {

    private String login;

    public UserConnected(String login) {
        this.login = login;
    }

    @Override
    public CommandType getType() {
        return CommandType.CONNECTED;
    }
}
