package com.elevenchu.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.TurnoverRecord;
import com.elevenchu.mapper.TurnoverRecordMapper;
import com.elevenchu.service.TurnoverRecordService;
@Service
public class TurnoverRecordServiceImpl extends ServiceImpl<TurnoverRecordMapper, TurnoverRecord> implements TurnoverRecordService{

}
