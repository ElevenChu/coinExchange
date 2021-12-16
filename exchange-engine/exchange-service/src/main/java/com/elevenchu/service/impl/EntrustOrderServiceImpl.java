package com.elevenchu.service.impl;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elevenchu.domain.EntrustOrder;
import com.elevenchu.mapper.EntrustOrderMapper;
import com.elevenchu.service.EntrustOrderService;
@Service
public class EntrustOrderServiceImpl extends ServiceImpl<EntrustOrderMapper, EntrustOrder> implements EntrustOrderService{

}
