package com.example.fileserver.modules.folder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.fileserver.common.api.ResultCode;
import com.example.fileserver.common.constant.FileConstants;
import com.example.fileserver.common.constant.PolicyConstants;
import com.example.fileserver.common.exception.BusinessException;
import com.example.fileserver.modules.folder.entity.FsFolder;
import com.example.fileserver.modules.folder.mapper.FolderMapper;
import com.example.fileserver.modules.folder.service.FolderService;
import com.example.fileserver.modules.folder.vo.FolderTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderMapper folderMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public FsFolder createFolder(Long userId, String folderName, Long parentId) {
        // Validate folder name
        if (folderName == null || folderName.isBlank()) {
            throw new BusinessException(ResultCode.FOLDER_NAME_INVALID);
        }

        // Check for path traversal
        if (folderName.contains("..") || folderName.contains("/") || folderName.contains("\\")) {
            throw new BusinessException(ResultCode.FOLDER_NAME_INVALID, "文件夹名包含非法字符");
        }

        // Calculate depth
        int depth = 0;
        String folderPath = "/" + folderName;
        if (parentId != null && parentId > 0) {
            FsFolder parent = folderMapper.selectById(parentId);
            if (parent == null || parent.getIsDeleted()) {
                throw new BusinessException(ResultCode.FOLDER_NOT_FOUND, "父文件夹不存在");
            }
            if (!parent.getOwnerUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权访问父文件夹");
            }
            depth = parent.getDepth() + 1;
            folderPath = parent.getFolderPath() + "/" + folderName;
        }

        // Check max depth
        if (depth > FileConstants.MAX_FOLDER_DEPTH) {
            throw new BusinessException(ResultCode.FOLDER_DEPTH_EXCEEDED);
        }

        // Check duplicate name
        LambdaQueryWrapper<FsFolder> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(FsFolder::getOwnerUserId, userId)
                  .eq(FsFolder::getParentId, parentId)
                  .eq(FsFolder::getFolderName, folderName)
                  .eq(FsFolder::getIsDeleted, false);
        Long dupCount = folderMapper.selectCount(dupWrapper);
        if (dupCount > 0) {
            throw new BusinessException(ResultCode.FOLDER_ALREADY_EXISTS);
        }

        FsFolder folder = new FsFolder();
        folder.setOwnerUserId(userId);
        folder.setParentId(parentId != null ? parentId : FileConstants.ROOT_FOLDER_ID_LONG);
        folder.setFolderName(folderName);
        folder.setFolderPath(folderPath);
        folder.setDepth(depth);
        folder.setSortOrder(0);
        folder.setIsDeleted(false);
        folderMapper.insert(folder);

        // Clear folder tree cache
        clearFolderTreeCache(userId);

        log.info("Folder created: userId={}, folderId={}, name={}", userId, folder.getId(), folderName);
        return folder;
    }

    @Override
    public List<FolderTreeVO> getFolderTree(Long userId) {
        String cacheKey = PolicyConstants.CACHE_FOLDER_TREE_PREFIX + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List) {
            @SuppressWarnings("unchecked")
            List<FolderTreeVO> result = (List<FolderTreeVO>) cached;
            return result;
        }

        LambdaQueryWrapper<FsFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsFolder::getOwnerUserId, userId)
               .eq(FsFolder::getIsDeleted, false)
               .orderByAsc(FsFolder::getDepth)
               .orderByAsc(FsFolder::getSortOrder)
               .orderByAsc(FsFolder::getFolderName);
        List<FsFolder> folders = folderMapper.selectList(wrapper);

        List<FolderTreeVO> tree = buildTree(folders);
        redisTemplate.opsForValue().set(cacheKey, tree, 10, java.util.concurrent.TimeUnit.MINUTES);
        return tree;
    }

    @Override
    public IPage<FsFolder> listFolders(Long userId, Long parentId, int pageNum, int pageSize) {
        Page<FsFolder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<FsFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsFolder::getOwnerUserId, userId)
               .eq(FsFolder::getParentId, parentId)
               .eq(FsFolder::getIsDeleted, false)
               .orderByAsc(FsFolder::getSortOrder)
               .orderByAsc(FsFolder::getFolderName);
        return folderMapper.selectPage(page, wrapper);
    }

    @Override
    public void deleteFolder(Long userId, Long folderId) {
        FsFolder folder = folderMapper.selectById(folderId);
        if (folder == null || folder.getIsDeleted()) {
            throw new BusinessException(ResultCode.FOLDER_NOT_FOUND);
        }
        if (!folder.getOwnerUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // Logical delete folder and all sub-folders
        List<FsFolder> allFolders = getAllSubFolders(userId, folderId);
        allFolders.add(folder);

        for (FsFolder f : allFolders) {
            f.setIsDeleted(true);
            f.setDeletedAt(LocalDateTime.now());
            folderMapper.updateById(f);
        }

        clearFolderTreeCache(userId);
        log.info("Folder deleted: userId={}, folderId={}", userId, folderId);
    }

    @Override
    public FsFolder renameFolder(Long userId, Long folderId, String newName) {
        FsFolder folder = folderMapper.selectById(folderId);
        if (folder == null || folder.getIsDeleted()) {
            throw new BusinessException(ResultCode.FOLDER_NOT_FOUND);
        }
        if (!folder.getOwnerUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // Check duplicate name in same parent
        LambdaQueryWrapper<FsFolder> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(FsFolder::getOwnerUserId, userId)
                  .eq(FsFolder::getParentId, folder.getParentId())
                  .eq(FsFolder::getFolderName, newName)
                  .eq(FsFolder::getIsDeleted, false)
                  .ne(FsFolder::getId, folderId);
        if (folderMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException(ResultCode.FOLDER_ALREADY_EXISTS);
        }

        folder.setFolderName(newName);
        folderMapper.updateById(folder);

        clearFolderTreeCache(userId);
        return folder;
    }

    @Override
    public FsFolder getFolderById(Long folderId) {
        FsFolder folder = folderMapper.selectById(folderId);
        if (folder == null || folder.getIsDeleted()) {
            throw new BusinessException(ResultCode.FOLDER_NOT_FOUND);
        }
        return folder;
    }

    private List<FsFolder> getAllSubFolders(Long userId, Long parentId) {
        LambdaQueryWrapper<FsFolder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FsFolder::getOwnerUserId, userId)
               .eq(FsFolder::getIsDeleted, false);

        List<FsFolder> allFolders = folderMapper.selectList(wrapper);
        List<FsFolder> result = new ArrayList<>();
        collectSubFolders(allFolders, parentId, result);
        return result;
    }

    private void collectSubFolders(List<FsFolder> allFolders, Long parentId, List<FsFolder> result) {
        for (FsFolder folder : allFolders) {
            if (parentId.equals(folder.getParentId())) {
                result.add(folder);
                collectSubFolders(allFolders, folder.getId(), result);
            }
        }
    }

    private List<FolderTreeVO> buildTree(List<FsFolder> folders) {
        List<FolderTreeVO> allVOs = folders.stream().map(f -> {
            FolderTreeVO vo = new FolderTreeVO();
            vo.setId(f.getId());
            vo.setFolderName(f.getFolderName());
            vo.setParentId(f.getParentId());
            vo.setDepth(f.getDepth());
            vo.setFolderPath(f.getFolderPath());
            vo.setCreatedAt(f.getCreatedAt());
            vo.setChildren(new ArrayList<>());
            return vo;
        }).toList();

        Map<Long, FolderTreeVO> voMap = allVOs.stream().collect(Collectors.toMap(FolderTreeVO::getId, v -> v));
        List<FolderTreeVO> roots = new ArrayList<>();

        for (FolderTreeVO vo : allVOs) {
            if (vo.getParentId() == null || vo.getParentId() == 0L) {
                roots.add(vo);
            } else {
                FolderTreeVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }

        return roots;
    }

    private void clearFolderTreeCache(Long userId) {
        String cacheKey = PolicyConstants.CACHE_FOLDER_TREE_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }
}
