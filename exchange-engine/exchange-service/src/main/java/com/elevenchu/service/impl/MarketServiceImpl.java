package com.elevenchu.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.mapper.MarketMapper;
import com.elevenchu.domain.Market;
import com.elevenchu.service.MarketService;
@Service
public class MarketServiceImpl extends ServiceImpl<MarketMapper, Market> implements MarketService{

}
