package com.example.fileserver.modules.file.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileMoveDTO {

    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @NotNull(message = "目标文件夹ID不能为空")
    private Long targetFolderId;
}
