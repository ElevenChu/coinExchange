package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.TradeAreaMapper;
import com.elevenchu.domain.TradeArea;
import com.elevenchu.service.TradeAreaService;
import org.springframework.util.StringUtils;

@Service
public class TradeAreaServiceImpl extends ServiceImpl<TradeAreaMapper, TradeArea> implements TradeAreaService{

    @Override
    public Page<TradeArea> findByPage(Page<TradeArea> page, String name, Byte status) {
        return page(page,new LambdaQueryWrapper<TradeArea>().eq(status!=null,TradeArea::getStatus,status)
                                                            .like(!StringUtils.isEmpty(name),TradeArea::getName,name)             );
    }
}
