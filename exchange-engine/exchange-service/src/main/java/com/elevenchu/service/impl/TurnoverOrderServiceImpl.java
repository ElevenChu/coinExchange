package com.elevenchu.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.TurnoverOrder;
import com.elevenchu.mapper.TurnoverOrderMapper;
import com.elevenchu.service.TurnoverOrderService;
@Service
public class TurnoverOrderServiceImpl extends ServiceImpl<TurnoverOrderMapper, TurnoverOrder> implements TurnoverOrderService{

}
