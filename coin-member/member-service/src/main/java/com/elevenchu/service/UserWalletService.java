package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.UserWallet;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserWalletService extends IService<UserWallet>{


    Page<UserWallet> findByPage(Page<UserWallet> page, Long userId);

    List<UserWallet> findUserWallets(Long userId, Long coinId);
    boolean save(UserWallet entity);

    boolean deleteUserWallet(Long addressId, String payPassword);
}
