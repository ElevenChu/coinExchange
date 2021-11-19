package com.elevenchu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elevenchu.domain.Notice;
import com.baomidou.mybatisplus.extension.service.IService;
public interface NoticeService extends IService<Notice>{


    Page<Notice> findByPage(Page<Notice> page, String title, String startTime, String endTime, Integer status);
}
