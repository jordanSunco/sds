package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dawnwin.stick.mapper.StickUserDeviceMapper;
import com.dawnwin.stick.model.StickDevice;
import com.dawnwin.stick.model.StickUserDevice;
import com.dawnwin.stick.service.StickUserDeviceService;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class StickUserDeviceServiceImpl extends ServiceImpl<StickUserDeviceMapper, StickUserDevice> implements StickUserDeviceService {
    @Autowired
    private StickUserDeviceMapper stickUserDeviceMapper;

    @Override
    public List<StickDevice> listDevicesByUserId(int userId) {
        return stickUserDeviceMapper.listDevicesByUserId(userId);
    }

    @Override
    public void deleteByUserAndDevice(int userId, int deviceId) {
        Map<String,Object> params = Maps.newHashMap();
        params.put("userId", userId);
        params.put("deviceId", deviceId);
        stickUserDeviceMapper.deleteByUserIdAndDeviceId(params);
    }
}
