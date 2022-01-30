package com.geekbrains.cloud.jan.Model;

import java.io.Serializable;

public interface CloudMessage extends Serializable {
    CommandType getType();
}
