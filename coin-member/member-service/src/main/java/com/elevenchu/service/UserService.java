package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elevenchu.model.UnSetPayPasswordParam;
import com.elevenchu.model.UpdateLoginParam;
import com.elevenchu.model.UpdatePhoneParam;
import com.elevenchu.model.UserAuthForm;

import java.util.List;

public interface UserService extends IService<User>{


    Page<User> findByPage(Page<User> page, String mobile, Long userId, String userName, String realName, Integer status,Integer reviewStatus);

    Page<User> findDirectInvitePage(Page<User> page, Long userId);

    void updateUserAuthStatus(Long id, Byte authStatus, Long authCode, String remark);

    boolean identifyVerfiy(Long valueOf, UserAuthForm userAuthForm);

    void authUser(Long id, List<String> asList);

    boolean updatePhone(Long id,UpdatePhoneParam updatePhoneParam);

    boolean checkNewPhone(String mobile, String countryCode);

    boolean updateUserLoginPwd(Long userId, UpdateLoginParam updateLoginParam);

    boolean updateUserPayPwd(Long userId, UpdateLoginParam updateLoginParam);

    boolean unsetPayPassword(Long userId, UnSetPayPasswordParam unsetPayPasswordParam);
}
