package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.AdminBank;
import com.baomidou.mybatisplus.extension.service.IService;
public interface AdminBankService extends IService<AdminBank>{


    Page<AdminBank> findByPage(Page<AdminBank> page, String bankCard);
}
