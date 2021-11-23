package com.elevenchu.vo;

import com.elevenchu.domain.User;
import com.elevenchu.domain.UserAuthAuditRecord;
import com.elevenchu.domain.UserAuthInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value = "用户认证的详细信息")
public class UseAuthInfoVo implements Serializable {
    @ApiModelProperty("用户")
    private User user;

    @ApiModelProperty("用户认证的详情列表")
    private List<UserAuthInfo> userAuthInfoList;

    @ApiModelProperty("用户审核历史")
    private List<UserAuthAuditRecord> authAuditRecordList;

}
