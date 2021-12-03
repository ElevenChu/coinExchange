package com.elevenchu.controller;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.User;
import com.elevenchu.domain.UserAuthAuditRecord;
import com.elevenchu.domain.UserAuthInfo;
import com.elevenchu.model.*;
import com.elevenchu.service.UserAuthAuditRecordService;
import com.elevenchu.service.UserAuthInfoService;
import com.elevenchu.service.UserService;
import com.elevenchu.vo.UseAuthInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@Api("会员的控制器")
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserAuthAuditRecordService userAuthAuditRecordService;
    @Autowired
    private UserAuthInfoService userAuthInfoService;

    @GetMapping
    @ApiOperation(value = "查询会员的列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数"),
            @ApiImplicitParam(name = "mobile", value = "会员的手机号"),
            @ApiImplicitParam(name = "userId", value = "会员的Id,精确查询"),
            @ApiImplicitParam(name = "userName", value = "会员的名称"),
            @ApiImplicitParam(name = "realName", value = "会员的真实名称"),
            @ApiImplicitParam(name = "status", value = "会员的状态")

    })
    @PreAuthorize("hasAuthority('user_query')")
    public R<Page<User>> findByPage(@ApiIgnore Page<User> page,
                                    String mobile,
                                    Long userId,
                                    String userName,
                                    String realName,
                                    Integer status
    ) {
        page.addOrder(OrderItem.desc("last_update_time"));
        Page<User> userPage = userService.findByPage(page, mobile, userId, userName, realName, status,null);
        return R.ok(userPage);
    }
    @PostMapping("/status")
    @ApiOperation("修改用户状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "用户的ID"),
            @ApiImplicitParam(name = "status",value = "用户的状态")
    })

    public R add(Long id,Byte status){
        User user=new User();
        user.setId(id);
        user.setStatus(status);
        boolean b = userService.updateById(user);
        if(b){
            return R.ok("更新成功");
        }
        return R.fail("更新失败");

    }

    @PatchMapping
    @ApiOperation(value = "修改用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user", value = "会员的json数据"),
    })
    @PreAuthorize("hasAuthority('user_update')")
    public R updateStatus(@RequestBody @Validated User user) {
        boolean updateById = userService.updateById(user);
        if (updateById) {
            return R.ok("更新成功");
        }
        return R.fail("更新失败");
    }

    @GetMapping("/info")
    @ApiOperation(value = "查询会员的详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "会员的Id")
    })
    public R<User> userInfo(Long id) {
        User user = userService.getById(id);
        return R.ok(user);
    }


    @GetMapping("/directInvites")
    @ApiOperation(value = "查询该用户邀请的用户列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "该用户的Id"),
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数"),

    })
    public R<Page<User>> getDirectInvites(@ApiIgnore Page<User> page, Long userId) {
        Page<User> userPage = userService.findDirectInvitePage(page, userId);
        return R.ok(userPage);


    }
    @GetMapping("/auths")
    @ApiOperation(value = "查询用户的审核列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "size", value = "每页显示的条数"),
            @ApiImplicitParam(name = "mobile", value = "会员的手机号"),
            @ApiImplicitParam(name = "userId", value = "会员的Id,精确查询"),
            @ApiImplicitParam(name = "userName", value = "会员的名称"),
            @ApiImplicitParam(name = "realName", value = "会员的真实名称"),
            @ApiImplicitParam(name = "reviewsStatus", value = "会员的状态")

    })
    public R<Page<User>> findUserAuths(
            @ApiIgnore Page<User> page,
            String mobile,
            Long userId,
            String userName,
            String realName,
            Integer reviewsStatus
    ) {

        Page<User> userPage = userService.findByPage(page, mobile, userId, userName, realName, null, reviewsStatus);
        return R.ok(userPage);

    }

    @GetMapping("/auth/info")
    @ApiOperation(value = "查询用户详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name ="id",value = "用户的ID")
    })
    public R<UseAuthInfoVo> getUserAuthInfo(Long id){
        User user = userService.getById(id);
        List<UserAuthAuditRecord> userAuthAuditRecordList=null;
        List<UserAuthInfo> userAuthInfoList=null;
        if(user!=null){
            //用户的审核记录
            Integer reviewsStatus = user.getReviewsStatus();
            if(reviewsStatus==null||reviewsStatus==0){
                //待审核
                userAuthAuditRecordList= Collections.emptyList();
                userAuthInfoList=userAuthInfoService.getUserAuthInfoByUserId(id);
            }else{
                userAuthAuditRecordList= userAuthAuditRecordService.getUserAuthAuditRecordList(id);
                //查询用户的认证详情列表->用户的身份信息
                UserAuthAuditRecord userAuthAuditRecord = userAuthAuditRecordList.get(0);//查询是按照日期顺序排列的,第0个为最新查询的记录
                Long authCode = userAuthAuditRecord.getAuthCode();//认证的唯一标识
                userAuthInfoList= userAuthInfoService.getUserAuthInfoByCode(authCode);



            }
        }
                return R.ok(new UseAuthInfoVo(user,userAuthInfoList,userAuthAuditRecordList));
    }

    @PostMapping("/auths/status")
    @ApiImplicitParams({
            @ApiImplicitParam(name ="id" ,value = "用户ID"),
            @ApiImplicitParam(name ="authStatus" ,value = "用户的审核状态"),
            @ApiImplicitParam(name ="authCode" ,value = "一组图片的唯一标识"),
            @ApiImplicitParam(name ="remark" ,value = "审核拒绝的原因")
    })
    public R updateUserAuthStatus(@RequestParam(required = true) Long id,@RequestParam(required = true)Byte authStatus,@RequestParam(required = true)Long authCode,@RequestParam(required = true)String remark){
        //1.修改User里面的ReviewStatus
        //2.在authAuditRecord里面添加一条记录
        userService.updateUserAuthStatus(id,authStatus,authCode,remark);
        return R.ok();



    }

    @PostMapping("/authAccount")
    @ApiOperation("用户的实名认证")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "",value = "")
    })
    public R  identifyCheck(@RequestBody UserAuthForm userAuthForm){
        String idStr = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        boolean isOk=   userService.identifyVerfiy(Long.valueOf(idStr),userAuthForm);
        if(isOk){
            return R.ok();
        }
        return R.fail("认证失败");
    }

