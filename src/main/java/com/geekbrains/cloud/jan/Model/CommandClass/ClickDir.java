package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

/**
 * Сообщение серверу о двойном клике по директории.
 */

@Data
public class ClickDir implements CloudMessage {

    private String clickDir;

    public ClickDir(String clickDir) {
        this.clickDir = clickDir;
    }

    @Override
    public CommandType getType() {
        return CommandType.CLICK_DIR;
    }
}
