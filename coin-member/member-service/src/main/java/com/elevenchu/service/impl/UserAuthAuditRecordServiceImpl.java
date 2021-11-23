package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.UserAuthAuditRecordMapper;
import com.elevenchu.domain.UserAuthAuditRecord;
import com.elevenchu.service.UserAuthAuditRecordService;
@Service
public class UserAuthAuditRecordServiceImpl extends ServiceImpl<UserAuthAuditRecordMapper, UserAuthAuditRecord> implements UserAuthAuditRecordService{

    @Override
    public List<UserAuthAuditRecord> getUserAuthAuditRecordList(Long id) {
        return  list(new LambdaQueryWrapper<UserAuthAuditRecord>()
        .eq(UserAuthAuditRecord::getUserId,id)
        .orderByDesc(UserAuthAuditRecord::getCreated)
        .last("limit 3"));
    }
}
