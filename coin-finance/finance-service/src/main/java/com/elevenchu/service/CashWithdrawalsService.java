package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.CashWithdrawAuditRecord;
import com.elevenchu.domain.CashWithdrawals;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elevenchu.model.CashSellParam;

public interface CashWithdrawalsService extends IService<CashWithdrawals>{

    //提现记录查询
    Page<CashWithdrawals> findByPage(Page<CashWithdrawals> page, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime);

    boolean updateWithdrawalsStatus(Long userId, CashWithdrawAuditRecord cashWithdrawAuditRecord);

    Page<CashWithdrawals> findCashWithdrawals(Page<CashWithdrawals> page, Long userId, Byte status);

    boolean sell(Long userId, CashSellParam cashSellParam);
}
