package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.dto.UserDto;
import com.elevenchu.feign.UserServiceFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.AccountDetailMapper;
import com.elevenchu.domain.AccountDetail;
import com.elevenchu.service.AccountDetailService;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class AccountDetailServiceImpl extends ServiceImpl<AccountDetailMapper, AccountDetail> implements AccountDetailService{
    @Autowired
    private UserServiceFeign userServiceFeign;

    @Override
    public Page<AccountDetail> findByPage(Page<AccountDetail> page, Long coinId, Long accountId, Long userId, String userName, String mobile, String amountStart, String amountEnd, String startTime, String endTime) {
        LambdaQueryWrapper<AccountDetail> accountDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        Map<Long, UserDto> basicUsers = null;
        // 用户的查询
        if (userId != null || !StringUtils.isEmpty(userName) || !StringUtils.isEmpty(mobile)) {
            basicUsers = userServiceFeign.getBasicUsers(userId == null ? null : Arrays.asList(userId), userName, mobile);
            if (CollectionUtils.isEmpty(basicUsers)) {
                return page;
            }
            Set<Long> userIds = basicUsers.keySet();// 用户的Id 集合
            accountDetailLambdaQueryWrapper.in(AccountDetail::getUserId, userIds);
        }
        accountDetailLambdaQueryWrapper.eq(accountId != null, AccountDetail::getAccountId, accountId)
                .eq(coinId != null, AccountDetail::getCoinId, coinId)
                .between(
                        !(StringUtils.isEmpty(amountStart) || StringUtils.isEmpty(amountEnd)),
                        AccountDetail::getAmount,
                        new BigDecimal(StringUtils.isEmpty(amountStart) ? "0" : amountStart), new BigDecimal(StringUtils.isEmpty(amountEnd) ? "0" : amountEnd)
                )
                .between(
                        !(StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)),
                        AccountDetail::getCreated,
                        startTime, endTime + " 23:59:59"
                );
        Page<AccountDetail> accountDetailPage = page(page, accountDetailLambdaQueryWrapper);
        List<AccountDetail> records = accountDetailPage.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<Long> userIds = records.stream().map(AccountDetail::getUserId).collect(Collectors.toList());
            if (basicUsers == null) {
                basicUsers = userServiceFeign.getBasicUsers(userIds, null, null);
            }
            Map<Long, UserDto> finalBasicUsers = basicUsers;
            records.forEach(accountDetail -> {
                UserDto userDto = finalBasicUsers.get(accountDetail.getUserId());
                if (userDto != null) {
                    accountDetail.setUsername(userDto.getUsername());
                    accountDetail.setRealName(userDto.getRealName());
                }

            });
        }
        return accountDetailPage;
    }
}
