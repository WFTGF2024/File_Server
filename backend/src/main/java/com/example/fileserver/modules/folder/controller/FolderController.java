package com.example.fileserver.modules.folder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.fileserver.common.api.Result;
import com.example.fileserver.common.security.SecurityUserContext;
import com.example.fileserver.modules.folder.dto.FolderCreateDTO;
import com.example.fileserver.modules.folder.entity.FsFolder;
import com.example.fileserver.modules.folder.service.FolderService;
import com.example.fileserver.modules.folder.vo.FolderTreeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping("/create")
    public Result<FsFolder> create(@Valid @RequestBody FolderCreateDTO dto) {
        Long userId = SecurityUserContext.getCurrentUserId();
        return Result.success(folderService.createFolder(userId, dto.getFolderName(), dto.getParentId()));
    }

    @GetMapping("/tree")
    public Result<List<FolderTreeVO>> tree() {
        Long userId = SecurityUserContext.getCurrentUserId();
        return Result.success(folderService.getFolderTree(userId));
    }

    @GetMapping("/list")
    public Result<IPage<FsFolder>> list(
            @RequestParam(defaultValue = "0") Long parentId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "50") int pageSize) {
        Long userId = SecurityUserContext.getCurrentUserId();
        return Result.success(folderService.listFolders(userId, parentId, pageNum, pageSize));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUserContext.getCurrentUserId();
        folderService.deleteFolder(userId, id);
        return Result.success();
    }

    @PutMapping("/rename")
    public Result<FsFolder> rename(@RequestParam Long folderId, @RequestParam String newName) {
        Long userId = SecurityUserContext.getCurrentUserId();
        return Result.success(folderService.renameFolder(userId, folderId, newName));
    }
}
