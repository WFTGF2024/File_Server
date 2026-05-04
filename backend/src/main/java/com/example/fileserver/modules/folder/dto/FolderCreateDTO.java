package com.example.fileserver.modules.folder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FolderCreateDTO {

    @NotBlank(message = "文件夹名称不能为空")
    private String folderName;

    @NotNull(message = "父文件夹ID不能为空")
    private Long parentId;
}
