package com.elevenchu.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.Coin;
import com.elevenchu.mapper.CoinMapper;
import com.elevenchu.service.CoinService;
@Service
public class CoinServiceImpl extends ServiceImpl<CoinMapper, Coin> implements CoinService{

}
