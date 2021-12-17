package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.WorkIssue;
import com.baomidou.mybatisplus.extension.service.IService;
public interface WorkIssueService extends IService<WorkIssue>{


    Page<WorkIssue> findByPage(Page<WorkIssue> page, Integer status, String startTime, String endTime);

    Page<WorkIssue> getIssueList(Page<WorkIssue> page, Long userId);
}
