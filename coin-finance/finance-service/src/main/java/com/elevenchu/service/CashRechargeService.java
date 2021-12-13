package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.CashRecharge;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elevenchu.domain.CashRechargeAuditRecord;
import com.elevenchu.model.CashParam;
import com.elevenchu.vo.CashTradeVo;

public interface CashRechargeService extends IService<CashRecharge>{


    Page<CashRecharge> findByPage(Page<CashRecharge> page, Long coinId, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime);

    boolean cashRechargeAudit(Long userId, CashRechargeAuditRecord cashRechargeAuditRecord);

    Page<CashRecharge> findUserCashRecharge(Page<CashRecharge> page, Long userId, Byte status);

    CashTradeVo buy(Long userId, CashParam cashParam);
}
