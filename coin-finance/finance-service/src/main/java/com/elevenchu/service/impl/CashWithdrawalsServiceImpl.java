package com.elevenchu.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.*;
import com.elevenchu.dto.UserBankDto;
import com.elevenchu.dto.UserDto;
import com.elevenchu.feign.UserBankServiceFeign;
import com.elevenchu.feign.UserServiceFeign;
import com.elevenchu.mapper.CashWithdrawAuditRecordMapper;
import com.elevenchu.model.CashSellParam;
import com.elevenchu.service.AccountService;
import com.elevenchu.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.CashWithdrawalsMapper;
import com.elevenchu.service.CashWithdrawalsService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class CashWithdrawalsServiceImpl extends ServiceImpl<CashWithdrawalsMapper, CashWithdrawals> implements CashWithdrawalsService{
    @Autowired
    private UserServiceFeign userServiceFeign;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AccountService accountService;
    @Autowired
    private CashWithdrawAuditRecordMapper cashWithdrawAuditRecordMapper;
    @Autowired
    private ConfigService configService;
    @Autowired
    private UserBankServiceFeign userBankServiceFeign;

    @CreateCache(name = "CASH_WITHDRAWALS_LOCK:", expire = 100, timeUnit = TimeUnit.SECONDS, cacheType = CacheType.BOTH)
    private Cache<String, String> lock;
    @Override
    public Page<CashWithdrawals> findByPage(Page<CashWithdrawals> page, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime) {
// 有用户的信息时
        Map<Long, UserDto> basicUsers = null;
        LambdaQueryWrapper<CashWithdrawals> cashWithdrawalsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (userId != null || !StringUtils.isEmpty(userName) || !StringUtils.isEmpty(mobile)) {
            basicUsers = userServiceFeign.getBasicUsers(userId == null ? null : Arrays.asList(userId), userName, mobile);
            if (CollectionUtils.isEmpty(basicUsers)) {
                return page;
            }
            Set<Long> userIds = basicUsers.keySet();
            cashWithdrawalsLambdaQueryWrapper.in(CashWithdrawals::getUserId, userIds);
        }
        // 其他的查询信息
        cashWithdrawalsLambdaQueryWrapper.eq(status != null, CashWithdrawals::getStatus, status)
                .between(
                        !(StringUtils.isEmpty(numMin) || StringUtils.isEmpty(numMax)),
                        CashWithdrawals::getNum,
                        new BigDecimal(StringUtils.isEmpty(numMin) ? "0" : numMin),
                        new BigDecimal(StringUtils.isEmpty(numMax) ? "0" : numMax)
                )
                .between(
                        !(StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)),
                        CashWithdrawals::getCreated,
                        startTime, endTime + " 23:59:59"
                );
        Page<CashWithdrawals> pageDate = page(page, cashWithdrawalsLambdaQueryWrapper);
        List<CashWithdrawals> records = pageDate.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Long> userIds = records.stream().map(CashWithdrawals::getUserId).collect(Collectors.toList());
            if (basicUsers == null) {
                basicUsers = userServiceFeign.getBasicUsers(userIds, null, null);
            }
            Map<Long, UserDto> finalBasicUsers = basicUsers;
            records.forEach(cashWithdrawals -> {
                UserDto userDto = finalBasicUsers.get(cashWithdrawals.getUserId());
                if (userDto != null) {
                    cashWithdrawals.setUsername(userDto.getUsername());
                    cashWithdrawals.setRealName(userDto.getRealName());
                }
            });
        }
        return pageDate;
    }

    /**
     * 审核提现记录
     *
     * @param userId
     * @param cashWithdrawAuditRecord
     * @return
     */
    @Override
    public boolean updateWithdrawalsStatus(Long userId, CashWithdrawAuditRecord cashWithdrawAuditRecord) {
        // 1 使用锁锁住
        boolean isOk = lock.tryLockAndRun(cashWithdrawAuditRecord.getId() + "", 300, TimeUnit.SECONDS, () -> {
            CashWithdrawals cashWithdrawals = getById(cashWithdrawAuditRecord.getId());
            if (cashWithdrawals == null) {
                throw new IllegalArgumentException("现金的审核记录不存在");
            }

            // 2 添加一个审核的记录
            CashWithdrawAuditRecord cashWithdrawAuditRecordNew = new CashWithdrawAuditRecord();
            cashWithdrawAuditRecordNew.setAuditUserId(userId);
            cashWithdrawAuditRecordNew.setRemark(cashWithdrawAuditRecord.getRemark());
            cashWithdrawAuditRecordNew.setCreated(new Date());
            cashWithdrawAuditRecordNew.setStatus(cashWithdrawAuditRecord.getStatus());
            Integer step = cashWithdrawals.getStep() + 1;
            cashWithdrawAuditRecordNew.setStep(step.byteValue());
            cashWithdrawAuditRecordNew.setOrderId(cashWithdrawals.getId());

            // 记录保存成功
            int count = cashWithdrawAuditRecordMapper.insert(cashWithdrawAuditRecordNew);
            if (count > 0) {
                cashWithdrawals.setStatus(cashWithdrawAuditRecord.getStatus());
                cashWithdrawals.setRemark(cashWithdrawAuditRecord.getRemark());
                cashWithdrawals.setLastTime(new Date());
                cashWithdrawals.setAccountId(userId); //
                cashWithdrawals.setStep(step.byteValue());
                boolean updateById = updateById(cashWithdrawals);   // 审核拒绝
                if (updateById) {
                    // 审核通过 withdrawals_out
                    Boolean isPass = accountService.decreaseAccountAmount(
                            userId, cashWithdrawals.getUserId(), cashWithdrawals.getCoinId(),
                            cashWithdrawals.getId(), cashWithdrawals.getNum(), cashWithdrawals.getFee(),
                            cashWithdrawals.getRemark(), "withdrawals_out", (byte) 2
                    );
                }
            }
        });

        return isOk;
    }

    @Override
    public Page<CashWithdrawals> findCashWithdrawals(Page<CashWithdrawals> page, Long userId, Byte status) {
        return page(page, new LambdaQueryWrapper<CashWithdrawals>()
                .eq(CashWithdrawals::getUserId, userId)
                .eq(status != null, CashWithdrawals::getStatus, status)
        );
    }

    /**
     * GCN卖出
     * @param userId
     * @param cashSellParam
     * @return
     */
    @Override
    public boolean sell(Long userId, CashSellParam cashSellParam) {
        //1.参数校验
        checkCashSellParam(cashSellParam);

        Map<Long, UserDto> basicUsers = userServiceFeign.getBasicUsers(Arrays.asList(userId), null, null);
        if (CollectionUtils.isEmpty(basicUsers)) {
            throw new IllegalArgumentException("用户的id错误");
        }
        UserDto userDto = basicUsers.get(userId);

        //手机验证码
        validatePhoneCode(userDto.getMobile(),cashSellParam.getValidateCode()) ;

        //支付密码验证
        checkUserPayPassword(userDto.getPaypassword(),cashSellParam.getPayPassword());

        //2.远程调用查询用户的银行卡
        UserBankDto userBankInfo = userBankServiceFeign.getUserBankInfo(userId);
        if (userBankInfo == null) {
            throw new IllegalArgumentException("该用户暂未绑定银行卡信息");
        }
        String remark = RandomUtil.randomNumbers(6);

        // 3 通过数量得到本次交易的金额
        BigDecimal amount = getCashWithdrawalsAmount(cashSellParam.getNum());
        // 4 计算本次的手续费
        BigDecimal fee = getCashWithdrawalsFee(amount);
        // 5查询用户的账号ID
        Account account = accountService.findByUserAndCoin(userId, "GCN");
        // 6 订单的创建
        CashWithdrawals cashWithdrawals = new CashWithdrawals();
        cashWithdrawals.setUserId(userId);
        cashWithdrawals.setAccountId(account.getId());
        cashWithdrawals.setCoinId(cashSellParam.getCoinId());
        cashWithdrawals.setStatus((byte) 0);
        cashWithdrawals.setStep((byte) 1);
        cashWithdrawals.setNum(cashSellParam.getNum());
        cashWithdrawals.setMum(amount.subtract(fee)); // 实际金额 = amount-fee
        cashWithdrawals.setFee(fee);
        cashWithdrawals.setBank(userBankInfo.getBank());
        cashWithdrawals.setBankCard(userBankInfo.getBankCard());
        cashWithdrawals.setBankAddr(userBankInfo.getBankAddr());
        cashWithdrawals.setBankProv(userBankInfo.getBankProv());
        cashWithdrawals.setBankCity(userBankInfo.getBankCity());
        cashWithdrawals.setTruename(userBankInfo.getRealName());
        cashWithdrawals.setRemark(remark);
        boolean save = save(cashWithdrawals);
        if(save){
            accountService.lockUserAmount(userId, cashWithdrawals.getCoinId(), cashWithdrawals.getMum(), "withdrawals_out", cashWithdrawals.getId(), cashWithdrawals.getFee());
        }

        return save;
    }

    private BigDecimal getCashWithdrawalsFee(BigDecimal amount) {
        // 1 通过总金额* 费率 = 手续费
        // 2 若金额较小---->最小的提现的手续费

        // 最小的提现费用
        Config withdrawMinPoundage = configService.getConfigByCode("WITHDRAW_MIN_POUNDAGE");
        BigDecimal withdrawMinPoundageFee = new BigDecimal(withdrawMinPoundage.getValue());

        // 提现的费率
        Config withdrawPoundageRate = configService.getConfigByCode("WITHDRAW_POUNDAGE_RATE");


        // 通过费率计算的手续费
        BigDecimal poundageFee = amount.multiply(new BigDecimal(withdrawPoundageRate.getValue())).setScale(2, RoundingMode.HALF_UP);

        //min 取2 个的最小值
        return poundageFee.min(withdrawMinPoundageFee).equals(poundageFee) ? withdrawMinPoundageFee : poundageFee;
    }


    //通过数量计算金额
    private BigDecimal getCashWithdrawalsAmount(BigDecimal num) {
        Config rateConfig = configService.getConfigByCode("USDT2CNY");
        return num.multiply(new BigDecimal(rateConfig.getValue())).setScale(2, RoundingMode.HALF_UP);

    }

    private void checkUserPayPassword(String paypassword, String payPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(payPassword, paypassword);

        if (!matches){
            throw new IllegalArgumentException("支付密码错误");
        }

    }

    /**
     * 检验用户的手机验证码
     * @param mobile
     * @param validateCode
     */
    private void validatePhoneCode(String mobile, String validateCode) {
        // 验证:SMS:CASH_WITHDRAWS:mobile
        String code = redisTemplate.opsForValue().get("SMS:CASH_WITHDRAWS:" + mobile);
        if (!validateCode.equals(code)) {
            throw new IllegalArgumentException("验证码错误");
        }

    }



    //参数校验
    private void checkCashSellParam(CashSellParam cashSellParam) {
        //1.提现状态
        Config cashWithdrawalsStatus = configService.getConfigByCode("WITHDRAW_STATUS");
        if (Integer.valueOf(cashWithdrawalsStatus.getValue()) != 1) {
            throw new IllegalArgumentException("提现暂未开启");
        }
        //2.提现的金额
         BigDecimal cashSellParamNum = cashSellParam.getNum();
        // 2.1 最小的提现额度100
        Config cashWithdrawalsConfigMin = configService.getConfigByCode("WITHDRAW_MIN_AMOUNT");
        //101
        if (cashSellParamNum.compareTo(new BigDecimal(cashWithdrawalsConfigMin.getValue())) < 0) {
            throw new IllegalArgumentException("检查提现的金额");
        }
        // 2.1 最大的提现额度
        Config cashWithdrawalsConfigMax = configService.getConfigByCode("WITHDRAW_MAX_AMOUNT");
        if (cashSellParamNum.compareTo(new BigDecimal(cashWithdrawalsConfigMax.getValue())) >= 0) {
            throw new IllegalArgumentException("检查提现的金额");
        }
    }
}
