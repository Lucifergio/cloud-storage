package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

@Data
public class NewFolder implements CloudMessage {
    String nameFolder;

    public NewFolder(String nameFolder) {
        this.nameFolder = nameFolder;
    }

    @Override
    public CommandType getType() {
        return CommandType.NEW_FOLDER;
    }
}