@PostMapping("/authUser")
@ApiOperation("用户进行高级认证")
@ApiImplicitParams({
        @ApiImplicitParam(name = "imgs",value = "用户的图片地址")
})
 public R authUser(String[] imgs){
    String idStr = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
    userService.authUser(Long.valueOf(idStr), Arrays.asList(imgs));
return R.ok();
}

@PostMapping("/updatePhone")
@ApiOperation("修改手机号")
@ApiImplicitParams({
        @ApiImplicitParam(name = "updatePhoneParam",value = "updatePhoneParam的JSON数据")

})
public R updatePhone(@RequestBody UpdatePhoneParam updatePhoneParam){
    String idStr = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
         boolean isOk=   userService.updatePhone(Long.valueOf(idStr),updatePhoneParam);
        if (isOk){
            return R.ok();
        }
        return R.fail("修改手机号失败");
}
@GetMapping("/checkTel")
@ApiOperation("检查新的手机号是否可用,如可用,则给该新手机发送验证码")
@ApiImplicitParams({
        @ApiImplicitParam(name = "mobile", value = "新的手机号"),
        @ApiImplicitParam(name = "countryCode", value = "手机号的区域")
})
public R checkNewPhone(@RequestParam(required = true) String mobile,@RequestParam(required = true)String countryCode){
    boolean isOk = userService.checkNewPhone(mobile, countryCode);
    return isOk ? R.ok() : R.fail("新的手机号校验失败");


}
    @PostMapping("/updateLoginPassword")
    @ApiOperation(value = "修改用户的登录密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "updateLoginParam", value = "修改用户的登录密码")
    })
public R updateLoginPwd(@RequestBody @Validated UpdateLoginParam updateLoginParam){
      Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
      boolean isOk=userService.updateUserLoginPwd(userId,updateLoginParam);
        if (isOk) {
            return R.ok();
        }
        return R.fail("修改失败");

}

    @PostMapping("/updatePayPassword")
    @ApiOperation(value = "修改用户的交易密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "updateLoginParam", value = "修改用户的交易密码")
    })
    public R updatePayPwd(@RequestBody @Validated UpdateLoginParam updateLoginParam) {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        boolean isOk = userService.updateUserPayPwd(userId, updateLoginParam);
        if (isOk) {
            return R.ok();
        }
        return R.fail("修改失败");
    }


    @PostMapping("/setPayPassword")
    @ApiOperation(value = "重新设置交易密码")
    public R setPayPassword(@RequestBody @Validated UnSetPayPasswordParam unsetPayPasswordParam) {
        Long userId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        boolean isOk = userService.unsetPayPassword(userId, unsetPayPasswordParam);
        if (isOk) {
            return R.ok();
        }
        return R.fail("重置失败");
    }



}
