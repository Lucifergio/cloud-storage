package com.geekbrains.cloud.jan.Model;

import lombok.Data;

@Data
public class ClickBack implements CloudMessage{

    @Override
    public CommandType getType() {
        return CommandType.CLICK_BACK;
    }
}
