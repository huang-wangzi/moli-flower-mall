package com.hengzhou.moli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hengzhou.moli.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户Mapper接口
 *
 * @author 横州茉莉花
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Update("UPDATE sys_user SET shop_qualification_status = #{status} WHERE id = #{userId}")
    void updateQualificationStatus(@Param("userId") Long userId, @Param("status") Integer status);
}
