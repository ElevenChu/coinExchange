package com.elevenchu.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.RandomUtil;
import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.CashRechargeAuditRecord;
import com.elevenchu.domain.Coin;
import com.elevenchu.domain.Config;
import com.elevenchu.dto.AdminBankDto;
import com.elevenchu.dto.UserDto;
import com.elevenchu.feign.AdminBankServiceFeign;
import com.elevenchu.feign.UserServiceFeign;
import com.elevenchu.mapper.CashRechargeAuditRecordMapper;
import com.elevenchu.model.CashParam;
import com.elevenchu.service.AccountService;
import com.elevenchu.service.CoinService;
import com.elevenchu.service.ConfigService;
import com.elevenchu.vo.CashTradeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.CashRechargeMapper;
import com.elevenchu.domain.CashRecharge;
import com.elevenchu.service.CashRechargeService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class CashRechargeServiceImpl extends ServiceImpl<CashRechargeMapper, CashRecharge> implements CashRechargeService{
    @Autowired
    private UserServiceFeign userServiceFeign;

    @Autowired
    private ConfigService configService;

    @Autowired
    private Snowflake snowflake;

    @CreateCache(name = "CASH_RECHARGE_LOCK:",expire = 100,timeUnit = TimeUnit.SECONDS,cacheType = CacheType.BOTH)
    private Cache<String,String> cache;

    @Autowired
    private CashRechargeAuditRecordMapper cashRechargeAuditRecordMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AdminBankServiceFeign adminBankServiceFeign;
    @Autowired
    private CoinService coinService;



    @Override
    public Page<CashRecharge> findByPage(Page<CashRecharge> page, Long coinId, Long userId, String userName, String mobile, Byte status, String numMin, String numMax, String startTime, String endTime) {

        LambdaQueryWrapper<CashRecharge> cashRechargeLambdaQueryWrapper = new LambdaQueryWrapper<>();

        // 1 若用户本次的查询中,带了用户的信息userId, userName,mobile ----> 本质就是要把用户的Id 放在我们的查询条件里面
        Map<Long, UserDto> basicUsers = null;
        if (userId != null || !StringUtils.isEmpty(userName) | !StringUtils.isEmpty(mobile)) {
            // 需要远程调用查询用户的信息
            basicUsers = userServiceFeign.getBasicUsers(userId == null ? null : Arrays.asList(userId), userName, mobile);
            if (CollectionUtils.isEmpty(basicUsers)) { // 找不到这样的用户->
                return page;
            }
            Set<Long> userIds = basicUsers.keySet();
            cashRechargeLambdaQueryWrapper.in(!CollectionUtils.isEmpty(userIds), CashRecharge::getUserId, userIds);

        }
        // 2 若用户本次的查询中,没有带了用户的信息
        cashRechargeLambdaQueryWrapper.eq(coinId != null, CashRecharge::getCoinId, coinId)
                .eq(status != null, CashRecharge::getStatus, status)
                .between(!(StringUtils.isEmpty(numMin)||StringUtils.isEmpty(numMax)),
                        CashRecharge::getNum,
                        new BigDecimal(StringUtils.isEmpty(numMin) ? "0" : numMin),
                        new BigDecimal(StringUtils.isEmpty(numMax) ? "0" : numMax))
                .between(!(StringUtils.isEmpty(startTime)||StringUtils.isEmpty(endTime)),
                        CashRecharge::getCreated,
                        startTime,endTime+ " 23:59:59");

        Page<CashRecharge> cashRechargePage = page(page, cashRechargeLambdaQueryWrapper);
        List<CashRecharge> records = cashRechargePage.getRecords();
        if (!CollectionUtils.isEmpty(records)){
            List<Long> userIds = records.stream().map(CashRecharge::getUserId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(basicUsers)) {
                basicUsers = userServiceFeign.getBasicUsers(userIds, null, null);
            }
            Map<Long, UserDto> finalBasicUsers = basicUsers;
            records.forEach(cashRecharge -> { // 设置用户相关的数据
                UserDto userDto = finalBasicUsers.get(cashRecharge.getUserId());
                if (userDto != null) {
                    cashRecharge.setUsername(userDto.getUsername()); // 远程调用查询用户的信息
                    cashRecharge.setRealName(userDto.getRealName());
                }
            });


        }
        return cashRechargePage;
    }

    /**
     * 现金的充值审核
     * @param userId 审核人
     * @param cashRechargeAuditRecord 审核记录
     * @return 是否成功
     */
    @Override
    public boolean cashRechargeAudit(Long userId, CashRechargeAuditRecord cashRechargeAuditRecord) {
        //1.当一个员工审核时，另一个员工不能再审核
        boolean tryLockAndRun=cache.tryLockAndRun(cashRechargeAuditRecord.getId()+"",300,TimeUnit.SECONDS,()->{
            Long rechageId = cashRechargeAuditRecord.getId();
            CashRecharge cashRecharge = getById(rechageId);
            if (cashRecharge == null) {
                throw new IllegalArgumentException("充值记录不存在");
            }
            Byte status = cashRecharge.getStatus();
            if (status == 1) {
                throw new IllegalArgumentException("充值记录审核已经通过");
            }

            CashRechargeAuditRecord cashRechargeAuditRecordDb = new CashRechargeAuditRecord();
            cashRechargeAuditRecordDb.setAuditUserId(userId);
            cashRechargeAuditRecordDb.setStatus(cashRechargeAuditRecord.getStatus());

            cashRechargeAuditRecordDb.setRemark(cashRechargeAuditRecord.getRemark());
            Integer step = cashRecharge.getStep() + 1;
            cashRechargeAuditRecordDb.setStep(step.byteValue());
         //2.保存审核记录
            int insert = cashRechargeAuditRecordMapper.insert(cashRechargeAuditRecordDb);
            if (insert == 0) {
                throw new IllegalArgumentException("审核记录保存失败");
            }

            cashRecharge.setStatus(cashRechargeAuditRecord.getStatus());
            cashRecharge.setAuditRemark(cashRechargeAuditRecord.getRemark());
            cashRecharge.setStep(step.byteValue());
            //管理员没有通过审核
            if (cashRechargeAuditRecord.getStatus() == 2) { // 拒绝
                updateById(cashRecharge);
            } else {
                // 管理员通过审核 ,给用户的账户充值

                // 用户的余额增加
                Boolean isOk = accountService.transferAccountAmount(userId,cashRecharge.getUserId(), cashRecharge.getCoinId(), cashRecharge.getId(), cashRecharge.getNum(), cashRecharge.getFee(),
                        "充值", "recharge_into",(byte)1);
                if (isOk) {
                    cashRecharge.setLastTime(new Date()); // 设置完成时间
                    updateById(cashRecharge);
                }
            }
        });





        return tryLockAndRun;
    }

    @Override
    public Page<CashRecharge> findUserCashRecharge(Page<CashRecharge> page, Long userId, Byte status) {
        return page(page, new LambdaQueryWrapper<CashRecharge>()
                .eq(CashRecharge::getUserId, userId)
                .eq(status != null, CashRecharge::getStatus, status)
        );
    }

    @Override
    public CashTradeVo buy(Long userId, CashParam cashParam) {
        //1.校验现金参数
        checkCashParam(cashParam);
        //2.远程调用 查询公司的银行卡
        List<AdminBankDto> allAdminBanks = adminBankServiceFeign.getAllAdminBanks();
        //只需一张银行卡
        AdminBankDto adminBankDto=loadbalancer(allAdminBanks);
        //3.生成订单号/参考号
        String orderNo = String.valueOf(snowflake.nextId());
        String remark = RandomUtil.randomNumbers(6);
        Coin coin = coinService.getById(cashParam.getCoinId());

        if (coin == null) {
            throw new IllegalArgumentException("coinId不存在");
        }

        Config buyGCNRate = configService.getConfigByCode("CNY2USDT");
        BigDecimal realMum = cashParam.getMum().multiply(new BigDecimal(buyGCNRate.getValue())).setScale(2, RoundingMode.HALF_UP);
        //4.在数据库插入一条充值记录
        CashRecharge cashRecharge = new CashRecharge();
        cashRecharge.setUserId(userId);
        // 银行卡的信息
        cashRecharge.setName(adminBankDto.getName());
        cashRecharge.setBankName(adminBankDto.getBankName());
        cashRecharge.setBankCard(adminBankDto.getBankCard());

        cashRecharge.setTradeno(orderNo);
        cashRecharge.setCoinId(cashParam.getCoinId());
        cashRecharge.setCoinName(coin.getName());
        cashRecharge.setNum(cashParam.getNum());
        cashRecharge.setMum(realMum); // 实际的交易金额
        cashRecharge.setRemark(remark);
        cashRecharge.setFee(BigDecimal.ZERO);
        cashRecharge.setType("linepay"); // 在线支付
        cashRecharge.setStatus((byte) 0); // 待审核
        cashRecharge.setStep((byte) 1);// 第一步
        boolean save = save(cashRecharge);
        if (save) {
            // 5.返回我们的成功对象
            CashTradeVo cashTradeVo = new CashTradeVo();
            // 我们收户的银行卡信息
            cashTradeVo.setAmount(realMum);
            cashTradeVo.setStatus((byte) 0);
            cashTradeVo.setName(adminBankDto.getName());
            cashTradeVo.setBankName(adminBankDto.getBankName());
            cashTradeVo.setBankCard(adminBankDto.getBankCard());
            cashTradeVo.setRemark(remark);
            return cashTradeVo;
        }
        return null;
    }

    /**
     * 简易负载均衡
     * @param allAdminBanks
     * @return
     */
    private AdminBankDto loadbalancer(List<AdminBankDto> allAdminBanks) {
        if (CollectionUtils.isEmpty(allAdminBanks)){
        throw new RuntimeException("没有发现可用的银行卡");
    }
        int size = allAdminBanks.size();
        if(size==1){
            return allAdminBanks.get(0);
        }
        Random random=new Random();
        return allAdminBanks.get(random.nextInt(allAdminBanks.size()));

    }

    private void checkCashParam(CashParam cashParam){
         BigDecimal num = cashParam.getNum(); // 现金充值的数量
        Config withDrowConfig = configService.getConfigByCode("WITH_DROW");
         String value = withDrowConfig.getValue();
        BigDecimal minRecharge = new BigDecimal(value);
        if (num.compareTo(minRecharge) < 0) {
            throw new IllegalArgumentException("充值数量太小");
        }
    }
}
