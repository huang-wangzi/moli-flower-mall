package com.hengzhou.moli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hengzhou.moli.entity.ChatRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天记录Mapper
 */
@Mapper
public interface ChatRecordMapper extends BaseMapper<ChatRecord> {
}
