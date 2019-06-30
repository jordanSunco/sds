package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dawnwin.stick.mapper.StickGPSMapper;
import com.dawnwin.stick.mapper.StickUserMapper;
import com.dawnwin.stick.model.StickGPS;
import com.dawnwin.stick.model.StickUser;
import com.dawnwin.stick.service.StickGPSService;
import com.dawnwin.stick.service.StickUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StickGPSServiceImpl extends ServiceImpl<StickGPSMapper, StickGPS> implements StickGPSService {
    @Autowired
    private StickGPSMapper stickGPSMapper;

    @Override
    public StickGPS getLatestGPS(int deviceId) {
        StickGPS cond = new StickGPS();
        cond.setDeviceId(deviceId);
        StickGPS gps = selectOne(new EntityWrapper<StickGPS>(cond).orderBy("gps_time", false));
        return gps;
    }
}
