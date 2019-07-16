package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dawnwin.stick.mapper.StickDeviceMapper;
import com.dawnwin.stick.mapper.StickUserMapper;
import com.dawnwin.stick.model.StickDevice;
import com.dawnwin.stick.model.StickUser;
import com.dawnwin.stick.service.StickDeviceService;
import com.dawnwin.stick.service.StickUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StickDeviceServiceImpl extends ServiceImpl<StickDeviceMapper, StickDevice> implements StickDeviceService {
    @Autowired
    private StickDeviceMapper stickDeviceMapper;

    @Override
    public StickDevice findDeviceByImei(String imei) {
        StickDevice cond = new StickDevice();
        cond.setDeviceImei(imei);
        return selectOne(new EntityWrapper<>(cond));
    }

    @Override
    public List<StickDevice> listDeviceByUserId(int userId) {
        StickDevice cond = new StickDevice();
        cond.setUserId(userId);
        return selectList(new EntityWrapper<>(cond));
    }
}