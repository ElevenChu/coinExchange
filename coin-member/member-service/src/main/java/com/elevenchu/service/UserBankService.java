package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.UserBank;
import com.baomidou.mybatisplus.extension.service.IService;
public interface UserBankService extends IService<UserBank>{


    Page<UserBank> findByPage(Page<UserBank> page, Long usrId);

    UserBank getCurrentUserBank(Long userId);

    boolean bindBank(Long userId, UserBank userBank);
}
