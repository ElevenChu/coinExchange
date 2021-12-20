package com.elevenchu.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.TurnoverOrder;
import com.elevenchu.mapper.TurnoverOrderMapper;
import com.elevenchu.service.TurnoverOrderService;
@Service
public class TurnoverOrderServiceImpl extends ServiceImpl<TurnoverOrderMapper, TurnoverOrder> implements TurnoverOrderService{

    @Override
    public Page<TurnoverOrder> findByPage(Page<TurnoverOrder> page, Long userId, String symbol, Integer type) {
        return null;
    }
}
