package com.example.fileserver.modules.quota.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fileserver.modules.quota.entity.FsUserQuota;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserQuotaMapper extends BaseMapper<FsUserQuota> {
}
