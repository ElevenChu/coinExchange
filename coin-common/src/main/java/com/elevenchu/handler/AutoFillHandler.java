package com.elevenchu.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AutoFillHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        /**
         * 插入元对象字段填充
         * 新增数据时要添加的为
         * 1创建人
         * 2创建时间
         * 3lastupdatetime
         */

        Long userId=getCurrentUserId();
        this.strictInsertFill(metaObject,"createBy",Long.class,userId);//创建人
        this.strictInsertFill(metaObject,"created", Date.class,new Date());
        this.strictInsertFill(metaObject,"lastUpdateTime",Date.class,new Date());



    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();//从安全的上下文中获取用户ID
        if(authentication!=null){
            String s = authentication.getPrincipal().toString();//s即为String版的用户ID
          if("anonymousUser".equals(s)){
              return null;
          }
            return Long.valueOf(s);//用户ID转成Long

        }

            return null;
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId=getCurrentUserId();
        this.strictUpdateFill(metaObject,"modifyBy",Long.class,userId);
        this.strictUpdateFill(metaObject,"lastUpdateTime",Date.class,new Date());
    }
}
