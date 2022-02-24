package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

@Data
public class DirResponse implements CloudMessage {

    private String dirBack;

    public DirResponse(String dirBack) {
            this.dirBack = dirBack;
    }

    @Override
    public CommandType getType() {
        return CommandType.DIRECTORY_RESPONSE;
    }
}
