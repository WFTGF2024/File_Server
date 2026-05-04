package com.example.fileserver.modules.file.convert;

import com.example.fileserver.modules.file.entity.FsFile;
import com.example.fileserver.modules.file.vo.FileDetailVO;
import com.example.fileserver.modules.file.vo.FileVO;

public class FileConvert {

    private FileConvert() {
    }

    public static FileVO toVO(FsFile file) {
        FileVO vo = new FileVO();
        vo.setId(file.getId());
        vo.setFileName(file.getFileName());
        vo.setFileExt(file.getFileExt());
        vo.setMimeType(file.getMimeType());
        vo.setFileSize(file.getFileSize());
        vo.setFileHash(file.getFileHash());
        vo.setParentFolderId(file.getParentFolderId());
        vo.setDescription(file.getDescription());
        vo.setPreviewStatus(file.getPreviewStatus());
        vo.setCreatedAt(file.getCreatedAt());
        vo.setUpdatedAt(file.getUpdatedAt());
        return vo;
    }

    public static FileDetailVO toDetailVO(FsFile file) {
        FileDetailVO vo = new FileDetailVO();
        vo.setId(file.getId());
        vo.setFileName(file.getFileName());
        vo.setFileExt(file.getFileExt());
        vo.setMimeType(file.getMimeType());
        vo.setFileSize(file.getFileSize());
        vo.setFileHash(file.getFileHash());
        vo.setParentFolderId(file.getParentFolderId());
        vo.setStorageProvider(file.getStorageProvider());
        vo.setRefCount(file.getRefCount());
        vo.setDescription(file.getDescription());
        vo.setPreviewStatus(file.getPreviewStatus());
        vo.setCreatedAt(file.getCreatedAt());
        vo.setUpdatedAt(file.getUpdatedAt());
        return vo;
    }
}
