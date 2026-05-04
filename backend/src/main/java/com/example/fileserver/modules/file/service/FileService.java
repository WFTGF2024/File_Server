package com.example.fileserver.modules.file.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.fileserver.modules.file.entity.FsFile;
import com.example.fileserver.modules.file.vo.FileDetailVO;
import com.example.fileserver.modules.file.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface FileService {

    FileVO upload(Long userId, MultipartFile file, Long parentFolderId, String description);

    InputStream download(Long userId, Long fileId);

    IPage<FileVO> listFiles(Long userId, Long parentFolderId, int pageNum, int pageSize);

    void deleteFile(Long userId, Long fileId);

    FileVO renameFile(Long userId, Long fileId, String newName);

    FileVO moveFile(Long userId, Long fileId, Long targetFolderId);

    IPage<FileVO> searchFiles(Long userId, String keyword, int pageNum, int pageSize);

    FileDetailVO getFileDetail(Long userId, Long fileId);

    FsFile getFileById(Long fileId);

    void decrementRefCount(Long fileId);

    void deleteFileRecord(Long fileId);

    void restoreFileRecord(FsFile file);
}
