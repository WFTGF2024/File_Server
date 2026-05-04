package com.example.fileserver.modules.folder.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FolderTreeVO {

    private Long id;
    private String folderName;
    private Long parentId;
    private Integer depth;
    private String folderPath;
    private LocalDateTime createdAt;
    private List<FolderTreeVO> children;
}
