package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dawnwin.stick.mapper.StickFenceMapper;
import com.dawnwin.stick.model.StickFence;
import com.dawnwin.stick.service.StickFenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StickFenceServiceImpl extends ServiceImpl<StickFenceMapper, StickFence> implements StickFenceService {
    @Autowired
    private StickFenceMapper stickFenceMapper;
}
