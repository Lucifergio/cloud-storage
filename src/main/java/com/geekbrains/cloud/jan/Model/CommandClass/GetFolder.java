package com.geekbrains.cloud.jan.Model.CommandClass;

import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

import java.nio.file.Path;

@Data
public class GetFolder implements CloudMessage {

    private String folder;

    public GetFolder(Path folder) {
        this.folder = folder.toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.FOLDER;
    }
}
