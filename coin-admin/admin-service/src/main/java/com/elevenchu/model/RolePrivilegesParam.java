package com.elevenchu.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@ApiModel(value = "接收角色和权限数据")
public class RolePrivilegesParam {
    @ApiModelProperty(value = "角色的ID")
    private Long roleId;

    @ApiModelProperty("角色包含的权限")
    private List<Long> privilegesIds= Collections.emptyList();


}
