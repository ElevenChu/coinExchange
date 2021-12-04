package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.User;
import com.elevenchu.service.UserBankService;
import com.elevenchu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.UserBankMapper;
import com.elevenchu.domain.UserBank;



@Service
public class UserBankServiceImpl extends ServiceImpl<UserBankMapper, UserBank> implements UserBankService {
    @Autowired
    private UserService userService;

    @Override
    public Page<UserBank> findByPage(Page<UserBank> page, Long usrId) {
        return page(page,new LambdaQueryWrapper<UserBank>()
                .eq(usrId != null ,UserBank::getUserId ,usrId)




        ) ;
    }

    @Override
    public UserBank getCurrentUserBank(Long userId) {

                UserBank userBank= getOne(new LambdaQueryWrapper<UserBank>()
                                .eq(UserBank::getUserId,userId)
                                .eq(UserBank::getStatus,1)

      );


        return userBank;
    }

    @Override
    public boolean bindBank(Long userId, UserBank userBank) {
        // 支付密码的判断
         String payPassword = userBank.getPayPassword();
        User user = userService.getById(userId);
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        if(!bCryptPasswordEncoder.matches(payPassword,user.getPaypassword())){
            throw new IllegalArgumentException("用户的支付密码错误");
        }
        //若有Id则代表是修改操作
        Long id = userBank.getId();
        if(id!=null){
            UserBank userBankDb = getById(id);
            if(userBankDb==null){
                throw new IllegalArgumentException("用户银行卡输入的id有错误");
            }
            return updateById(userBank);
        }
        //若银行卡的ID为null 则需要新建操作
        userBank.setUserId(userId);
        return save(userBank);


    }
}
