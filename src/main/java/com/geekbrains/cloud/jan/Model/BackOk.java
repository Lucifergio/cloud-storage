package com.geekbrains.cloud.jan.Model;

import lombok.Data;

@Data
public class BackOk implements CloudMessage{
    private String dirBack;

    public BackOk(String dirBack) {
            this.dirBack = dirBack;
    }

    @Override
    public CommandType getType() {
        return CommandType.BACK_OK;
    }
}
