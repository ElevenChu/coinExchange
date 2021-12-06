package com.elevenchu.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.CoinWithdraw;
import com.elevenchu.mapper.CoinWithdrawMapper;
import com.elevenchu.service.CoinWithdrawService;
@Service
public class CoinWithdrawServiceImpl extends ServiceImpl<CoinWithdrawMapper, CoinWithdraw> implements CoinWithdrawService{

}
