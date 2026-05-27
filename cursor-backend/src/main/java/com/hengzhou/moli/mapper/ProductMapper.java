package com.hengzhou.moli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hengzhou.moli.entity.Product;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 物理删除商品（绕过逻辑删除）
     * @param id 商品ID
     * @return 影响行数
     */
    @Delete("DELETE FROM product WHERE id = #{id}")
    int deleteByIdPhysical(@Param("id") Long id);
}
