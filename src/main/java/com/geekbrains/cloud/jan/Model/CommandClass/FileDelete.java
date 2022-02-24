package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

@Data
public class FileDelete implements CloudMessage {

    private String file;

    public FileDelete(String file) {
        this.file = file;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_DELETE;
    }
}
