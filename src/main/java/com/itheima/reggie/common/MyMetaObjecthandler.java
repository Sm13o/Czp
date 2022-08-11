package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充对时间字段的统一管理 需要在封装类这些字段上加上注解  @TableField
 */
@Slf4j
@Component
public class MyMetaObjecthandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
     metaObject.setValue("createTime",LocalDateTime.now());
     metaObject.setValue("updateTime",LocalDateTime.now());
     metaObject.setValue("createUser",BaseContext.getByID());
     metaObject.setValue("updateUser",BaseContext.getByID());
    }

    @Override
    public void updateFill(MetaObject metaObject) {


      metaObject.setValue("updateTime",LocalDateTime.now());
      metaObject.setValue("updateUser",BaseContext.getByID());
    }

}
