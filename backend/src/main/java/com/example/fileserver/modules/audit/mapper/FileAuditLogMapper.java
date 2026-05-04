package com.example.fileserver.modules.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fileserver.modules.audit.entity.FsFileAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileAuditLogMapper extends BaseMapper<FsFileAuditLog> {
}
