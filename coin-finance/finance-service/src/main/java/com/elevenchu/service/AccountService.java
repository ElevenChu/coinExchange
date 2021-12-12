package com.elevenchu.service;

import com.elevenchu.domain.Account;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

public interface AccountService extends IService<Account>{


    Boolean transferAccountAmount(Long adminId, Long userId, Long coinId, Long id, BigDecimal num, BigDecimal fee, String remark, String businessType, Byte direction);
}
