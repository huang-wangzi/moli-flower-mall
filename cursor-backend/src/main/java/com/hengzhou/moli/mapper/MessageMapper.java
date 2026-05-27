package com.hengzhou.moli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hengzhou.moli.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息Mapper
 *
 * @author 横州茉莉花
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
