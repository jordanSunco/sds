package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dawnwin.stick.mapper.StickHeartBloodMapper;
import com.dawnwin.stick.model.StickHeartBlood;
import com.dawnwin.stick.service.StickHeartBloodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StickHeartBloodServiceImpl extends ServiceImpl<StickHeartBloodMapper, StickHeartBlood> implements StickHeartBloodService {
    @Autowired
    private StickHeartBloodMapper stickHeartBloodMapper;
}
