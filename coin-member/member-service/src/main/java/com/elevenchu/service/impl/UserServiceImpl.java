package com.elevenchu.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.config.IdAutoConfiguration;
import com.elevenchu.domain.Sms;
import com.elevenchu.domain.UserAuthAuditRecord;
import com.elevenchu.domain.UserAuthInfo;
import com.elevenchu.dto.UserDto;
import com.elevenchu.geetest.GeetestLib;
import com.elevenchu.mappers.UserDtoMapper;
import com.elevenchu.model.UnSetPayPasswordParam;
import com.elevenchu.model.UpdateLoginParam;
import com.elevenchu.model.UpdatePhoneParam;
import com.elevenchu.model.UserAuthForm;
import com.elevenchu.service.SmsService;
import com.elevenchu.service.UserAuthAuditRecordService;
import com.elevenchu.service.UserAuthInfoService;
import com.elevenchu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.UserMapper;
import com.elevenchu.domain.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;

import static org.graalvm.compiler.debug.DebugOptions.Count;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private GeetestLib geetestLib;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private UserAuthAuditRecordService userAuthAuditRecordService;
    @Autowired
    private Snowflake snowflake;
    @Autowired
    private UserAuthInfoService userAuthInfoService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SmsService smsService;


    @Override
    public Page<User> findByPage(Page<User> page, String mobile, Long userId, String userName, String realName, Integer status, Integer reviewStatus) {
        return page(page, new LambdaQueryWrapper<User>()
                .like(!StringUtils.isEmpty(mobile), User::getMobile, mobile)
                .like(!StringUtils.isEmpty(userName), User::getUsername, userName)
                .like(!StringUtils.isEmpty(realName), User::getRealName, realName)
                .eq(userId != null, User::getId, userId)
                .eq(status != null, User::getStatus, status)
                .eq(reviewStatus != null, User::getReviewsStatus, reviewStatus)

        );
    }

    /**
     * 通过用户的Id 查询该用户邀请的人员
     *
     * @param page   分页参数
     * @param userId 用户的Id
     * @return
     */
    @Override
    public Page<User> findDirectInvitePage(Page<User> page, Long userId) {
        return page(page, new LambdaQueryWrapper<User>().eq(User::getDirectInviteid, userId));
    }

    @Override
    @Transactional
    public void updateUserAuthStatus(Long id, Byte authStatus, Long authCode, String remark) {
        User user = getById(id);
        if (user != null) {
            user.setReviewsStatus(authStatus.intValue());
            updateById(user); // 修改用户的状态
        }
        UserAuthAuditRecord userAuthAuditRecord = new UserAuthAuditRecord();
        userAuthAuditRecord.setUserId(id);
        userAuthAuditRecord.setStatus(authStatus);
        userAuthAuditRecord.setAuthCode(authCode);
        String usrStr = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        userAuthAuditRecord.setAuditUserId(Long.valueOf(usrStr)); // 审核人的ID
        userAuthAuditRecord.setAuditUserName("---------------------------");// 审核人的名称 --> 远程调用admin-service ,没有事务
        userAuthAuditRecord.setRemark(remark);

        userAuthAuditRecordService.save(userAuthAuditRecord);
    }

    @Override
    public boolean identifyVerfiy(Long id, UserAuthForm userAuthForm) {
        User user = getById(id);
        Assert.notNull(user, "认证用户不存在");
        Byte authStatus = user.getAuthStatus();
        if (!authStatus.equals((byte) 0)) {
            throw new IllegalArgumentException("该用户已经认证成功了");

        }
        //执行认证
        checkForm(userAuthForm);//极验

        //实名认证
        boolean check = IdAutoConfiguration.check(userAuthForm.getRealName(), userAuthForm.getIdCard());
        if (!check) {
            throw new IllegalArgumentException("该用户信息错误,请检查");
        }
        //设置用户认证属性
        user.setAuthtime(new Date());
        user.setAuthStatus((byte) 1);
        user.setRealName(userAuthForm.getRealName());
        user.setIdCard(userAuthForm.getIdCard());
        user.setIdCardType(userAuthForm.getIdCardType());

        return updateById(user);
    }


    private void checkForm(UserAuthForm userAuthForm) {
        userAuthForm.check(geetestLib, redisTemplate);


    }


    public User getById(Serializable id) {
        User user = super.getById(id);
        if (user == null) {
            throw new IllegalArgumentException("请输入正确的用户ID");
        }
        Byte seniorAuthStatus = null;//用户的高级认证状态
        String seniorAuthDesc = "";//用户的高级认证未通过，原因
        Integer reviewsStatus = user.getReviewsStatus();//用户被审核状态1通过，2拒绝，0待审核
        if (reviewsStatus == null) {//为null时代表用户的资料没有上传
            seniorAuthStatus = 3;
            seniorAuthDesc = "资料未填写";
        }else {

            switch (reviewsStatus) {
                case 1:
                    seniorAuthStatus = 1;
                    seniorAuthDesc = "审核通过";
                    break;
                case 2:
                    seniorAuthStatus = 2;
                    //查询被拒绝原因-->审核记录里面的
                    //时间排序
                    List<UserAuthAuditRecord> userAuthAuditRecordList = userAuthAuditRecordService.getUserAuthAuditRecordList(user.getId());
                    if (!CollectionUtils.isEmpty(userAuthAuditRecordList)) {
                        UserAuthAuditRecord userAuthAuditRecord = userAuthAuditRecordList.get(0);
                        seniorAuthDesc = userAuthAuditRecord.getRemark();
                    }
                    //eniorAuthDesc = "原因未知";
                    break;
                case 0:
                    seniorAuthStatus = 0;
                    seniorAuthDesc = "等待审核";
                    break;
            }
        }
        user.setSeniorAuthStatus(seniorAuthStatus);
        user.setSeniorAuthDesc(seniorAuthDesc);
        return user;
    }

    @Override
    public void authUser(Long id, List<String> imgs) {
        if (CollectionUtils.isEmpty(imgs)) {
            throw new IllegalArgumentException("用户的身份证信息为null");
        }
        User user = getById(id);
        if (user == null) {
            throw new IllegalArgumentException("请输入正确的userId");
        }
        long authCode = snowflake.nextId(); // 使用时间戳(有重复) --> 雪花算法
        List<UserAuthInfo> userAuthInfoList = new ArrayList<>(imgs.size());
        for (int i=0;i<imgs.size();i++){
            String s = imgs.get(i);
            UserAuthInfo userAuthInfo = new UserAuthInfo();
            userAuthInfo.setImageUrl(imgs.get(i));
            userAuthInfo.setUserId(id);
            userAuthInfo.setSerialno(i + 1);  // 设置序号 ,1 正面  2 反面 3 手持
            userAuthInfo.setAuthCode(authCode); // 是一组身份信息的标识 3 个图片为一组
            userAuthInfoList.add(userAuthInfo);
        }
        userAuthInfoService.saveBatch(userAuthInfoList); // 批量操作
        user.setReviewsStatus(0);
        updateById(user);
        
    }

    /**
     * 修改用户的手机号
     * @param userId
     * @param updatePhoneParam
     * @return
     */
    @Override
    public boolean updatePhone(Long userId,UpdatePhoneParam updatePhoneParam) {
        //1.使用userId查询用户
        User user = getById(userId);

        //2.验证旧手机
        @NotBlank String oldMobile = user.getMobile();
        String oldMobileCode = stringRedisTemplate.opsForValue().get("SMS:VERIFY_OLD_PHONE:" + oldMobile);
        if(!updatePhoneParam.getOldValidateCode().equals(oldMobileCode)){
            throw new IllegalArgumentException("旧手机的验证码错误");
        }

        //3.验证新手机
        String newMobileCode = stringRedisTemplate.opsForValue().get("SMS:CHANGE_PHONE_:" + updatePhoneParam.getNewMobilePhone());
        if(updatePhoneParam.getValidateCode().equals(newMobileCode)){
            throw new IllegalArgumentException("新手机的验证码错误");
        }

        //执行修改手机号,并更新保存
        user.setMobile(updatePhoneParam.getNewMobilePhone());

        return updateById(user);
    }

    /**
     * 检验新的手机号是否可用,若可用,则给新的手机号发送一个验证码
     *
     * @param mobile      新的手机号
     * @param countryCode 国家代码
     * @return
     */
    @Override
    public boolean checkNewPhone(String mobile, String countryCode) {
        //1 新的手机号,没有旧的用户使用
      int count=  count(new LambdaQueryWrapper<User>().eq(User::getMobile, mobile).eq(User::getCountryCode, countryCode));
        if (count > 0) { // 有用户占用这个手机号
            throw new IllegalArgumentException("该手机号已经被占用");
        }
        Sms sms=new Sms();
        sms.setMobile(mobile);
        sms.setCountryCode(countryCode);
        sms.setTemplateCode("CHANGE_PHONE_VERIFY");//模板代码  -- > 校验手机号
        return smsService.sendSms(sms);

    }
    /**
     * 修改用户的登录密码
     *
     * @param userId           用户的ID
     * @param updateLoginParam 修改密码的表单参数
     * @return
     */
    @Override
    public boolean updateUserLoginPwd(Long userId, UpdateLoginParam updateLoginParam) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户的Id错误");
        }
      String oldpassword = updateLoginParam.getOldpassword();
        // 1 校验之前的密码 数据库的密码都是我们加密后的密码.-->
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        boolean matches = bCryptPasswordEncoder.matches(updateLoginParam.getOldpassword(), user.getPassword());
        if (!matches) {
            throw new IllegalArgumentException("用户的原始密码输入错误");
        }
        // 2 校验手机的验证码
        String validateCode = updateLoginParam.getValidateCode();
        String phoneValidateCode = stringRedisTemplate.opsForValue().get("SMS:CHANGE_LOGIN_PWD_VERIFY:" + user.getMobile());//"SMS:CHANGE_LOGIN_PWD_VERIFY:111111"
        if (!validateCode.equals(phoneValidateCode)) {
            throw new IllegalArgumentException("手机验证码错误");
        }
        user.setPassword(bCryptPasswordEncoder.encode(updateLoginParam.getNewpassword())); // 修改为加密后的密码
        return updateById(user);
    }

    /**
     * 修改用户的交易密码
     *
     * @param userId           用户的Id
     * @param updateLoginParam 修改交易密码的表单参数
     * @return
     */
    @Override
    public boolean updateUserPayPwd(Long userId, UpdateLoginParam updateLoginParam) {
        // 1 查询之前的用户
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户的Id错误");
        }
        String oldpassword = updateLoginParam.getOldpassword();
        // 1 校验之前的密码 数据库的密码都是我们加密后的密码.-->
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        boolean matches = bCryptPasswordEncoder.matches(updateLoginParam.getOldpassword(), user.getPaypassword());
        if (!matches) {
            throw new IllegalArgumentException("用户的原始密码输入错误");
        }
        // 2 校验手机的验证码
        String validateCode = updateLoginParam.getValidateCode();
        String phoneValidateCode = stringRedisTemplate.opsForValue().get("SMS:CHANGE_PAY_PWD_VERIFY:" + user.getMobile());//"SMS:CHANGE_LOGIN_PWD_VERIFY:111111"
        if (!validateCode.equals(phoneValidateCode)) {
            throw new IllegalArgumentException("手机验证码错误");
        }
        user.setPaypassword(bCryptPasswordEncoder.encode(updateLoginParam.getNewpassword())); // 修改为加密后的密码
        return updateById(user);
    }

    /**
     * 重置用户的支付密码
     *
     * @param userId                用户的Id
     * @param unsetPayPasswordParam 重置的表单参数
     * @return 是否重置成功
     */
    @Override
    public boolean unsetPayPassword(Long userId, UnSetPayPasswordParam unsetPayPasswordParam) {
        User user = getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户的Id 错误");
        }
        String validateCode = unsetPayPasswordParam.getValidateCode();
        String phoneValidate = stringRedisTemplate.opsForValue().get("SMS:FORGOT_PAY_PWD_VERIFY:" + user.getMobile());
        if (!validateCode.equals(phoneValidate)) {
            throw new IllegalArgumentException("用户的验证码错误");
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user.setPaypassword(bCryptPasswordEncoder.encode(unsetPayPasswordParam.getPayPassword()));

        return updateById(user);
    }

    @Override
    public List<User> getUserInvites(Long userId) {
        List<User> list=list(new LambdaQueryWrapper<User>().eq(User::getDirectInviteid,userId));
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }

        list.forEach(user -> {
            user.setPaypassword("*********");
            user.setPassword("********");
            user.setAccessKeyId("*********");
            user.setAccessKeySecret("*********");
        });
        return list;
    }

    /**
     * 通过用户的id批量查询用户的基础信息
     * @param ids
     * @return
     */
    @Override
    public List<UserDto> getBasicUsers(List<Long> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return Collections.emptyList();
        }
        List<User> list = list(new LambdaQueryWrapper<User>().in(User::getId,ids));
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        //将user 转化成userDto
        List<UserDto> userDtos=UserDtoMapper.INSTANCE.convert2Dto(list);

          return userDtos;

    }


}