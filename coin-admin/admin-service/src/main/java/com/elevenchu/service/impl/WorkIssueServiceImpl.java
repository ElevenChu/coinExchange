package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.WorkIssueMapper;
import com.elevenchu.domain.WorkIssue;
import com.elevenchu.service.WorkIssueService;
@Service
public class WorkIssueServiceImpl extends ServiceImpl<WorkIssueMapper, WorkIssue> implements WorkIssueService{

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

            return page(page, new LambdaQueryWrapper<WorkIssue>()
                    .eq(status != null, WorkIssue::getStatus, status)
                    .between(
                            !StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime),
                            WorkIssue::getCreated,
                            startTime, endTime + " 23:59:59")
            );
    }
}
