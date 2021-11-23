package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.UserAuthInfo;
import com.elevenchu.mapper.UserAuthInfoMapper;
import com.elevenchu.service.UserAuthInfoService;
@Service
public class UserAuthInfoServiceImpl extends ServiceImpl<UserAuthInfoMapper, UserAuthInfo> implements UserAuthInfoService{

    @Override
    public List<UserAuthInfo> getUserAuthInfoByCode(Long authCode) {
        return list(new LambdaQueryWrapper<UserAuthInfo>()
        .eq(UserAuthInfo::getAuthCode,authCode));
    }

    @Override
    public List<UserAuthInfo> getUserAuthInfoByUserId(Long id) {
        List<UserAuthInfo> list=list(new LambdaQueryWrapper<UserAuthInfo>()
                .eq(UserAuthInfo::getUserId,id)
        );
        return list==null? Collections.emptyList():list;//处理空值
    }
}
