package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

@Data
public class ClickBack implements CloudMessage {

    @Override
    public CommandType getType() {
        return CommandType.CLICK_BACK;
    }
}
