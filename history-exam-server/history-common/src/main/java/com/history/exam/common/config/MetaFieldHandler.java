package com.history.exam.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.history.exam.common.constant.CommonConstants;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充处理器
 * <p>插入时填充 createdAt / updatedAt / deleted(默认 0)；更新时刷新 updatedAt。</p>
 */
@Component
public class MetaFieldHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充：createdAt、updatedAt、deleted
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "deleted", Integer.class, CommonConstants.NOT_DELETED);
    }

    /**
     * 更新时自动填充：updatedAt
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
