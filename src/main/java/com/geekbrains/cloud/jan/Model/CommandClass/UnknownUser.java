package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;

public class UnknownUser implements CloudMessage {

    @Override
    public CommandType getType() {
        return CommandType.UNKNOWN_USER;
    }
}
