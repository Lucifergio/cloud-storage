package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

@Data
public class FileRename implements CloudMessage {

    private String fileName;
    private String newFilename;

    public FileRename(String fileName, String newFilename) {
        this.fileName = fileName;
        this.newFilename = newFilename;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_RENAME;
    }
}
