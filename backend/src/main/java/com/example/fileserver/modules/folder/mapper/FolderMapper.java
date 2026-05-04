package com.example.fileserver.modules.folder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fileserver.modules.folder.entity.FsFolder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FolderMapper extends BaseMapper<FsFolder> {
}
