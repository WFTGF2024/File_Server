package com.example.fileserver.modules.file.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileVO {

    private Long id;
    private String fileName;
    private String fileExt;
    private String mimeType;
    private Long fileSize;
    private String fileHash;
    private Long parentFolderId;
    private String description;
    private Integer previewStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
