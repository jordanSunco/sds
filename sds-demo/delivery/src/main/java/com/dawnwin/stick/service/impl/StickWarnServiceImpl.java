package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dawnwin.stick.mapper.StickWarnMapper;
import com.dawnwin.stick.model.StickWarn;
import com.dawnwin.stick.service.StickWarnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StickWarnServiceImpl extends ServiceImpl<StickWarnMapper, StickWarn> implements StickWarnService {
    @Autowired
    private StickWarnMapper stickWarnMapper;
}
