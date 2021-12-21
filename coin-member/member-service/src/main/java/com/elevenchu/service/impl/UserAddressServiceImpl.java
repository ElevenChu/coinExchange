package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.UserAddressMapper;
import com.elevenchu.domain.UserAddress;
import com.elevenchu.service.UserAddressService;
@Service
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService{

    @Override
    public Page<UserAddress> findByPage(Page<UserAddress> page, Long userId) {
        return page(page,new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId,userId)
        );
    }

    @Override
    public UserAddress getUserAddressByUserIdAndCoinId(String userId, Long coinId) {
        return  getOne(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getCoinId,coinId)
        .eq(UserAddress::getUserId,userId));
    }

    @Override
    public List<UserAddress> getUserAddressByUserId(Long userId) {
        return list(new LambdaQueryWrapper<UserAddress>().eq(UserAddress::getUserId,userId).orderByDesc(UserAddress::getCreated));
    }
}
