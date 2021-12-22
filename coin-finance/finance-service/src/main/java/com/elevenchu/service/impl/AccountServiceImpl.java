package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elevenchu.domain.AccountDetail;
import com.elevenchu.domain.Coin;
import com.elevenchu.domain.Config;
import com.elevenchu.dto.MarketDto;
import com.elevenchu.feign.MarketServiceFeign;
import com.elevenchu.mappers.AccountVoMappers;
import com.elevenchu.service.AccountDetailService;
import com.elevenchu.service.CoinService;
import com.elevenchu.service.ConfigService;
import com.elevenchu.vo.AccountVo;
import com.elevenchu.vo.UserTotalAccountVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.AccountMapper;
import com.elevenchu.domain.Account;
import com.elevenchu.service.AccountService;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService{

    @Autowired
    private CoinService coinService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private AccountDetailService accountDetailService;

    @Autowired
    private MarketServiceFeign marketServiceFeign;

    @Override
    public Boolean transferAccountAmount(Long adminId, Long userId, Long coinId, Long orderNum, BigDecimal num, BigDecimal fee, String remark, String businessType, Byte direction) {
        Account coinAccount = getCoinAccount(coinId, userId);
        if (coinAccount == null) {
            throw new IllegalArgumentException("用户当前的该币种的余额不存在");
        }
        // 增加一条流水记录
        AccountDetail accountDetail = new AccountDetail();
        accountDetail.setCoinId(coinId);
        accountDetail.setUserId(userId);
        accountDetail.setAmount(num);
        accountDetail.setFee(fee);
        accountDetail.setOrderId(orderNum);
        accountDetail.setAccountId(coinAccount.getId());
        accountDetail.setRefAccountId(coinAccount.getId());
        accountDetail.setRemark(remark);
        accountDetail.setBusinessType(businessType);
        accountDetail.setDirection(direction);
        accountDetail.setCreated(new Date());
        boolean save = accountDetailService.save(accountDetail);
        if (save) { // 用户余额的增加
            coinAccount.setBalanceAmount(coinAccount.getBalanceAmount().add(num));
            boolean updateById = updateById(coinAccount);
            return updateById;
        }
        return save;
    }

    @Override
    public Boolean decreaseAccountAmount(Long adminId, Long userId, Long coinId, Long orderNum, BigDecimal num, BigDecimal fee, String remark, String businessType, Byte direction) {
        Account coinAccount = getCoinAccount(coinId, userId);
        if (coinAccount == null) {
            throw new IllegalArgumentException("账户不存在");
        }
        AccountDetail accountDetail = new AccountDetail();
        accountDetail.setUserId(userId);
        accountDetail.setCoinId(coinId);
        accountDetail.setAmount(num);
        accountDetail.setFee(fee);
        accountDetail.setAccountId(coinAccount.getId());
        accountDetail.setRefAccountId(coinAccount.getId());
        accountDetail.setRemark(remark);
        accountDetail.setBusinessType(businessType);
        accountDetail.setDirection(direction);
        boolean save = accountDetailService.save(accountDetail);
        if (save) { // 新增了流水记录
            BigDecimal balanceAmount = coinAccount.getBalanceAmount();
            BigDecimal result = balanceAmount.add(num.multiply(BigDecimal.valueOf(-1)));
            if (result.compareTo(BigDecimal.ONE) > 0) {
                coinAccount.setBalanceAmount(result);
                return updateById(coinAccount);
            } else {
                throw new IllegalArgumentException("余额不足");
            }
        }
        return false;
    }

    @Override
    public Account findByUserAndCoin(Long userId, String coinName) {
        Coin coin = coinService.getCoinByCoinName(coinName);
        if (coin == null) {
            throw new IllegalArgumentException("货币不存在");
        }
        Account account=getOne(new LambdaQueryWrapper<Account>().eq(Account::getCoinId,coin.getId())
                                                                .eq(Account::getUserId,userId));
        if (account == null) {
            throw new IllegalArgumentException("该资产不存在");
        }

        Config sellRateConfig = configService.getConfigByCode("USDT2CNY");
        account.setSellRate(new BigDecimal(sellRateConfig.getValue())); // 出售的费率

        Config setBuyRateConfig = configService.getConfigByCode("CNY2USDT");
        account.setBuyRate(new BigDecimal(setBuyRateConfig.getValue())); // 买进来的费率

        return account;
    }

    @Override
    public void lockUserAmount(Long userId, Long coinId, BigDecimal mum, String type, Long orderId, BigDecimal fee) {
        Account account = getOne(new LambdaQueryWrapper<Account>().eq(Account::getCoinId, coinId).eq(Account::getUserId, userId));
        if (account == null) {
            throw new IllegalArgumentException("您输入的资产类型不存在");
        }
        BigDecimal balanceAmount = account.getBalanceAmount();
        if (balanceAmount.compareTo(mum) < 0) {
            throw new IllegalArgumentException("账号的资金不足");
        }
        account.setBalanceAmount(balanceAmount.subtract(mum));
        account.setFreezeAmount(account.getFreezeAmount().add(mum));
        boolean updateById = updateById(account);
        if (updateById) {  // 增加流水记录
            AccountDetail accountDetail = new AccountDetail(
                    null,
                    userId,
                    coinId,
                    account.getId(),
                    account.getId(), // 如果该订单时邀请奖励,有我们的ref的account ,否则,值和account 是一样的
                    orderId,
                    (byte) 2,
                    type,
                    mum,
                    fee,
                    "用户提现",
                    null,
                    null,
                    null
            );
            accountDetailService.save(accountDetail);
        }
    }

    @Override
    public UserTotalAccountVo getUserTotalAccount(Long userId) {
        // 计算总资产
        UserTotalAccountVo userTotalAccountVo = new UserTotalAccountVo();
        BigDecimal basicCoin2CnyRate = BigDecimal.ONE; // 汇率
        BigDecimal basicCoin = BigDecimal.ZERO; // 平台计算币的基币
        List<AccountVo> assertList = new ArrayList<AccountVo>();
        // 用户的总资产位于Account 里面
        List<Account> accounts = list(new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, userId)
        );
        if (CollectionUtils.isEmpty(accounts)) {
            userTotalAccountVo.setAssertList(assertList);
            userTotalAccountVo.setAmountUs(BigDecimal.ZERO);
            userTotalAccountVo.setAmount(BigDecimal.ZERO);
            return userTotalAccountVo; //
        }
        AccountVoMappers mappers = AccountVoMappers.INSTANCE;
        // 获取所有的币种
        for (Account account : accounts) {
            AccountVo accountVo = mappers.toConvertVo(account);
            Long coinId = account.getCoinId();
            Coin coin = coinService.getById(coinId);
            if (coin == null || coin.getStatus() != (byte) 1) {
                continue;
            }
            // 设置币的信息
            accountVo.setCoinName(coin.getName());
            accountVo.setCoinImgUrl(coin.getImg());
            accountVo.setCoinType(coin.getType());
            accountVo.setWithdrawFlag(coin.getWithdrawFlag());
            accountVo.setRechargeFlag(coin.getRechargeFlag());
            accountVo.setFeeRate(BigDecimal.valueOf(coin.getRate()));
            accountVo.setMinFeeNum(coin.getMinFeeNum());

            assertList.add(accountVo);
            // 计算总的账面余额 //
            BigDecimal volume = accountVo.getBalanceAmount().add(accountVo.getFreezeAmount());
            accountVo.setCarryingAmount(volume); // 总的账面余额
            // 将该币和我们系统统计币使用的基币转化
            BigDecimal currentPrice = getCurrentCoinPrice(coinId);

            BigDecimal total = volume.multiply(currentPrice);
            basicCoin = basicCoin.add(total); // 将该子资产添加到我们的总资产里面
        }

        userTotalAccountVo.setAmount(basicCoin.multiply(basicCoin2CnyRate).setScale(8, RoundingMode.HALF_UP)); // 总的人民币
        userTotalAccountVo.setAmountUs(basicCoin); // 总的平台计算的币种(基础币)
        userTotalAccountVo.setAmountUsUnit("GCN");
        userTotalAccountVo.setAssertList(assertList);
        return userTotalAccountVo;
    }




    private BigDecimal getCurrentCoinPrice(Long coinId) {
        // 1 查询我们的基础币是什么?
        Config configBasicCoin = configService.getConfigByCode("PLATFORM_COIN_ID"); // 基础币
        if (configBasicCoin == null) {
            throw new IllegalArgumentException("请配置基础币后使用");
        }
        Long basicCoinId = Long.valueOf(configBasicCoin.getValue());
        if (coinId.equals(basicCoinId)) { // 该币就是基础币
            return BigDecimal.ONE;
        }
        // 不等于,我们需要查询交易市场   ,使用基础币作为我们报价货币,使用报价货币的的金额 来计算我们的当前币的价格
        MarketDto market = marketServiceFeign.findByCoinId(basicCoinId, coinId);
        if (market != null) { // 存在交易对
            return market.getOpenPrice();
        } else {
            // 该交易对不存在?
            log.error("不存在当前币和平台币兑换的市场,请后台人员及时添加");
            return BigDecimal.ZERO;//TODO
        }
    }

    /**
     * 获取用户的某种币的资产
     *
     * @param coinId
     * @param userId
     * @return
     */
    private Account getCoinAccount(Long coinId, Long userId) {

        return getOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getCoinId, coinId)
                .eq(Account::getUserId, userId)
                .eq(Account::getStatus, 1)
        );
    }

}
