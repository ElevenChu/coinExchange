package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.User;
import com.elevenchu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.UserWalletMapper;
import com.elevenchu.domain.UserWallet;
import com.elevenchu.service.UserWalletService;
import org.springframework.util.StringUtils;

@Service
public class UserWalletServiceImpl extends ServiceImpl<UserWalletMapper, UserWallet> implements UserWalletService{
    @Autowired
    private UserService userService;
    @Override
    public Page<UserWallet> findByPage(Page<UserWallet> page, Long userId) {
        return page(page, new LambdaQueryWrapper<UserWallet>().eq(UserWallet::getUserId ,userId));
    }

    @Override
    public List<UserWallet> findUserWallets(Long userId, Long coinId) {
        return list(new LambdaQueryWrapper<UserWallet>().eq(UserWallet::getCoinId,coinId).eq(UserWallet::getUserId,userId));
    }
    @Override
    public boolean save(UserWallet entity) {
        Long userId = entity.getUserId();
        User user = userService.getById(userId);
        if(user==null){
            throw new IllegalArgumentException("该用户不存在") ;
        }
        String paypassword = user.getPaypassword();
        if(StringUtils.isEmpty(paypassword) ||  !(new BCryptPasswordEncoder().matches(entity.getPayPassword(),paypassword))){
            throw new IllegalArgumentException("交易密码错误") ;
        }
        return super.save(entity);
    }

    @Override
    public boolean deleteUserWallet(Long addressId, String payPassword) {
        UserWallet userWallet = getById(addressId);
        if(userWallet==null){
            throw new IllegalArgumentException("提现地址错误");
        }
        Long userId = userWallet.getUserId();
        User user = userService.getById(userId);
        if(user==null){
            throw new IllegalArgumentException("用户不存在");
        }
        String paypassword = user.getPaypassword();
        if(StringUtils.isEmpty(payPassword)||!(new BCryptPasswordEncoder().matches(payPassword,paypassword))){
            throw new IllegalArgumentException("交易密码错误");
        }

        return super.removeById(addressId);
    }
}
