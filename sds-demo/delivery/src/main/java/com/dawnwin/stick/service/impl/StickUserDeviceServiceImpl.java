package com.dawnwin.stick.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.dawnwin.stick.mapper.StickUserDeviceMapper;
import com.dawnwin.stick.model.StickDevice;
import com.dawnwin.stick.model.StickUserDevice;
import com.dawnwin.stick.service.StickUserDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StickUserDeviceServiceImpl extends ServiceImpl<StickUserDeviceMapper, StickUserDevice> implements StickUserDeviceService {
    @Autowired
    private StickUserDeviceMapper stickUserDeviceMapper;

    @Override
    public List<StickDevice> listLoveDeviceByUserId(int userId) {
        return stickUserDeviceMapper.listLoveDeviceByUserId(userId);
    }
}
