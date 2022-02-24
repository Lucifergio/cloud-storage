package com.geekbrains.cloud.jan.Model.CommandClass;


import com.geekbrains.cloud.jan.Model.CloudMessage;
import com.geekbrains.cloud.jan.Model.CommandType;
import lombok.Data;

@Data
public class FileRequest implements CloudMessage {

    private final String fileName;

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}
