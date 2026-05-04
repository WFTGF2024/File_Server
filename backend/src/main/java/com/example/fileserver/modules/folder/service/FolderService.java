package com.example.fileserver.modules.folder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.fileserver.modules.folder.entity.FsFolder;
import com.example.fileserver.modules.folder.vo.FolderTreeVO;

import java.util.List;

public interface FolderService {

    FsFolder createFolder(Long userId, String folderName, Long parentId);

    List<FolderTreeVO> getFolderTree(Long userId);

    IPage<FsFolder> listFolders(Long userId, Long parentId, int pageNum, int pageSize);

    void deleteFolder(Long userId, Long folderId);

    FsFolder renameFolder(Long userId, Long folderId, String newName);

    FsFolder getFolderById(Long folderId);
}
