package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elevenchu.domain.Config;

public interface ConfigService extends IService<Config>{


    Page<Config> findByPage(Page<Config> page, String type, String name, String code);

    Config getConfigByCode(String sign);
}
