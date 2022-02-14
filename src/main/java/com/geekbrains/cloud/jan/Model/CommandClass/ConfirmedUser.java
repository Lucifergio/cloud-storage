package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;

public class ConfirmedUser implements CloudMessage {
    @Override
    public CommandType getType() {
        return CommandType.CONFIRM_USER;
    }
}
