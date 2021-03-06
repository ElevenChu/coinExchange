package com.elevenchu.service;

import com.elevenchu.domain.Account;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elevenchu.vo.UserTotalAccountVo;

import java.math.BigDecimal;

public interface AccountService extends IService<Account>{


    Boolean transferAccountAmount(Long adminId, Long userId, Long coinId, Long orderNum, BigDecimal num, BigDecimal fee, String remark, String businessType, Byte direction);

    Boolean decreaseAccountAmount(Long adminId, Long userId, Long coinId, Long orderNum, BigDecimal num, BigDecimal fee, String remark, String businessType, Byte direction);

    Account findByUserAndCoin(Long userId, String coinName);

    void lockUserAmount(Long userId, Long coinId, BigDecimal mum, String type, Long orderId, BigDecimal fee);

    UserTotalAccountVo getUserTotalAccount(Long userId);

    void transferBuyAmount(Long fromUserId, Long toUserId, Long coinId, BigDecimal amount, String businessType, Long orderId);

    void transferSellAmount(Long fromUserId, Long toUserId, Long coinId, BigDecimal amount, String businessType, Long orderId);
}
