package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.dto.UserDto;
import com.elevenchu.feign.UserServiceFeign;
import com.elevenchu.service.WorkIssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.WorkIssueMapper;
import com.elevenchu.domain.WorkIssue;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkIssueServiceImpl extends ServiceImpl<WorkIssueMapper, WorkIssue> implements WorkIssueService {
    @Autowired
    private UserServiceFeign userServiceFeign;
    @Override
    public Page<WorkIssue> findByPage(Page<WorkIssue> page, Integer status, String startTime, String endTime) {
        /**
         * 条件分页查询工单列表
         *
         * @param page      分页参数
         * @param status    工单的状态
         * @param startTime 查询的工单创建起始时间
         * @param endTime   查询的工单创建截至时间
         * @return
         */

        Page<WorkIssue> pageData = page(page, new LambdaQueryWrapper<WorkIssue>()
                .eq(status != null, WorkIssue::getStatus, status)
                .between(
                        !StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime),
                        WorkIssue::getCreated,
                        startTime, endTime + " 23:59:59")
        );
        List<WorkIssue> records = pageData.getRecords();
        if(CollectionUtils.isEmpty(records)){
            return pageData;
        }


        //1.收集Id
        List<Long> userIds = records.stream().map(WorkIssue::getUserId).collect(Collectors.toList());
        //2.远程调用
        List<UserDto> basicUsers = userServiceFeign.getBasicUsers(userIds);
        //2.1小技巧 list->map
        if(CollectionUtils.isEmpty(basicUsers)){
            return pageData;
        }
        Map<Long, UserDto> idMapUserDtos = basicUsers.stream().collect(Collectors
                .toMap(userDto -> userDto.getId(),//key
                        userDto -> userDto));//value

        records.forEach(workIssue -> {//循环每一个workIssue，给它里面设置用户的信息map.get(userId)
            UserDto userDto=idMapUserDtos.get(workIssue.getUserId());
            workIssue.setUsername(userDto==null?"测试用户":userDto.getUsername());
            workIssue.setRealName(userDto==null?"测试用户":workIssue.getRealName());

        });

        return pageData;

    }
}
