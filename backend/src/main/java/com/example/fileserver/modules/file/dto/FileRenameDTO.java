package com.example.fileserver.modules.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileRenameDTO {

    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @NotBlank(message = "新文件名不能为空")
    private String newName;
}
