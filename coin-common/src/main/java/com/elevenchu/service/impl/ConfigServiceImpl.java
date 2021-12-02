package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.Config;

import com.elevenchu.mapper.ConfigMapper;
import com.elevenchu.service.ConfigService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    @Override
    public Page<Config> findByPage(Page<Config> page, String type, String name, String code) {
        return page(page,new LambdaQueryWrapper<Config>()
                .like(!StringUtils.isEmpty(type),Config::getType,type)
                .like(!StringUtils.isEmpty(name),Config::getName,name)
                .like(!StringUtils.isEmpty(code),Config::getCode,code)
        );
    }

    @Override
    public Config getConfigByCode(String sign) {
        return getOne(new LambdaQueryWrapper<Config>().eq(Config::getCode,sign));
    }
}
