package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
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

    @Override
    public StickHeartBlood getLatestHeartBlood(int deviceId) {
        StickHeartBlood cond = new StickHeartBlood();
        cond.setDeviceId(deviceId);
        return selectOne(new EntityWrapper<StickHeartBlood>().orderBy("add_time", false));
    }
}
