package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.UserWalletMapper;
import com.elevenchu.domain.UserWallet;
import com.elevenchu.service.UserWalletService;
@Service
public class UserWalletServiceImpl extends ServiceImpl<UserWalletMapper, UserWallet> implements UserWalletService{

    @Override
    public Page<UserWallet> findByPage(Page<UserWallet> page, Long userId) {
        return page(page, new LambdaQueryWrapper<UserWallet>().eq(UserWallet::getUserId ,userId));
    }

    @Override
    public List<UserWallet> findUserWallets(Long userId, Long coinId) {
        return list(new LambdaQueryWrapper<UserWallet>().eq(UserWallet::getCoinId,coinId).eq(UserWallet::getUserId,userId));
    }
}
