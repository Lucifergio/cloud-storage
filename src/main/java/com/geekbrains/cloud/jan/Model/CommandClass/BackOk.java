package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

/**
 * Ответ сервера на нажатие кнопки "Назад"
 */

@Data
public class BackOk implements CloudMessage {
    private String dirBack;

    public BackOk(String dirBack) {
            this.dirBack = dirBack;
    }

    @Override
    public CommandType getType() {
        return CommandType.BACK_OK;
    }
}
