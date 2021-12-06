package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.CoinType;
import com.baomidou.mybatisplus.extension.service.IService;
public interface CoinTypeService extends IService<CoinType>{


    Page<CoinType> findByPage(Page<CoinType> page, String code);
}
