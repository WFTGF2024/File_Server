package com.example.fileserver.common.exception;

import com.example.fileserver.common.api.ResultCode;
import lombok.Getter;

@Getter
public class FileOperationException extends RuntimeException {

    private final int code;

    public FileOperationException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public FileOperationException(int code, String message) {
        super(message);
        this.code = code;
    }

    public FileOperationException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
