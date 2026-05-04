package com.example.fileserver.modules.policy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fileserver.modules.policy.entity.FsUserCategoryPolicy;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCategoryPolicyMapper extends BaseMapper<FsUserCategoryPolicy> {
}
