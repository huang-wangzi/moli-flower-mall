package com.hengzhou.moli.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充配置
 */
@Component
public class MyBatisPlusConfig implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        // 插入时无条件设置所有时间字段
        setFieldValByName("createTime", now, metaObject);
        setFieldValByName("createdAt", now, metaObject);
        setFieldValByName("createdDate", now, metaObject);
        setFieldValByName("updateTime", now, metaObject);
        setFieldValByName("updatedAt", now, metaObject);
        setFieldValByName("updatedDate", now, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        // 更新时无条件设置更新时间字段
        setFieldValByName("updateTime", now, metaObject);
        setFieldValByName("updatedAt", now, metaObject);
        setFieldValByName("updatedDate", now, metaObject);
    }
}
