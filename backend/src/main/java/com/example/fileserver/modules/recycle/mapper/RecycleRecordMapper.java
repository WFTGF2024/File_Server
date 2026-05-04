package com.example.fileserver.modules.recycle.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.fileserver.modules.recycle.entity.FsRecycleRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RecycleRecordMapper extends BaseMapper<FsRecycleRecord> {
}
